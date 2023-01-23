import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 *  Evo DG. For each gen, a player plays once. At end of gen, the highest scoring individual has
 *  their strategy copied by the lowest scoring individual. It is assumed that each player's score
 *  resets to zero after each gen.
 */
public class EvoDG1 {
    static final double prize = 10.0;
    static final int N = 100;
    static final int max_gens = 1000000;
    static final boolean reset1 = true;
    static final String results_csv="results.csv";
    static final String COMMA_DELIMITER = ",";
    static final String NEW_LINE_SEPARATOR = "\n";
    static final String tester = "14";

    public static void main(String[] args) throws IOException {
        System.out.println("Executing "+Thread.currentThread().getStackTrace()[1].getMethodName()+"()...");
        Player.setPrize(prize);
        ArrayList<Player> pop = new ArrayList<>();
        ArrayList<Integer> player_id_list = new ArrayList<>();
        for(int i=0;i<N;i++){
            pop.add(new Player(ThreadLocalRandom.current().nextDouble(),0.0,false));
            player_id_list.add(pop.get(i).getId());
        }
        int generation = 0;
        while(generation!=max_gens){
            resetScores(pop, reset1);
            // this arraylist helps keep track of who has played already in a given gen
            ArrayList<Integer> player_id_list_copy = (ArrayList) player_id_list.clone();
            for(int i=0;i<N/2;i++){
                int random_int1 = ThreadLocalRandom.current().nextInt(player_id_list_copy.size()); // find a random player
                int random_int2 = ThreadLocalRandom.current().nextInt(player_id_list_copy.size());
                while(random_int1 == random_int2){
                    random_int2 = ThreadLocalRandom.current().nextInt(player_id_list_copy.size()); // find a different random player
                }
                Player player1 = pop.get(player_id_list_copy.get(random_int1));
                Player player2 = pop.get(player_id_list_copy.get(random_int2));
                boolean random_bool = ThreadLocalRandom.current().nextBoolean(); // role assignment
                if(random_bool){
                    player1.playDG(player2); // if true, player1 is dictator
                } else{
                    player2.playDG(player1); // if false, player2 is dictator
                }
                player_id_list_copy.remove(Integer.valueOf(player1.getId())); // player1 won't play again this gen
                player_id_list_copy.remove(Integer.valueOf(player2.getId())); // player2 won't play again this gen
            }
            double highest_score = 0.0;
            double lowest_score = prize;
            Player parent = new Player();  // player whose strategy will be imitated.
            Player offspring = new Player();  // player who will imitate the parent's strategy
            for(Player player: pop){
                if(player.getScore() > highest_score){
                    parent = player;
                    highest_score = parent.getScore();
                } else if(player.getScore() < lowest_score){
                    offspring = player;
                    lowest_score = offspring.getScore();
                }
            }
            offspring.setP(parent.getP()); // offspring copies parent's strategy
            generation++;
        }
        displayStats(pop);
        writeToCSV(results_csv, pop);
    }

    public static void resetScores(ArrayList<Player> player_list, boolean reset){
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
                + "Tester: "+tester+COMMA_DELIMITER
                + "Gens: "+max_gens+COMMA_DELIMITER
                + "N: "+N+COMMA_DELIMITER
                + "Reset scores start round: "+reset1+NEW_LINE_SEPARATOR);
        for(Player player: player_list){
            fw.append(player.getId() + COMMA_DELIMITER
                    + player.getP() + NEW_LINE_SEPARATOR);
        }
        fw.close();
        System.out.println("Completed writing to "+filename);
    }
    public static void displayStats(ArrayList<Player> player_list){
        double avg_p=0;
        double highest_p = 0.0;
        double lowest_p = 1.0;
        for(Player player: player_list){
            if(player.getP() > highest_p){
                highest_p = player.getP();
            } else if(player.getP() < lowest_p){
                lowest_p = player.getP();
            }
            avg_p+=player.getP();
        }
        avg_p /= N;
        System.out.println("Average value of p: "+avg_p);
        System.out.println("Highest value of p: "+highest_p);
        System.out.println("Lowest value of p: "+lowest_p);
    }
}
