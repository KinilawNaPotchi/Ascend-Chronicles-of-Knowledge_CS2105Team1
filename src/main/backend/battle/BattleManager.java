package backend.battle;

import backend.model.Category;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.LongSupplier;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Start a battle, submit answers and skill choices, and call updateTime at each timer deadline
 * (or on a UI animation pulse). All commands and view callbacks run on the caller's event thread.
 */
public class BattleManager {
    private final BattleResourceManager resourceManager;
    private final SaveManager saves;
    private SaveManager.Player player;
    private final List<Integer> shownQuestionIds = new ArrayList<>();
    private final LongSupplier clock;
    private final Random random;
    private BattleView view;
    private Category category;
    private int bossNumber;
    private String characterName;
    private BattleHealth health;
    private BattleCombat combat;
    private BattleQuestion question;
    private Phase phase = Phase.FINISHED;
    private long phaseStarted;
    private long lastBurnUpdate;

    public BattleManager(BattleResourceManager resourceManager) {
        this(resourceManager, new SaveManager());
    }

    public BattleManager(BattleResourceManager resourceManager, SaveManager saves) {
        this(resourceManager, saves, System::nanoTime, new Random());
    }

    BattleManager(BattleResourceManager resourceManager, SaveManager saves, LongSupplier clock, Random random) {
        this.resourceManager = resourceManager;
        this.saves = saves;
        this.clock = clock;
        this.random = random;
    }

    /** Call battle commands and updateTime on one UI/event thread. */
    public boolean startBattle(Category category, int bossNumber, BattleView view) {
        player = saves.currentPlayer();
        if (!saves.progress(category).allows(bossNumber)) return false;
        this.category = category;
        this.bossNumber = bossNumber;
        this.view = view;
        shownQuestionIds.clear();
        characterName = player.characterName();
        health = new BattleHealth(characterName, player.level(), category, bossNumber);
        view.showDialogue(resourceManager.getBossBeforeDialog(category, bossNumber));
        view.showDialogue(resourceManager.getMcBeforeDialog(category, bossNumber, characterName));
        combat = new BattleCombat(health, random, view);
        showStatusAndNextQuestion();
        return true;
    }

    private void showStatusAndNextQuestion() {
        view.showStatus(combat.status(characterName));
        if (health.isBattleOver()) {
            phase = Phase.FINISHED;
            String bossName = health.getBossName();
            boolean playerWon = health.isBossDefeated();
            SaveManager.Victory victory = playerWon ? saves.recordVictory(player.saveId(), category, bossNumber) : null;
            view.showBattleEnd(bossName, playerWon);
            for (String dialogue : resourceManager.getEndDialogue(category, bossName, characterName, playerWon)) {
                view.showDialogue(dialogue);
            }
            if (victory != null && victory.shardAwarded()) view.showShard(victory.level());
            return;
        }
        question = questionSearch(category, bossNumber);
        phase = Phase.PREVIEW;
        view.showPreview(question.question());
        phaseStarted = clock.getAsLong();
    }

    public Phase getPhase() {
        return phase;
    }

    /** How long the UI can wait for input before the next preview or burn event. */
    public long nanosUntilUpdate() {
        long now = clock.getAsLong();
        if (phase == Phase.PREVIEW) return Math.max(0, SECONDS.toNanos(10) - (now - phaseStarted));
        if (phase == Phase.ANSWER && combat.nanosUntilTick() != Long.MAX_VALUE) {
            return Math.max(0, combat.nanosUntilTick() - (now - lastBurnUpdate));
        }
        return Long.MAX_VALUE;
    }

    public void updateTime() {
        long now = clock.getAsLong();
        if (phase == Phase.PREVIEW && now - phaseStarted >= SECONDS.toNanos(10)) {
            view.showAnswerPrompt(question.choices());
            phaseStarted = clock.getAsLong();
            lastBurnUpdate = phaseStarted;
            phase = Phase.ANSWER;
        } else if (phase == Phase.ANSWER) {
            advanceBurn(now);
        }
    }

    private void advanceBurn(long now) {
        if (now - lastBurnUpdate < 0) return;
        combat.advanceBurn(now - lastBurnUpdate);
        lastBurnUpdate = now;
        if (health.isBattleOver()) showStatusAndNextQuestion();
    }

    public void submitAnswer(String response) {
        submitAnswer(response, clock.getAsLong());
    }

    /** For queued input, use its arrival time from the same monotonic clock. */
    public void submitAnswer(String response, long enteredAt) {
        if (phase != Phase.ANSWER || enteredAt - phaseStarted < 0) return;
        advanceBurn(enteredAt);
        if (phase == Phase.FINISHED) return;
        if (response == null || response.trim().isEmpty()) {
            leaveBattle();
            return;
        }
        AnswerResult result = checkAnswer(question, response.trim());
        if (result == AnswerResult.INVALID_CHOICE) {
            view.showAnswerResult(result, question, 0);
            return;
        }
        boolean correct = result == AnswerResult.CORRECT;
        int damage = combat.dealAnswerDamage(correct, enteredAt - phaseStarted);
        view.showAnswerResult(result, question, damage);
        combat.updateMeters(correct);
        if (correct && combat.skillReady() && !health.isBattleOver()) {
            phase = Phase.SKILL;
            view.showSkillPrompt();
        } else {
            finishTurn();
        }
    }

    public void chooseSkill(Skill choice) {
        if (phase != Phase.SKILL) return;
        combat.useSkill(choice);
        finishTurn();
    }

    private void finishTurn() {
        combat.finishTurn();
        showStatusAndNextQuestion();
    }

    public void leaveBattle() {
        phase = Phase.FINISHED;
    }

    private AnswerResult checkAnswer(BattleQuestion question, String response) {
        String answer = response;
        if (!question.choices().isEmpty()) {
            int index = response.charAt(0) - 'a';
            if (response.length() != 1 || index < 0 || index >= question.choices().size()) {
                return AnswerResult.INVALID_CHOICE;
            }
            answer = question.choices().get(index);
        }
        return question.answer().equals(answer) ? AnswerResult.CORRECT : AnswerResult.INCORRECT;
    }

    public BattleQuestion questionSearch(Category category, int bossNumber) {
        int id = resourceManager.getQuestionID(category, bossNumber, shownQuestionIds);
        BattleQuestion result = new BattleQuestion(id, resourceManager.getQuestion(id),
                resourceManager.getAnswer(id), resourceManager.getChoices(id));
        shownQuestionIds.add(id);
        return result;
    }

    public record BattleQuestion(int id, String question, String answer, List<String> choices) {}

    public enum AnswerResult { CORRECT, INCORRECT, INVALID_CHOICE }
    public enum Phase { PREVIEW, ANSWER, SKILL, FINISHED }
    public enum Skill { HEAL, DAMAGE, SKIP }
    public enum Effect {
        PASSIVE_HEAL, PROOF_BY_RECOVERY, MC_HEAL, MC_DAMAGE,
        BURN_STARTED, BURN_DAMAGE, BURN_ENDED, CORRUPTION_STARTED, CORRUPTION_ENDED
    }

    public record Status(String characterName, int characterHP, String bossName, int bossHP,
                         int skill, int misses, boolean hasSpecialMeter, int special,
                         int burnTicksLeft, int corruptionTurns) {}

    public interface BattleView {
        void showDialogue(String dialogue);
        void showStatus(Status status);
        void showBattleEnd(String bossName, boolean playerWon);
        void showShard(int newLevel);
        void showPreview(String question);
        void showAnswerPrompt(List<String> choices);
        void showSkillPrompt();
        void showAnswerResult(AnswerResult result, BattleQuestion question, int damage);
        void showEffect(Effect effect, int amount);
    }
}
