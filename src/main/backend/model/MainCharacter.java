package backend.model;

public class MainCharacter extends Character {

    private int level;

    public MainCharacter(String name, int level) {
        super(name, 100, 100);
        this.level = level;
    }

    public void displayDetails(){
        System.out.println("MAIN CHARACTER");
        System.out.println("Name" + getName());
    }

}
