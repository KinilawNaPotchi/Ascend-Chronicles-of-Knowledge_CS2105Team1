package backend.model;
import java.util.ArrayList;
import java.util.List;

public class BossManager {

    private final ArrayList<Boss> bosses;

    public BossManager (){
        bosses = new ArrayList<>();

        Boss math1 = new Boss(
                "Axiom", 100, Category.MATHEMATICS, false
        );
        Boss math2 = new Boss(
                "Continuum", 100, Category.MATHEMATICS, false
        );
        Boss math3 = new Boss(
                "Infinitra", 100, Category.MATHEMATICS, true
        );
        Boss science1 = new Boss(
                "VOLT-EX", 100, Category.SCIENCE, false
        );
        Boss science2 = new Boss(
                "CatalyX", 100, Category.SCIENCE, false
        );
        Boss science3 = new Boss(
                "GENEXIS", 100, Category.SCIENCE, true
        );
        Boss programming1 = new Boss(
                "RepetiAI", 100, Category.PROGRAMMING, false
        );
        Boss programming2 = new Boss(
                "HaluGen", 100, Category.PROGRAMMING, false
        );
        Boss programming3 = new Boss(
                "ChatWise", 100, Category.PROGRAMMING, true
        );

        bosses.add(math1);
        bosses.add(math2);
        bosses.add(math3);
        bosses.add(science1);
        bosses.add(science2);
        bosses.add(science3);
        bosses.add(programming1);
        bosses.add(programming2);
        bosses.add(programming3);
    }

    public List<Boss> getBosses(Category category) {
        return bosses.stream().filter(boss -> boss.getCategory() == category).toList();
    }

    public Boss getBoss(Category category, int bossNumber) {
        List<Boss> categoryBosses = getBosses(category);
        if (bossNumber < 1 || bossNumber > categoryBosses.size()) {
            throw new IllegalArgumentException("Invalid boss number for " + category + ": " + bossNumber);
        }
        return categoryBosses.get(bossNumber - 1);
    }
}
