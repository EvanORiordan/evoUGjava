import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Evo UG with a random chance to abstain. After a responder determines that an offer is unsatisfactory, they
 * consider abstaining. The greater the difference between the offer and the responder's acceptance threshold,
 * the more likely the responder is to abstain. At the end of a generation, the lowest scoring individual in
 * the population copies the strategy of the highest scoring individual.
 *
 * Initial conclusions: pop is not particularly fair or rational
 */
public class AbstainEvoUG1 {
    static double prize = 1.0;
    static int N = 100;
    static int max_gens = 100000;
    static String results_csv="results.csv";
    static String COMMA_DELIMITER = ",";
    static String NEW_LINE_SEPARATOR = "\n";

    public static void main(String[] args) throws IOException {
        System.out.println("Executing "+Thread.currentThread().getStackTrace()[1].getClassName()+"."
                +Thread.currentThread().getStackTrace()[1].getMethodName()+"()...\n");

        // set up the simulation
        Player.setPrize(prize);
        Player.setLoners_payoff(prize * 0.1);
        ArrayList<Player> pop = new ArrayList<>();
        ArrayList<Integer> player_id_list = new ArrayList<>();
        for(int i=0;i<N;i++){
            pop.add(new Player(
                    ThreadLocalRandom.current().nextDouble(),
                    ThreadLocalRandom.current().nextDouble()));
            player_id_list.add(pop.get(i).getId());
        }

        int gen = 0;
        while(gen != max_gens){

            // play UG
            ArrayList<Integer> player_id_list_copy = (ArrayList) player_id_list.clone();
            for(int i=0;i<N/2;i++){
                int random_int1 = ThreadLocalRandom.current().nextInt(player_id_list_copy.size());
                int random_int2 = ThreadLocalRandom.current().nextInt(player_id_list_copy.size());
                while(random_int1 == random_int2){
                    random_int2 = ThreadLocalRandom.current().nextInt(player_id_list_copy.size());
                }
                Player player1 = pop.get(player_id_list_copy.get(random_int1));
                Player player2 = pop.get(player_id_list_copy.get(random_int2));
                boolean random_bool = ThreadLocalRandom.current().nextBoolean();
                if(random_bool){
                    player1.playAbstinenceUG2(player2);
                } else{
                    player2.playAbstinenceUG2(player1);
                }
                player_id_list_copy.remove(Integer.valueOf(player1.getId()));
                player_id_list_copy.remove(Integer.valueOf(player2.getId()));
            }

            // evolve
            double highest_score = 0.0;
            double lowest_score = prize;
            Player parent = new Player();
            Player offspring = new Player();
            for(Player player: pop){
                if(player.getScore() > highest_score){
                    parent = player;
                    highest_score = parent.getScore();
                } else if(player.getScore() < lowest_score){
                    offspring = player;
                    lowest_score = offspring.getScore();
                }
            }
            offspring.setStrategy(parent.getP(), parent.getQ());

            gen++;
        }
        displayStats(pop);
        writeToCSV(results_csv, pop);
    }

    public static void reset(ArrayList<Player> pop){
        for(Player player: pop){
            player.setScore(0);
        }
    }

    public static void displayStats(ArrayList<Player> pop){
        int p_geq_q_tally=0;
        double avg_p=0;
        double avg_q=0;
        double highest_p = 0.0;
        double lowest_p = 1.0;
        double highest_q = 0.0;
        double lowest_q = 1.0;
        for(Player player: pop){
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

    public static void writeToCSV(String filename, ArrayList<Player> pop) throws IOException {
        FileWriter fw = new FileWriter(filename, false);
        fw.append("Player ID"+COMMA_DELIMITER
                + "p"+COMMA_DELIMITER
                + "q"+COMMA_DELIMITER
                + "Program: "+Thread.currentThread().getStackTrace()[1].getClassName()+COMMA_DELIMITER
                + "Gens: "+max_gens+COMMA_DELIMITER
                + "N: "+N+COMMA_DELIMITER
                + NEW_LINE_SEPARATOR);
        for(Player player: pop){
            fw.append(player.getId()+COMMA_DELIMITER
                    + player.getP()+COMMA_DELIMITER
                    + player.getQ()+NEW_LINE_SEPARATOR);
        }
        fw.close();
        System.out.println("Completed writing to "+filename);
    }
}
