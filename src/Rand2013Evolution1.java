import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * My first attempt at reproducing the algorithm based on details read from paper
 * by Rand et al., 2013 [rand2013evolution].
 * <p>Pop size is denoted by {@code N}.</p>
 * <p>Each player {@code i} plays the UG once with each of the N-1 other players each generation.</p>
 * <p>Player i's average payoff after a given generation is denoted by pi_i.</p>
 * <p>To determine which player to select as parent,
 * each player has an effective average payoff defined as exp(w*pi_i).</p>
 * <p>The intensity of selection is denoted by {@code w}. As w -> infinity, only highest payoff players are imitated.
 * As w -> 0, all strategies can be selected to be imitated.</p>
 * <p>Select one player each generation with respect to w. Then, one other random player from the population must
 * either imitate the selected player or mutate their strategy to random values in the range [0,1]. Mutation rate
 * {@code u} determines how often the latter occurs.</p>
 */

public class Rand2013Evolution1 {
    static final double prize = 1.0;
    static final int N = 100;
    static final double u = Math.pow(10, -1.25);
    static final double w = Math.pow(10, -1.5);
    static final int max_num_gens = 100000;
    static final boolean reset1 = true;
    static final String results_csv="results.csv";
    static final String COMMA_DELIMITER = ",";
    static final String NEW_LINE_SEPARATOR = "\n";
    static final String tester = "11";

    public static void main(String[] args) throws IOException {
        System.out.println("Executing "+Thread.currentThread().getStackTrace()[1].getMethodName()+"()...");

        Player.setPrize(prize);
        ArrayList<Player> pop = new ArrayList<>();
        for(int i=0;i<N;i++){
            pop.add(new Player(
                    ThreadLocalRandom.current().nextDouble(),
                    ThreadLocalRandom.current().nextDouble(),
                    false));
        }
        int gen = 0;
        while(gen != max_num_gens){
            resetScores(pop, reset1);
            for(int i=0;i<N;i++){
                Player player1 = pop.get(i);
                for(int j = i+1; j < N; j++){
                    Player player2 = pop.get(j);
                    boolean random_bool = ThreadLocalRandom.current().nextBoolean();
                    if(random_bool){
                        player1.playUG(player2);
                    } else{
                        player2.playUG(player1);
                    }
                }
                player1.setEAP_rand2013evolution(w);
            }

            // start selection: select parent based on effective average payoff
            double denominator = 0;
            for(Player player: pop){
                denominator+=player.getEAP_rand2013evolution();
            }
            double random_double1 = ThreadLocalRandom.current().nextDouble(0.1 * denominator);
            double numerator = 0;
            double lower_bound = 0;
            Player parent = new Player();
            for (Player player : pop) {
                numerator += player.getEAP_rand2013evolution();
                double higher_bound = numerator / denominator;
                if ((lower_bound <= random_double1) && (random_double1 < higher_bound)) {
                    parent = player;
                    break;
                }
                lower_bound = higher_bound;
            }
            // end selection

            // start mutation/imitation
            Player offspring = new Player();
            int random_int3 = ThreadLocalRandom.current().nextInt(pop.size());
            while(random_int3==parent.getId()){  // Do not let the selected player also be the mutator player.
                random_int3 = ThreadLocalRandom.current().nextInt(pop.size());
            }
            double random_double2 = ThreadLocalRandom.current().nextDouble();
            if(random_double2<u){ // mutate strategy (u specifies the percentage chance for mutation to occur)
                offspring.setStrategy(ThreadLocalRandom.current().nextDouble(),ThreadLocalRandom.current().nextDouble());
            } else{
                offspring.setStrategy(parent.getP(),parent.getQ()); // copy strategy of prior selected individual
            }
            // end mutation/imitation

            gen++;
        }
        writeToCSV(results_csv, pop);
        displayStats(pop);
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
                + "gens: "+max_num_gens+COMMA_DELIMITER
                + "N: "+N+COMMA_DELIMITER
                + "u: "+u+COMMA_DELIMITER
                + "w: "+w+COMMA_DELIMITER
                + "Reset scores start gen: "+reset1+COMMA_DELIMITER
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
        double highest_p = 0.0;
        double lowest_p = 1.0;
        double highest_q = 0.0;
        double lowest_q = 1.0;
        for(Player player: player_list){
            if(player.getP() > player.getQ()){
                p_geq_q_tally++;
            }
            if(player.getP() > highest_p){
                highest_p = player.getP();
            } else if(player.getP() < lowest_p){
                lowest_p = player.getP();
            }
            if(player.getQ() > highest_q){
                highest_q = player.getQ();
            } else if(player.getQ() < lowest_q){
                lowest_q = player.getQ();
            }
            avg_p+=player.getP();
            avg_q+=player.getQ();
        }
        avg_p /= N;
        avg_q /= N;
        System.out.println("Number of players out of "+N+" that have p>q: "+p_geq_q_tally);
        System.out.println("Average value of p: "+avg_p);
        System.out.println("Average value of q: "+avg_q);
        System.out.println("Highest value of p: "+highest_p);
        System.out.println("Lowest value of p: "+lowest_p);
        System.out.println("Highest value of q: "+highest_q);
        System.out.println("Lowest value of q: "+lowest_q);
    }
}
