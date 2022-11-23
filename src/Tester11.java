import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Based on details read from paper by Rand et al., 2013 (rand2013evolution).
 * <p>Pop size is denoted by {@code N}.</p>
 * <p>Each player {@code i} plays the UG with each of the N-1 other players.</p>
 * <p>Player i's average payoff is denoted by pi_i.</p>
 * <p>To determine which player to select, each player has an effective average payoff defined as exp(w*pi_i).</p>
 * <p>The intensity of selection is denoted by {@code w}. As w -> infinity, only highest payoff players are imitated.
 * As w -> 0, all strategies can be selected to be imitated.</p>
 * <p>Select one player each generation with respect to w. Then, one other random player from the population must
 * either imitate the selected player or mutate their strategy to random values in the range [0,1]. Mutation rate
 * {@code u} determines how often the latter occurs.</p>
 */

public class Tester11 {
    static final double prize = 10.0;
    static final int N = 100;
    static final double u = Math.pow(10, -1.25);  // = 0.05623413251
//    static final double u = 0.01;
//    static final double w = Math.pow(10, -1.5);  // play around with more values of w
//    static final double w = Math.pow(10, -1);
//    static final double w = Math.pow(10, -0.5);
//    static final double w = Math.pow(10, 2);
//    static final double w = 1000.00;
    static final double w = Math.pow(10, 100);
    static final int max_num_rounds = 1000;
    static final boolean reset1 = true;
    static final boolean displayRoundMessages = false;
    static final String results_csv="results.csv";
    static final String COMMA_DELIMITER = ",";
    static final String NEW_LINE_SEPARATOR = "\n";
    static final String tester = "11";


    public static void main(String[] args) throws IOException {
        main1();
//        test1();
//        test2();
//        test3();
    }


    public static void main1() throws IOException {
        System.out.println("Executing main1()...");

        // start initialise population
        ArrayList<Player> pop = new ArrayList<>();
        ArrayList<Integer> player_id_list = new ArrayList<>();
        for(int i=0;i<N;i++){
            pop.add(new Player(ThreadLocalRandom.current().nextDouble(), ThreadLocalRandom.current().nextDouble()));
            player_id_list.add(pop.get(i).getId());
        }
        // end initialise population

        int round = 0;
        while(round != max_num_rounds){
            resetScores(pop, reset1);

            // start of UG playing this round
            ArrayList<Integer> player_id_list_copy = (ArrayList) player_id_list.clone();  // temp copy of pop
            for(int i=0;i<N/2;i++){
                int random_int1 = ThreadLocalRandom.current().nextInt(player_id_list_copy.size());
                Player player1 = pop.get(player_id_list_copy.get(random_int1));
                Player player2 = new Player();
                int keepgoing = 1;
                while(keepgoing==1){
                    int random_int2 = ThreadLocalRandom.current().nextInt(player_id_list_copy.size());
                    while(random_int1==random_int2){
                        random_int2 = ThreadLocalRandom.current().nextInt(player_id_list_copy.size());
                    }
                    for(Integer j : player1.getPlayers_ive_played_against()){
                        if(random_int2!=j){
                            player2 = pop.get(player_id_list_copy.get(random_int2));
                            keepgoing--;
                        }
                    }
                }
                boolean random_bool = ThreadLocalRandom.current().nextBoolean(); // determine roles of the two players
                if(random_bool){
                    player1.play(player2, prize, displayRoundMessages, w);
                } else{
                    player2.play(player1, prize, displayRoundMessages, w);
                }
                player_id_list_copy.remove(Integer.valueOf(player1.getId()));
                player_id_list_copy.remove(Integer.valueOf(player2.getId()));
            }
            // end of UG playing this round

            // start selection
            double denominator = 0; // select individual based on effective average payoff
            for(Player player: pop){
                denominator+=player.getEffective_average_payoff();
            }
            double random_double1 = ThreadLocalRandom.current().nextDouble(0.1 * denominator);
            double numerator = 0;
            double lower_bound = 0;
            double higher_bound;
            Player selected = new Player();
            for (Player player : pop) {
                numerator += player.getEffective_average_payoff();
                higher_bound = numerator / denominator;
                if ((lower_bound <= random_double1) && (random_double1 < higher_bound)) {
                    selected = player;
                    break;
                }
                lower_bound = higher_bound;
            }
            // end selection

            // start mutation/imitation
            Player mutator = new Player();
            int random_int3 = ThreadLocalRandom.current().nextInt(player_id_list.size());
            while(random_int3==selected.getId()){  // Do not let the selected player also be the mutator player.
                random_int3 = ThreadLocalRandom.current().nextInt(player_id_list.size());
            }
            double random_double2 = ThreadLocalRandom.current().nextDouble();
            if(random_double2<u){ // mutate strategy (u specifies the percentage chance for mutation to occur)
                mutator.setStrategy(ThreadLocalRandom.current().nextDouble(),ThreadLocalRandom.current().nextDouble());
            } else{
                mutator.setStrategy(selected.getP(),selected.getQ()); // copy strategy of prior selected individual
            }
            // end mutation/imitation

            round++;
        }
        writeToCSV(results_csv, pop);
        displayStats(pop);
    }

    public static void test1(){
        System.out.println("Executing "+Thread.currentThread().getStackTrace()[1].getMethodName()+"()...");
        System.out.println("Testing results of play() when displayMessages=true.");
        Player player1 = new Player(ThreadLocalRandom.current().nextDouble(),ThreadLocalRandom.current().nextDouble());
        Player player2 = new Player(ThreadLocalRandom.current().nextDouble(),ThreadLocalRandom.current().nextDouble());
        player1.play(
                player2,
                prize,
                true,
                w
        );
    }
    public static void test2(){
        System.out.println("Executing "+Thread.currentThread().getStackTrace()[1].getMethodName()+"()...");
        System.out.println("Testing results of play() when displayMessages=false.");
        Player player1 = new Player(ThreadLocalRandom.current().nextDouble(),ThreadLocalRandom.current().nextDouble());
        Player player2 = new Player(ThreadLocalRandom.current().nextDouble(),ThreadLocalRandom.current().nextDouble());
        player1.play(
                player2,
                prize,
                false,
                w
        );
        System.out.println(player1);
        System.out.println(player2);
    }
    public static void test3(){
        System.out.println("Executing "+Thread.currentThread().getStackTrace()[1].getMethodName()+"()...");
        System.out.println("Testing if the argument passed to displayMessages to play() affects player scores.");
        Player player1 = new Player(ThreadLocalRandom.current().nextDouble(),ThreadLocalRandom.current().nextDouble());
        Player player2 = new Player(ThreadLocalRandom.current().nextDouble(),ThreadLocalRandom.current().nextDouble());
        player1.play(
                player2,
                prize,
                true,
                w
        );
        ArrayList<Player> players = new ArrayList<>();
        players.add(player1);
        players.add(player2);
        resetScores(players, true);
        player1.play(
                player2,
                prize,
                false,
                w
        );
        System.out.println(player1);
        System.out.println(player2);
    }


    public static void resetScores(ArrayList<Player> player_list, boolean reset){ // set pi_i to 0
        if(reset){
            for(Player player: player_list){
                player.setScore(0);
            }
        }
    }
    public static void writeToCSV(String filename, ArrayList<Player> player_list) throws IOException {
        FileWriter fw = new FileWriter(filename, false);
        fw.append("Player ID"+COMMA_DELIMITER
                + "p"+COMMA_DELIMITER
                + "q"+COMMA_DELIMITER
                + "Tester: "+tester+COMMA_DELIMITER
                + "Rounds: "+max_num_rounds+COMMA_DELIMITER
                + "N: "+N+COMMA_DELIMITER
                + "u: "+u+COMMA_DELIMITER
                + "w: "+w+COMMA_DELIMITER
                + "Reset scores start round: "+reset1+COMMA_DELIMITER
                + NEW_LINE_SEPARATOR);
        for(Player player: player_list){
            fw.append(player.getId() +COMMA_DELIMITER
                    + player.getP() +COMMA_DELIMITER
                    + player.getQ()
                    + NEW_LINE_SEPARATOR);
        }
        fw.close();
        System.out.println("Completed writing to "+filename);
    }
    public static void displayStats(ArrayList<Player> player_list){
        int p_geq_q_tally=0;
        double avg_p=0;
        double avg_q=0;
        for(Player player: player_list){
            if(player.getP()>player.getQ()){
                p_geq_q_tally++;
            }
            avg_p+=player.getP();
            avg_q+=player.getQ();
        }
        avg_p /= N;
        avg_q /= N;
        System.out.println("Number of players out of "+N+" that have p>q: "+p_geq_q_tally);
        System.out.println("Average value of p: "+avg_p);
        System.out.println("Average value of q: "+avg_q);
    }
}
