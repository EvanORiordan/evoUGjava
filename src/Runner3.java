import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;

/**
 * Program for running multiple instances of an experiment program. Supports SADG8.
 */
public class Runner3 {
    public static void main(String[] args) {
        System.out.println("Executing "+Thread.currentThread().getStackTrace()[1].getClassName()+"."
                +Thread.currentThread().getStackTrace()[1].getMethodName()+"()...");

        // initialise variables in preparation for experimentation
        int runs=5000;
        Player.setPrize(1.0);
        Player.setLoners_payoff(Player.getPrize() * 0.6);
        Player.setNeighbourhoodType("VN");
        Player.getDf().setRoundingMode(RoundingMode.UP);
        SADG8.rows=30;
        SADG8.columns=30;
        SADG8.N = SADG8.rows * SADG8.columns;
        SADG8.max_gens=10000;
        SADG8.initial_num_abstainers = SADG8.N / 5;

        System.out.println("Runs="+runs
                + ", gens="+SADG8.max_gens
                + ", l="+Player.getLoners_payoff()
                + ", neighbourhood="+Player.getNeighbourhoodType()
                + ", pop size="+SADG8.N
                + ", init abstainers="+SADG8.initial_num_abstainers
        +": ");

        // stats representing experiment results
        double mean_avg_p = 0.0;
        double mean_highest_p = 0.0;
        double mean_lowest_p = 0.0;
        int mean_abstainers = 0;

        // run some experiments
        Instant start = Instant.now();
        for(int i=0;i<runs;i++){
            SADG8 run = new SADG8();
            run.start();
            mean_avg_p+=run.avg_p;
            mean_highest_p+=run.highest_p;
            mean_lowest_p+=run.lowest_p;
            mean_abstainers+=run.abstainers;
        }
        Instant finish = Instant.now();
        long secondsElapsed = Duration.between(start, finish).toSeconds();
        long minutesElapsed = Duration.between(start, finish).toMinutes();

        // display overall stats
        mean_avg_p /= runs;
        mean_highest_p /= runs;
        mean_lowest_p /= runs;
        mean_abstainers /= runs;

        System.out.println("Mean avg p="+Player.getDf().format(mean_avg_p)
                + ", mean abstainers="+mean_abstainers
                + ", mean highest p="+Player.getDf().format(mean_highest_p)
                + ", mean lowest p="+Player.getDf().format(mean_lowest_p)
        );

        System.out.print("Time elapsed: "+minutesElapsed+" minutes, "+secondsElapsed%60+" seconds");
    }
}
