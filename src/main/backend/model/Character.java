package backend.model;

public class Character {

    private String name;
    private int maxHP;
    private int currentHP;

    public Character(String name, int maxHP, int currentHP){
        this.name = name;
        this.maxHP = maxHP;
        this.currentHP = currentHP;
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

    public void takeDamage(int damage){

        currentHP -= damage;

        if (currentHP < 0) {
            currentHP = 0;
        }


    }
}
