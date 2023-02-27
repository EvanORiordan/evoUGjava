//import java.math.RoundingMode;
//import java.text.DecimalFormat;
//import java.time.Duration;
//import java.time.Instant;
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.Set;
//import java.util.concurrent.ThreadLocalRandom;
//
///**
// * Placeholder class that might help me set up a program in the future that uses virtual threads
// * to conduct experiment runs concurrently.
// *
// * DOESNT WORK FOR NOW...
// */
//public class VT_SADG {
//    static int rows;
//    static int columns;
//    static int N;
//    static int max_gens;
//    static int initial_num_abstainers;
//    static DecimalFormat df = Player.getDf();
//
//
//
//    public class SADGThread implements Runnable {
//        ArrayList<ArrayList<Player>> grid = new ArrayList<>();
//        double avg_p = 0;
//        int abstainers = 0;
//
//        public void run(){
//            Set<Integer> abstainer_positions = new HashSet<>();
//            while(abstainer_positions.size() != initial_num_abstainers){
//                abstainer_positions.add(ThreadLocalRandom.current().nextInt(0, N));
//            }
//            int pop_position=0;
//            for(int i=0;i<rows;i++){
//                ArrayList<Player> row = new ArrayList<>();
//                for(int j=0;j<columns;j++){
//                    boolean abstainer = false; // by default, a player initialises as a non-abstainer
//                    for(Integer abstainer_position: abstainer_positions){
//                        if(pop_position == abstainer_position){ // if true, this player is an abstainer
//                            abstainer=true;
//                            break;
//                        }
//                    }
//                    row.add(new Player(ThreadLocalRandom.current().nextDouble(), 0.0, abstainer));
//                    pop_position++;
//                }
//                grid.add(row);
//            }
//            for(int i=0;i<rows;i++){
//                for(int j=0;j<columns;j++){
//                    grid.get(i).get(j).findNeighbours2D(grid, i, j);
//                }
//            }
//            int gen = 0;
//            while(gen != max_gens) {
//                for(ArrayList<Player> row: grid){
//                    for(Player player: row){
//                        player.playSpatialAbstinenceUG();
//                    }
//                }
//                for(ArrayList<Player> row: grid){
//                    for(Player player: row){
//                        ArrayList<Player> neighbourhood = player.getNeighbourhood();
//                        double[] imitation_scores = new double[neighbourhood.size() + 1];
//                        double total_imitation_score = 0;
//                        double player_avg_score = player.getAverage_score();
//                        for(int i=0;i<neighbourhood.size();i++){
//                            imitation_scores[i] = Math.exp(neighbourhood.get(i).getAverage_score() - player_avg_score);
//                            total_imitation_score += imitation_scores[i];
//                        }
//                        total_imitation_score += 1.0;
//                        double imitation_score_tally = 0;
//                        double random_double_to_beat = ThreadLocalRandom.current().nextDouble();
//                        for(int j=0;j<neighbourhood.size();j++){
//                            imitation_score_tally += imitation_scores[j];
//                            if(random_double_to_beat < imitation_score_tally / total_imitation_score) {
//                                player.copyStrategy(neighbourhood.get(j));
//                                break;
//                            }
//                        }
//                    }
//                }
//                reset();
//                gen++;
//            }
//            getStats();
//        }
//
//        public void getStats(){
//            for(ArrayList<Player> row: grid){
//                for(Player player: row){
//                    avg_p+=player.getP();
//                    if(player.getAbstainer()){
//                        abstainers++;
//                    }
//                }
//            }
//            avg_p /= N;
//        }
//
//        public void reset(){
//            for(ArrayList<Player> row: grid){
//                for(Player player: row){
//                    player.setScore(0);
//                    player.setGamesPlayedThisGen(0);
//                    player.setOld_p(player.getP());
//                    player.setOldAbstainer(player.getAbstainer());
//                }
//            }
//        }
//    }
//
//
//
//    // carry out experiments in this program's SADG environment
//    public static void main(String[] args) {
//        // display the name of the current program that is running
//        System.out.println("Executing "+Thread.currentThread().getStackTrace()[1].getClassName()+"."
//                +Thread.currentThread().getStackTrace()[1].getMethodName()+"()...");
//
//        // display initial timestamp
//        System.out.println("Timestamp:" + java.time.Clock.systemUTC().instant());
//
//        // variables that define the characteristics/settings of the experiment
//        int runs=5000;
//        Player.setPrize(1.0);
//        Player.setLoners_payoff(Player.getPrize() * 0.2);
//        Player.setNeighbourhoodType("VN");
//        df.setRoundingMode(RoundingMode.UP);
//        SADG9.rows = 30;
//        SADG9.columns = 30;
//        SADG9.N = SADG9.rows * SADG9.columns;
//        SADG9.max_gens = 10000;
//        SADG9.initial_num_abstainers = SADG9.N / 2;
//
//        // display settings
//        System.out.println("Runs="+runs
//                + ", gens="+SADG9.max_gens
//                + ", l="+Player.getLoners_payoff()
//                + ", neighbourhood="+Player.getNeighbourhoodType()
//                + ", pop size="+SADG9.N
//                + ", init abstainers="+SADG9.initial_num_abstainers
//                +": ");
//
//
//        // variables for storing experiment results
//
//        // technically, this is the mean average value of p across the experiment's runs.
//        // i.e. this variable is storing the mean of the average p values.
//        double avg_p = 0;
//        double[] avg_p_values = new double[runs]; // average p values obtained from the runs
//        double sd_avg_p = 0; // the standard deviation of the group of avg p values
//        int avg_abstainers = 0;
//        int[] avg_abstainers_values = new int[runs];
//        double sd_avg_abstainers = 0;
//
//        // run the experiment
//        Instant start = Instant.now(); // start the stopwatch
//        for(int i=0;i<runs;i++){
//            SADGThread run = new SADGThread(); // DOESNT WORK!!!
//            Thread virtual_thread = Thread.ofVirtual().unstarted(run);
//            virtual_thread.start();
//
//            avg_p += run.avg_p;
//            avg_p_values[i] = run.avg_p;
//            avg_abstainers += run.abstainers;
//            avg_abstainers_values[i] = run.abstainers;
//        }
//        Instant finish = Instant.now(); // stop the stopwatch
//
//        // determine experiment results
//        avg_p /= runs;
//        avg_abstainers /= runs;
//        for(int i=0;i<runs;i++){
//            sd_avg_p += Math.pow(avg_p_values[i] - avg_p, 2);
//            sd_avg_abstainers += Math.pow(avg_abstainers_values[i] - avg_abstainers, 2);
//        }
//        sd_avg_p = Math.pow(sd_avg_p / runs, 0.5);
//        sd_avg_abstainers = Math.pow(sd_avg_abstainers / runs, 0.5);
//
//        // display experiment results
//        System.out.println("avg p="+df.format(avg_p)
//                + ", avg p SD="+df.format(sd_avg_p)
//                + ", avg abstainers="+avg_abstainers
//                + ", avg abstainers SD="+df.format(sd_avg_abstainers)
//        );
//
//        // display the time taken by the experiment
//        long secondsElapsed = Duration.between(start, finish).toSeconds();
//        long minutesElapsed = Duration.between(start, finish).toMinutes();
//        System.out.print("Time elapsed: "+minutesElapsed+" minutes, "+secondsElapsed%60+" seconds");
//        System.out.println("Timestamp:" + java.time.Clock.systemUTC().instant());
//    }
//
//
//    // displays a screenshot of some cluster of the population
////    public void displayScreenshotOfPop(){
////        System.out.println("p    S    A?");
////        int cluster_height = 3;
////        int cluster_width = 3;
////        for(int i=0;i<cluster_height;i++){
////            for(int j=0;j<cluster_width;j++){
////                Player player = grid.get(i).get(j);
////                double p = player.getP();
////                double avg_score = player.getAverage_score();
////                String abstainer = "NA"; // indicates "non-abstainer"
////                if(player.getAbstainer()){
////                    abstainer = "A ";
////                }
////                System.out.print(df.format(p) + " " + df.format(avg_score) + " " + abstainer + "      ");
////            }
////            System.out.println();
////        }
////        System.out.println();
////    }
//
//
//
//}
//
