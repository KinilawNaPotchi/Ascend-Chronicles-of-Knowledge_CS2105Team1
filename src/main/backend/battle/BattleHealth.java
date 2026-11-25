package backend.battle;

import backend.model.Boss;
import backend.model.BossManager;
import backend.model.Category;
import backend.model.MainCharacter;

public class BattleHealth {
    private final MainCharacter mainCharacter;
    private final Boss boss;

    public BattleHealth(String characterName, Category category, int bossNumber) {
        this(characterName, 1, category, bossNumber);
    }

    public BattleHealth(String characterName, int level, Category category, int bossNumber) {
        mainCharacter = new MainCharacter(characterName, level);
        boss = new BossManager().getBoss(category, bossNumber);
    }

    public int damageBoss(int amount) {
        return boss.takeDamage(amount);
    }

    public int damageMainCharacter(int amount) {
        return mainCharacter.takeDamage(amount);
    }

    public int healBoss(int amount) {
        return boss.heal(amount);
    }

    public int healMainCharacter(int amount) {
        return mainCharacter.heal(amount);
    }

    public boolean isFinalBoss(Category category) {
        return boss.isFinalBoss() && boss.getCategory() == category;
    }

    public int getMainCharacterHP() {
        return mainCharacter.getCurrentHP();
    }

    public int getBossHP() {
        return boss.getCurrentHP();
    }

    public String getBossName() {
        return boss.getName();
    }

    public boolean isBossDefeated() {
        return !boss.isAlive();
    }

    public boolean isBattleOver() {
        return isBossDefeated() || !mainCharacter.isAlive();
    }
}
