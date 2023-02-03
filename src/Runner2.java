import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;

/**
 * Program for running multiple instances of an experiment program. Supports SpatialAbstinenceDG7.
 */
public class Runner2 {
    public static void main(String[] args) {
        System.out.println("Executing "+Thread.currentThread().getStackTrace()[1].getClassName()+"."
                +Thread.currentThread().getStackTrace()[1].getMethodName()+"()...");

        // initialise variables in preparation for experimentation
        int runs=5000;
        Player.setPrize(1.0);
        Player.setLoners_payoff(Player.getPrize() * 0.2);
        Player.setNeighbourhoodType("vonNeumann4");
        Player.getDf().setRoundingMode(RoundingMode.UP);
        SpatialAbstinenceDG7.rows=60;
        SpatialAbstinenceDG7.columns=60;
        SpatialAbstinenceDG7.max_gens=1000;
        SpatialAbstinenceDG7.initial_num_abstainers=2450;
        double mean_avg_p = 0.0;
        double mean_highest_p = 0.0;
        double mean_lowest_p = 0.0;
        int mean_abstainers = 0;

        // run some experiments
        Instant start = Instant.now();
        for(int i=0;i<runs;i++){
            SpatialAbstinenceDG7 run = new SpatialAbstinenceDG7();
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
        System.out.println("Time elapsed: "+minutesElapsed+" minutes, "+secondsElapsed%60+" seconds"+
                "\nMean average value of p:\t"+Player.getDf().format(mean_avg_p)+
                "\nMean number of abstainers:\t"+mean_abstainers+
                "\nMean highest value of p:\t"+Player.getDf().format(mean_highest_p)+
                "\nMean lowest value of p:\t\t"+Player.getDf().format(mean_lowest_p));
    }
}
