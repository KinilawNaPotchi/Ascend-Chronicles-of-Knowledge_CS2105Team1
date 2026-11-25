package backend.model;

public class Character {

    private String name;
    private int maxHP;
    private int currentHP;

    public Character(String name, int currentHP){
        this.name = name;
        this.maxHP = 100;
        this.currentHP = Math.max(0, Math.min(maxHP, currentHP));
    }

    public String getName(){
        return name;
    }

    public int getMaxHP(){
        return maxHP;
    }

    public int getCurrentHP(){
        return currentHP;
    }

    public boolean isAlive(){
        return currentHP > 0;
    }

    public int takeDamage(int damage) {
        int lost = Math.min(currentHP, Math.max(0, damage));
        currentHP -= lost;
        return lost;
    }

    public int heal(int amount) {
        int recovered = Math.min(maxHP - currentHP, Math.max(0, amount));
        currentHP += recovered;
        return recovered;
    }
}
