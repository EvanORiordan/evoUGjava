import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * <p>Evo DG</p>
 * <p>NOTE: update this class! Its main() method does not work in its current state.</p>
 */
public class Tester14 {
    static final double prize = 10.0;
    static final int N = 8;
    static final int max_gens = 10000;
    static final boolean reset1 = true;
    static final String results_csv="results.csv";
    static final String COMMA_DELIMITER = ",";
    static final String NEW_LINE_SEPARATOR = "\n";
    static final String tester = "13";

//    public static void main(String[] args) throws IOException {
//        System.out.println("Executing "+Thread.currentThread().getStackTrace()[1].getMethodName()+"()...");
//        ArrayList<Player> pop = new ArrayList<>();
//        ArrayList<Integer> player_id_list = new ArrayList<>();
//        for(int i=0;i<N;i++){
//            pop.add(new Player(ThreadLocalRandom.current().nextDouble()));
//            player_id_list.add(pop.get(i).getId());
//        }
//        int generation = 0;
//        while(generation!=max_gens){
//            resetScores(pop, reset1);
//            ArrayList<Integer> player_id_list_copy = (ArrayList) player_id_list.clone();
//            for(int i=0;i<N/2;i++){
//                int random_int1 = ThreadLocalRandom.current().nextInt(player_id_list_copy.size());
//                Player player1 = pop.get(player_id_list_copy.get(random_int1));
//                Player player2 = new Player();
//                boolean found_partner = false;
//                while(!found_partner){
//                    int random_int2 = ThreadLocalRandom.current().nextInt(player_id_list_copy.size());
//                    while(random_int1 == random_int2){
//                        random_int2 = ThreadLocalRandom.current().nextInt(player_id_list_copy.size());
//                    }
//                    for(Integer j : player1.getDogTagList()){
//                        if(random_int2 != j){
//                            player2 = pop.get(player_id_list_copy.get(random_int2));
//                            found_partner = true;
//                        }
//                    }
//                }
//                boolean random_bool = ThreadLocalRandom.current().nextBoolean(); // determine roles of the two players
//                if(random_bool){
//                    player1.playDG(player2, prize);  // player1 is the dictator
//                } else{
//                    player2.playDG(player1, prize);  // player1 is the dictator
//                }
//                player_id_list_copy.remove(Integer.valueOf(player1.getId()));  // player1 is done playing for this gen
//                player_id_list_copy.remove(Integer.valueOf(player2.getId()));  // player2 is done playing for this gen
//            }
//            double highest_score = 0.0;
//            double lowest_score = prize;
//            Player parent = new Player();  // DG.Player whose strategy will be imitated.
//            Player offspring = new Player();  // DG.Player who will imitate the parent's strategy
//            for(Player player: pop){
//                if(player.getScore() > highest_score){
//                    parent = player;
//                    highest_score = parent.getScore();
//                } else if(player.getScore() < lowest_score){
//                    offspring = player;
//                    lowest_score = offspring.getScore();
//                }
//            }
//            offspring.setP(parent.getP());
//            generation++;
//        }
//        displayStats(pop);
//        writeToCSV(results_csv, pop);
//    }

    public static void resetScores(ArrayList<Player> player_list, boolean reset){
        if(reset){
            for(Player player: player_list){
                player.setScore(0);
            }
        }
    }
    public static void writeToCSV(String filename, ArrayList<Player> player_list) throws IOException {
        FileWriter fw = new FileWriter(filename, false);
        fw.append("DG.Player ID"+COMMA_DELIMITER
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
            avg_p += player.getP();
            if(player.getP() > highest_p){
                highest_p = player.getScore();
            } else if(player.getP() < lowest_p){
                lowest_p = player.getScore();
            }
        }
        avg_p /= N;
        System.out.println("Average value of p: "+avg_p);
        System.out.println("Highest value of p: "+highest_p);
        System.out.println("Lowest value of p: "+lowest_p);
    }
}
