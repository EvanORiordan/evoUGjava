//import java.math.RoundingMode;
//import java.time.Duration;
//import java.time.Instant;
//
///**
// * Program for running multiple instances of an experiment program. Supports SADG8.
// */
//public class Runner3 {
//    public static void main(String[] args) {
//        System.out.println("Executing "+Thread.currentThread().getStackTrace()[1].getClassName()+"."
//                +Thread.currentThread().getStackTrace()[1].getMethodName()+"()...");
//
//        System.out.println("Timestamp:" + java.time.Clock.systemUTC().instant());
//
//        // initialise variables in preparation for experimentation
//        int runs=10;
//        Player.setPrize(1.0);
//        Player.setLoners_payoff(Player.getPrize() * 0.2);
//        Player.setNeighbourhoodType("M");
//        Player.getDf().setRoundingMode(RoundingMode.UP);
//        SADG8.rows=30;
//        SADG8.columns=30;
//        SADG8.N = SADG8.rows * SADG8.columns;
//        SADG8.max_gens=500;
//        SADG8.initial_num_abstainers = SADG8.N / 2;
//
//        System.out.println("Runs="+runs
//                + ", gens="+SADG8.max_gens
//                + ", l="+Player.getLoners_payoff()
//                + ", neighbourhood="+Player.getNeighbourhoodType()
//                + ", pop size="+SADG8.N
//                + ", init abstainers="+SADG8.initial_num_abstainers
//        +": ");
//
//        // stats representing experiment results
//        double mean_avg_p = 0;
//        double[] mean_avg_p_values = new double[runs];
//        double standard_deviation_mean_avg_p = 0;
//        double mean_highest_p = 0;
//        double mean_lowest_p = 0;
//        int mean_abstainers = 0;
//        int[] mean_abstainers_values = new int[runs];
//        double standard_deviation_mean_abstainers = 0;
//
//        // run some experiments
//        System.out.println("Results of each run: ");
//        Instant start = Instant.now(); // start the stopwatch
//        for(int i=0;i<runs;i++){
//            SADG8 run = new SADG8();
//            run.start();
//            mean_avg_p += run.avg_p;
//            System.out.println("Avg p = " + run.avg_p);
//            mean_avg_p_values[i] = run.avg_p;
//            mean_highest_p += run.highest_p;
//            mean_lowest_p += run.lowest_p;
//            mean_abstainers += run.abstainers;
//            System.out.println("Abstainers remaining = " + run.abstainers + "\n");
//            mean_abstainers_values[i] = run.abstainers;
//        }
//        Instant finish = Instant.now(); // stop the stopwatch
//        long secondsElapsed = Duration.between(start, finish).toSeconds();
//        long minutesElapsed = Duration.between(start, finish).toMinutes();
//
//        // display stats
//        mean_avg_p /= runs;
//        mean_highest_p /= runs;
//        mean_lowest_p /= runs;
//        mean_abstainers /= runs;
//        for(int i=0;i<runs;i++){
//            standard_deviation_mean_avg_p += Math.pow(mean_avg_p_values[i] - mean_avg_p, 2);
//            standard_deviation_mean_abstainers += Math.pow(mean_abstainers_values[i] - mean_abstainers, 2);
//        }
//        standard_deviation_mean_avg_p = Math.pow(standard_deviation_mean_avg_p / runs, 0.5);
//        standard_deviation_mean_abstainers = Math.pow(standard_deviation_mean_abstainers / runs, 0.5);
//
//        System.out.println("Final statistics:\n"
//                + "Mean avg p="+Player.getDf().format(mean_avg_p)
//                + ", standard deviation mean avg p="+Player.getDf().format(standard_deviation_mean_avg_p)
//                + ", mean abstainers="+mean_abstainers
//                + ", standard deviation mean abstainers="+Player.getDf().format(standard_deviation_mean_abstainers)
////                + ", mean highest p="+Player.getDf().format(mean_highest_p)
////                + ", mean lowest p="+Player.getDf().format(mean_lowest_p)
//        );
//
//        System.out.print("Time elapsed: "+minutesElapsed+" minutes, "+secondsElapsed%60+" seconds");
//
//        System.out.println("Timestamp:" + java.time.Clock.systemUTC().instant());
//
//    }
//}
