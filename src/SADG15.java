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
// * 31/3/23
// *
// * This program copies SADG13.java but uses the edgeDecay2() edge decay mechanism.
// */
//public class SADG15 extends Thread {
//    static int rows;
//    static int columns;
//    static int N;
//    static int max_gens;
//    static int initial_num_abstainers;
//    ArrayList<ArrayList<Player>> grid = new ArrayList<>();
//    double avg_p;
//    int abstainers;
//    static DecimalFormat df = Player.getDf();
//    int gen = 0;
//
//    public void start(){
//        Set<Integer> abstainer_positions = new HashSet<>();
//        while(abstainer_positions.size() != initial_num_abstainers){
//            abstainer_positions.add(ThreadLocalRandom.current().nextInt(0, N));
//        }
//        int pop_position=0;
//        for(int i=0;i<rows;i++){
//            ArrayList<Player> row = new ArrayList<>();
//            for(int j=0;j<columns;j++){
//                boolean abstainer = false;
//                for(Integer abstainer_position: abstainer_positions){
//                    if(pop_position == abstainer_position){
//                        abstainer=true;
//                        break;
//                    }
//                }
//                row.add(new Player(ThreadLocalRandom.current().nextDouble(), 0.0, abstainer));
//                pop_position++;
//            }
//            grid.add(row);
//        }
//        for(int i=0;i<rows;i++){
//            for(int j=0;j<columns;j++){
//                grid.get(i).get(j).findNeighbours2D(grid, i, j);
//
//                // initialise edge weights for edge decay.
//                grid.get(i).get(j).initialiseEdgeWeights();
//            }
//        }
//        while(gen != max_gens) {
//
//            // each player plays the game (as dictator).
//            for(ArrayList<Player> row: grid){
//                for(Player player: row){
//                    player.playEdgeDecaySpatialAbstinenceUG();
//                }
//            }
//
//            // after playing, each player gets a chance to modify their edge weights.
//            for(ArrayList<Player> row: grid){
//                for(Player player: row){
//                    player.edgeDecay2();
//                }
//            }
//
////            displayScreenshotOfPop();
//
//            // evolution
//            for(ArrayList<Player> row: grid){
//                for(Player player: row){
//                    ArrayList<Player> neighbourhood = player.getNeighbourhood();
//                    double[] imitation_scores = new double[neighbourhood.size() + 1];
//                    double total_imitation_score = 0;
//                    double player_avg_score = player.getAverage_score();
//                    for(int i=0;i<neighbourhood.size();i++){
//                        imitation_scores[i] = Math.exp(neighbourhood.get(i).getAverage_score() - player_avg_score);
//                        total_imitation_score += imitation_scores[i];
//                    }
//                    total_imitation_score += 1.0;
//                    double imitation_score_tally = 0;
//                    double random_double_to_beat = ThreadLocalRandom.current().nextDouble();
//                    for(int j=0;j<neighbourhood.size();j++){
//                        imitation_score_tally += imitation_scores[j];
//                        if(random_double_to_beat < imitation_score_tally / total_imitation_score) {
//                            player.copyStrategy(neighbourhood.get(j));
//                            break;
//                        }
//                    }
//                }
//            }
//            reset();
//            gen++;
//        }
//        getStats();
//    }
//
//
//    public static void main(String[] args) {
//        System.out.println("Executing "+Thread.currentThread().getStackTrace()[1].getClassName()+"."
//                +Thread.currentThread().getStackTrace()[1].getMethodName()+"()...");
//        System.out.println("Timestamp:" + java.time.Clock.systemUTC().instant());
//        df.setRoundingMode(RoundingMode.UP);
//
//
//        // experiment parameters
//        int runs=10;
//        Player.setPrize(1.0);
//        Player.setLoners_payoff(Player.getPrize() * 0.2);
//        Player.setNeighbourhoodType("VN");
//        Player.setRate_of_change(0.1);
//        rows = 30;
//        columns = 30;
//        N = rows * columns;
//        max_gens = 10000;
//        initial_num_abstainers = N / 10;
//
//
//        System.out.println("Runs="+runs
//                + ", gens="+max_gens
//                + ", l="+Player.getLoners_payoff()
//                + ", neighbourhood="+Player.getNeighbourhoodType()
//                + ", N="+N
//                + ", init abstainers="+initial_num_abstainers
//                + ", ROC="+Player.getRate_of_change()
//                +": ");
//
//
//        double avg_p = 0;
//        double[] avg_p_values = new double[runs];
//        double sd_avg_p = 0;
//        int avg_abstainers = 0;
//        int[] avg_abstainers_values = new int[runs];
//        double sd_avg_abstainers = 0;
//        int[] avg_abstainers_value_occurrences = new int[N+1];
//
//
//        Instant start = Instant.now();
//        for(int i=0;i<runs;i++){
//            SADG15 run = new SADG15();
//            run.start();
//            avg_p += run.avg_p;
//            avg_p_values[i] = run.avg_p;
//            avg_abstainers += run.abstainers;
//            avg_abstainers_values[i] = run.abstainers;
//            avg_abstainers_value_occurrences[run.abstainers]++;
//        }
//        Instant finish = Instant.now();
//
//
//        avg_p /= runs;
//        avg_abstainers /= runs;
//        for(int i=0;i<runs;i++){
//            sd_avg_p += Math.pow(avg_p_values[i] - avg_p, 2);
//            sd_avg_abstainers += Math.pow(avg_abstainers_values[i] - avg_abstainers, 2);
//        }
//        sd_avg_p = Math.pow(sd_avg_p / runs, 0.5);
//        sd_avg_abstainers = Math.pow(sd_avg_abstainers / runs, 0.5);
//        System.out.println("avg p="+df.format(avg_p)
//                + ", avg p SD="+df.format(sd_avg_p)
//                + ", avg abstainers="+avg_abstainers
//                + ", avg abstainers SD="+df.format(sd_avg_abstainers)
//        );
//
//
//        // uncomment this if you want info on the number of runs with specific numbers of abstainers
//        // remaining to appear at the end of runtime. alternatively, I could simply refrain from
//        // adding these statistics to the experiment notebook.
//        for(int i=0;i<avg_abstainers_value_occurrences.length;i++){
//            if(avg_abstainers_value_occurrences[i] > 0){
//                System.out.println("How many times was there "+i+" abstainers left: "
//                        +avg_abstainers_value_occurrences[i]);
//            }
//        }
//
//
//        long secondsElapsed = Duration.between(start, finish).toSeconds();
//        long minutesElapsed = Duration.between(start, finish).toMinutes();
//        System.out.println("Time elapsed: "+minutesElapsed+" minutes, "+secondsElapsed%60+" seconds");
//        System.out.println("Timestamp:" + java.time.Clock.systemUTC().instant());
//    }
//
//
//    // method for recording statistics after an experiment has been conducted.
//    // in general, this should only get called after the final gen.
//    public void getStats(){
//        avg_p = 0.0;
//        abstainers = 0;
//        for(ArrayList<Player> row: grid){
//            for(Player player: row){
//                avg_p+=player.getP();
//                if(player.getAbstainer()){
//                    abstainers++;
//                }
//            }
//        }
//        avg_p /= N;
//
//        // uncomment to observe the average p and abstainers remaining during runtime.
////        System.out.println("avg p="+avg_p+"\tabstainers="+abstainers);
//    }
//
//    public void reset(){
//        for(ArrayList<Player> row: grid){
//            for(Player player: row){
//                player.setScore(0);
//                player.setGamesPlayedThisGen(0);
//                player.setOld_p(player.getP());
//                player.setOldAbstainer(player.getAbstainer());
//            }
//        }
//    }
//
//
//    public void displayScreenshotOfPop(){
//        System.out.println("p    s    A");
//        int outer_cluster_height = 5;
//        int outer_cluster_width = 5;
//        for(int i=0;i<outer_cluster_height;i++){
//            for(int j=0;j<outer_cluster_width;j++){
//                Player player = grid.get(i).get(j);
//                double p = player.getP();
//                double avg_score = player.getAverage_score();
//                String abstainer = "NA"; // indicates "non-abstainer"
//                if(player.getAbstainer()){
//                    abstainer = "A ";
//                }
//                System.out.print(df.format(p) + " " + df.format(avg_score) + " " + abstainer + "      ");
//            }
//            System.out.println();
//        }
//        System.out.println();
//    }
//}
