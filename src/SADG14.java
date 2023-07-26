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
// * Asymmetric-playing SADG program based on SADG13.java.
// *
// * Edge decay is not featured.
// *
// */
//
//public class SADG14 extends Thread {
//    static int rows;
//    static int columns;
//    static int N;
//    static int max_gens;
//    static int initial_num_abstainers;
//    ArrayList<ArrayList<Player>> grid = new ArrayList<>();
//    ArrayList<Player> pop = new ArrayList<>();
//    double avg_p;
//    int abstainers;
//    static DecimalFormat df = Player.getDf();
//    int gen = 0;
//
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
//                Player player = new Player(ThreadLocalRandom.current().nextDouble(), 0.0, abstainer);
//                row.add(player);
//                pop.add(player);
//                pop_position++;
//            }
//            grid.add(row);
//        }
//        for(int i=0;i<rows;i++){
//            for(int j=0;j<columns;j++){
//                grid.get(i).get(j).findNeighbours2D(grid, i, j);
//            }
//        }
//        while(gen != max_gens) {
//
//
//            // roulette wheel selection of a dictator to play asymmetric spatial abstinence DG (ASADG)
//            int x = 0;
//            while(x < N){
//                Player selected_dictator;
//                while(true){
//                    // select a random player from the pop
//                    int random_int = ThreadLocalRandom.current().nextInt(0,N);
//                    selected_dictator = pop.get(random_int);
//
//                    // if this player has not been a dictator yet this generation, assign them the role.
//                    if(!selected_dictator.getSelected()){
//                        selected_dictator.setSelected(true);
//                        break;
//                    }
//                }
//
//                // dictator plays the game with neighbours.
//                selected_dictator.playAsymmSpatialAbstinenceUG();
//            }
//
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
//        int runs=5000;
//        Player.setPrize(1.0);
//        Player.setLoners_payoff(Player.getPrize() * 0.2);
//        Player.setNeighbourhoodType("VN");
//        rows = 30;
//        columns = 30;
//        N = rows * columns;
//        max_gens = 10000;
//        initial_num_abstainers = 450;
//
//
//        System.out.println("Runs="+runs
//                + ", gens="+max_gens
//                + ", l="+Player.getLoners_payoff()
//                + ", neighbourhood="+Player.getNeighbourhoodType()
//                + ", N="+N
//                + ", init abstainers="+initial_num_abstainers
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
//            SADG13 run = new SADG13();
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
//
//    public void reset(){
//        for(ArrayList<Player> row: grid){
//            for(Player player: row){
//                player.setScore(0);
//                player.setGamesPlayedThisGen(0);
//                player.setOld_p(player.getP());
//                player.setOldAbstainer(player.getAbstainer());
//
//                // reset the players left for the given player to play.
//                player.setPlayers_left_to_play_this_gen(player.getNeighbourhood());
//            }
//        }
//    }
//}
