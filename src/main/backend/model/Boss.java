package backend.model;

public class Boss extends Character{

    private Category category;
    private boolean finalBoss;

    public Boss (String name, int maxHP, int currentHP, Category category, boolean finalBoss){
        super(name, maxHP, currentHP);
        this.category = category;
        this.finalBoss = finalBoss;
    }

    public Category getCategory(){
        return category;
    }

    public boolean isFinalBoss(){
        return finalBoss;
    }
}
