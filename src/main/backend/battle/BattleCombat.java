package backend.battle;

import backend.model.Category;
import backend.battle.BattleManager.BattleView;
import backend.battle.BattleManager.Effect;
import backend.battle.BattleManager.Skill;
import backend.battle.BattleManager.Status;
import java.util.Random;
import static java.util.concurrent.TimeUnit.SECONDS;

/** HP, meters and effects for one battle. All time amounts are in nanoseconds. */
final class BattleCombat {
    private static final long TICK_INTERVAL = SECONDS.toNanos(3);
    private final BattleHealth health;
    private final Random random;
    private final BattleView view;
    private int skill;
    private int misses;
    private int special;
    private int burnTicksLeft;
    private long timeToTick = TICK_INTERVAL;
    private int corruptionTurns;
    private boolean crossedHalfHP;
    private boolean corruptionQueued;

    BattleCombat(BattleHealth health, Random random, BattleView view) {
        this.health = health;
        this.random = random;
        this.view = view;
        if (health.isFinalBoss(Category.PROGRAMMING)) {
            startCorruption();
        }
    }

    Status status(String characterName) {
        boolean hasSpecial = health.isFinalBoss(Category.MATHEMATICS) || health.isFinalBoss(Category.SCIENCE);
        return new Status(characterName, health.getMainCharacterHP(), health.getBossName(),
                health.getBossHP(), skill, misses, hasSpecial, special, burnTicksLeft, corruptionTurns);
    }

    int dealAnswerDamage(boolean correct, long elapsed) {
        if (!correct) return health.damageMainCharacter(10);
        int damage;
        if (elapsed <= SECONDS.toNanos(10)) {
            damage = 10;
        } else if (elapsed <= SECONDS.toNanos(30)) {
            damage = random.nextInt(7, 10);
        } else if (elapsed <= SECONDS.toNanos(60)) {
            damage = random.nextInt(4, 7);
        } else {
            damage = 3;
        }
        return damageBoss(damage);
    }

    private int damageBoss(int amount) {
        int before = health.getBossHP();
        int lost = health.damageBoss(corruptionTurns > 0 ? 3 : amount);
        if (health.isFinalBoss(Category.PROGRAMMING) && !crossedHalfHP && before > 50 && health.getBossHP() <= 50) {
            crossedHalfHP = true;
            corruptionQueued = true;
        }
        return lost;
    }

    void updateMeters(boolean correct) {
        if (correct) {
            misses = 0;
            skill = Math.min(3, skill + 1);
            return;
        }
        if (!skillReady()) skill = 0;
        misses++;
        if (health.isFinalBoss(Category.MATHEMATICS) || health.isFinalBoss(Category.SCIENCE)) {
            special = Math.min(5, special + 1);
        }
        if (health.isBattleOver()) return;
        if (misses == 2) {
            misses = 0;
            view.showEffect(Effect.PASSIVE_HEAL, health.healBoss(5));
        }
        if (special == 5 && health.isFinalBoss(Category.MATHEMATICS)) {
            special = 0;
            view.showEffect(Effect.PROOF_BY_RECOVERY, health.healBoss(random.nextInt(10, 16)));
        } else if (special == 5 && burnTicksLeft == 0) {
            startBurn();
        }
    }

    boolean skillReady() {
        return skill == 3;
    }

    void useSkill(Skill choice) {
        if (choice == Skill.SKIP) return;
        skill = 0;
        int amount = random.nextInt(10, 16);
        if (choice == Skill.HEAL) {
            view.showEffect(Effect.MC_HEAL, health.healMainCharacter(amount));
        } else {
            view.showEffect(Effect.MC_DAMAGE, damageBoss(amount));
        }
    }

    void finishTurn() {
        if (corruptionTurns > 0) {
            corruptionTurns--;
            if (corruptionTurns == 0) {
                view.showEffect(Effect.CORRUPTION_ENDED, 0);
            }
        }
        if (corruptionQueued && corruptionTurns == 0 && !health.isBattleOver()) {
            corruptionQueued = false;
            startCorruption();
        }
    }

    private void startCorruption() {
        corruptionTurns = 3;
        view.showEffect(Effect.CORRUPTION_STARTED, corruptionTurns);
    }

    private void startBurn() {
        special = 0;
        burnTicksLeft = 20;
        timeToTick = TICK_INTERVAL;
        view.showEffect(Effect.BURN_STARTED, 0);
    }

    long nanosUntilTick() {
        return burnTicksLeft == 0 ? Long.MAX_VALUE : timeToTick;
    }

    void advanceBurn(long elapsed) {
        // A delayed UI update can cover several ticks, even the start of a queued burn.
        while (burnTicksLeft > 0 && elapsed >= timeToTick) {
            elapsed -= timeToTick;
            timeToTick = TICK_INTERVAL;
            burnTicksLeft--;
            view.showEffect(Effect.BURN_DAMAGE, health.damageMainCharacter(1));
            if (burnTicksLeft == 0) view.showEffect(Effect.BURN_ENDED, 0);
            if (health.isBattleOver()) return;
            if (burnTicksLeft == 0 && special == 5) startBurn();
        }
        if (burnTicksLeft > 0) timeToTick -= elapsed;
    }
}
