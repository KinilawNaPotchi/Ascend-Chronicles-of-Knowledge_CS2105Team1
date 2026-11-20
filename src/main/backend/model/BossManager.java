package backend.model;
import java.util.ArrayList;
import java.util.List;

public class BossManager {

    ArrayList<Boss> bosses;

    public BossManager (){
        bosses = new ArrayList<>();

        Boss math1 = new Boss(
                "Integral", 100, 100, Category.MATHEMATICS, false
        );
        Boss math2 = new Boss(
                "Summation", 100, 100, Category.MATHEMATICS, false
        );
        Boss math3 = new Boss(
                "Infinty", 100, 100, Category.MATHEMATICS, true
        );

        bosses.add(math1);
        bosses.add(math2);
        bosses.add(math3);
    }

}
