package terminalGame;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import backend.battle.BattleManager;
import backend.battle.BattleManager.AnswerResult;
import backend.battle.BattleManager.BattleQuestion;
import backend.battle.BattleManager.Effect;
import backend.battle.BattleManager.Phase;
import backend.battle.BattleManager.Skill;
import backend.battle.BattleManager.Status;
import backend.battle.BattleResourceManager;
import backend.model.Boss;
import backend.model.BossManager;
import backend.model.Category;

public class TerminalGame implements BattleManager.BattleView {

    public static Scanner input = new Scanner(System.in);
    // One reader owns the keyboard, even if a burn ends a battle during a pending read.
    private static final ExecutorService keyboard = Executors.newSingleThreadExecutor(task -> {
        Thread reader = new Thread(task, "terminal-keyboard");
        reader.setDaemon(true);
        return reader;
    });
    private record Line(String text, long enteredAt) {}
    private static Future<Line> pendingLine;

    private static Line readWithin(long nanos) throws InterruptedException, TimeoutException {
        if (pendingLine == null) {
            pendingLine = keyboard.submit(() -> new Line(input.hasNextLine() ? input.nextLine() : null,
                    System.nanoTime()));
        }
        try {
            Line line = pendingLine.get(nanos, TimeUnit.NANOSECONDS);
            pendingLine = null;
            return line;
        } catch (ExecutionException e) {
            pendingLine = null;
            throw new IllegalStateException("Could not read terminal input", e.getCause());
        }
    }

    private static String readMenuLine() {
        try {
            Line line = readWithin(Long.MAX_VALUE);
            if (line.text() == null) throw new java.util.NoSuchElementException("End of input");
            return line.text();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Menu input interrupted", e);
        } catch (TimeoutException e) {
            throw new IllegalStateException(e);
        }
    }

    private static final BattleResourceManager resourceManager = new BattleResourceManager();

    public static void main(String[] args){
        titleFrame();
        mainMenu();
    }

    public static void titleFrame(){
        System.out.println("```ASCEND```");
        System.out.println("------------");
        if (!PlayerAccount.accountExists()) {
            createAccount(false);
            return;
        }

        while (true) {
            System.out.println("You already have saved progress!!");
            System.out.print("[1] CONTINUE [2] NEW GAME: ");
            switch (readMenuLine().trim()) {
                case "1" -> {
                    return;
                }
                case "2" -> {
                    createAccount(true);
                    return;
                }
                default -> System.out.println("Wrong choice");
            }
        }
    }

    private static void createAccount(boolean replaceExistingSave) {
        System.out.println("---CREATE ACCOUNT---");
        System.out.print("name: ");
        String username = readMenuLine();
        System.out.print("age: ");
        int age = Integer.parseInt(readMenuLine().trim());
        System.out.print("character name: ");
        String characterName = readMenuLine();

        try {
            PlayerAccount.createAccount(username, age, characterName, replaceExistingSave);
        } catch (IOException e) {
            System.err.println("Could not save your account: " + e.getMessage());
        }
    }

    public static void play(){
        System.out.println("\n----------");
        System.out.println("--PORTAL--");
        System.out.println("----------\n");
        System.out.println("[1] MATHEMATICS");
        System.out.println("[2] PROGRAMMING");
        System.out.println("[3] SCIENCE");
        System.out.print("choice: ");
        char choice = readMenuLine().charAt(0);

        switch (choice) {
            case '1' -> bossChoice(Category.MATHEMATICS);
            case '2' -> bossChoice(Category.PROGRAMMING);
            case '3' -> bossChoice(Category.SCIENCE);
            default -> System.out.println("Wrong input");
        }
    }

    public static void mainMenu(){
        System.out.println("\n----------");
        System.out.println("MAIN MENU");
        System.out.println("---------");
        System.out.println("[1] PlAY");
        System.out.println("[2] SETTINGS");
        System.out.println("[3] PROFILE");
        System.out.println("[4] ABOUT US");
        System.out.print("choose: ");
        char choice = readMenuLine().charAt(0);
        switch (choice){
            case '1':
                play();
                break;
            case '2':
            case '3':
            case '4':
            default:
                System.out.println("Wrong choice");
        }

    }

    public static void bossChoice(Category category){
        System.out.println("\n----------");
        System.out.println(category);
        System.out.println("---------");
        List<Boss> bosses = new BossManager().getBosses(category);
        for (int index = 0; index < bosses.size(); index++) {
            System.out.printf("[%d] %s%n", index + 1, bosses.get(index).getName());
        }
        System.out.print("choice: ");
        char choice = readMenuLine().charAt(0);


        switch (choice) {
            case '1', '2', '3' -> {
                int bossNumber = choice - '0';
                BattleManager battle = new BattleManager(resourceManager);
                battle.startBattle(category, bossNumber, new TerminalGame());
                runBattle(battle);
            }
            default ->
                System.out.println("Wrong input");
        }
    }

    private static void runBattle(BattleManager battle) {
        try {
            while (battle.getPhase() != Phase.FINISHED) {
                Phase phase = battle.getPhase();
                try {
                    Line line = readWithin(battle.nanosUntilUpdate());
                    if (line.text() == null) break;
                    if (phase == Phase.ANSWER) {
                        battle.submitAnswer(line.text(), line.enteredAt());
                    } else if (phase == Phase.SKILL) {
                        switch (line.text().trim()) {
                            case "" -> battle.chooseSkill(Skill.SKIP);
                            case "1" -> battle.chooseSkill(Skill.HEAL);
                            case "2" -> battle.chooseSkill(Skill.DAMAGE);
                            default -> System.out.println("Choose 1, 2, or press Enter to skip.");
                        }
                    }
                } catch (TimeoutException timerDue) {
                    // The next update reveals choices or applies a burn tick.
                }
                battle.updateTime();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            battle.leaveBattle();
        }
    }

    @Override
    public void showDialogue(String dialogue) {
        System.out.println(dialogue);
    }

    @Override
    public void showStatus(Status status) {
        System.out.printf("%s: %d HP | %s: %d HP%n", status.characterName(), status.characterHP(),
                status.bossName(), status.bossHP());
        System.out.printf("Skill: %d/3 | Consecutive misses: %d/2", status.skill(), status.misses());
        if (status.hasSpecialMeter()) System.out.printf(" | Special: %d/5", status.special());
        if (status.corruptionTurns() > 0) System.out.printf(" | Runtime Corruption: %d turns left", status.corruptionTurns());
        System.out.println();
    }

    @Override
    public void showBattleEnd(String bossName, boolean playerWon) {
        System.out.println(playerWon ? "You defeated " + bossName + "!" : bossName + " defeated you.");
    }

    @Override
    public void showPreview(String question) {
        System.out.println();
        System.out.println("Your turn. Preview the question for 10 seconds.");
        System.out.println(question);
    }

    @Override
    public void showAnswerPrompt(List<String> choices) {
        for (int index = 0; index < choices.size(); index++) {
            System.out.printf("%c) %s%n", 'a' + index, choices.get(index));
        }
        System.out.println(choices.isEmpty() ? "Your turn to answer. Type your answer, or press Enter to leave."
                : "Your turn to answer. Choose a, b, or c, or press Enter to leave.");
    }

    @Override
    public void showSkillPrompt() {
        System.out.println("Skill ready!\n[1] Heal\n[2] Damage\nPress Enter to skip.");
    }

    @Override
    public void showAnswerResult(AnswerResult result, BattleQuestion question, int damage) {
        switch (result) {
            case CORRECT -> System.out.printf("Correct! You dealt %d damage.%n", damage);
            case INCORRECT -> System.out.printf("Incorrect. The answer is %s. The boss dealt %d damage.%n", question.answer(), damage);
            case INVALID_CHOICE -> System.out.println("Enter a, b, or c.");
        }
    }

    @Override
    public void showEffect(Effect effect, int amount) {
        switch (effect) {
            case PASSIVE_HEAL -> System.out.printf("Boss passive activated. The boss recovered %d HP.%n", amount);
            case PROOF_BY_RECOVERY -> System.out.printf("Proof by Recovery activated. The boss recovered %d HP.%n", amount);
            case MC_HEAL -> System.out.printf("Heal activated. You recovered %d HP.%n", amount);
            case MC_DAMAGE -> System.out.printf("Damage skill activated. You dealt %d damage.%n", amount);
            case BURN_STARTED -> System.out.println("Thermal Burn activated for 60 seconds of answer time.");
            case BURN_DAMAGE -> System.out.printf("Thermal Burn dealt %d damage to you.%n", amount);
            case BURN_ENDED -> System.out.println("Thermal Burn expired.");
            case CORRUPTION_STARTED -> System.out.printf("Runtime Corruption activated for %d turns.%n", amount);
            case CORRUPTION_ENDED -> System.out.println("Runtime Corruption expired.");
        }
    }

}
