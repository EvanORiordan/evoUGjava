import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Non-evo UG on a 1D line. For each gen, a player plays with their neighbourhood which consists of the player
 * before and after them in the line (think of the line as a circle). Scores and number of games played
 * each round of each player is reset to zero after each gen.
 */

public class Tester15 {
    static double prize = 10.0;
    static int N = 100;
//    static String tester = "15";
    static int max_gens = 10000;
    static String neighbourhood = "line2"; // type of neighbourhood and number of neighbours
    static String results_csv="results.csv";
    static String COMMA_DELIMITER = ",";
    static String NEW_LINE_SEPARATOR = "\n";

    public static void main(String[] args) throws IOException {
//        System.out.println("Executing Tester"+tester+"."+Thread.currentThread().getStackTrace()[1].getMethodName()+"()...");
        System.out.println("Executing "
                +Thread.currentThread().getStackTrace()[1].getClassName()
                +"."
                +Thread.currentThread().getStackTrace()[1].getMethodName()
                +"()...\n");

        // construct a population in the form of a line of players
        ArrayList<Player> line = new ArrayList<>();
        for(int i=0;i<N;i++){
            line.add(new Player(
                    ThreadLocalRandom.current().nextDouble(),
                    ThreadLocalRandom.current().nextDouble(),
                    neighbourhood));
        }

        // assign neighbours
        for(int i=0;i<N;i++){
            line.get(i).assignPosition1D(i);
            line.get(i).findNeighbours1D(line);
        }

        // play UG
        int gen = 0;
        while(gen != max_gens){
            for(int i=0;i<N;i++){
                line.get(i).playSpatialUG(prize);
            }
            gen++; // end of generation
            reset(line); // prepare players for next generation
        }

        displayStats(line);
        writeToCSV(results_csv, line);
    }

    public static void reset(ArrayList<Player> player_list){
        for(Player player: player_list){
            player.setScore(0);
            player.setGamesPlayedThisGen(0);
        }
    }

    // note that since this is a non-evo program, displaying stats is not very relevant due to the fact that
    // these players are not adjusting, i.e., evolving, their behaviour over time.
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

    public static void writeToCSV(String filename, ArrayList<Player> player_list) throws IOException {
        FileWriter fw = new FileWriter(filename, false);
        fw.append("Player ID"+COMMA_DELIMITER
                + "p"+COMMA_DELIMITER
                + "q"+COMMA_DELIMITER
                + "Program: "+Thread.currentThread().getStackTrace()[1].getClassName()+COMMA_DELIMITER
                + "Gens: "+max_gens+COMMA_DELIMITER
                + "N: "+N+COMMA_DELIMITER
                + NEW_LINE_SEPARATOR);
        for(Player player: player_list){
            fw.append(player.getId()+COMMA_DELIMITER
                    + player.getP()+COMMA_DELIMITER
                    + player.getQ()+NEW_LINE_SEPARATOR);
        }
        fw.close();
        System.out.println("Completed writing to "+filename);
    }
}
