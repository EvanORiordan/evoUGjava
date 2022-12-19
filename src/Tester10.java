import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/*
One-shot UG.
Population initialises with random strategy players.
Evolution: loser's losing strategy parameter copies winner's winning strategy parameter subject to noise (epsilon).
    E.g. if proposer x loses to responder y, then q_x is set to a value within the range [q_y - e, q_y + e].
Each player's score may be reset to zero at the start of each round.
 */
public class Tester10 {
    static Random random = new Random();
    static ArrayList<Player> population = new ArrayList<>();
    static final String COMMA_DELIMITER = ",";
    static final String NEW_LINE_SEPARATOR = "\n";

    static String tester = "10";
    static double epsilon = 0.1;
    static String starting_players="starting_players.csv";
    static String results_csv="results.csv";
    static double prize = 10.0;  // the prize being split in an interaction
    static int max_num_rounds = 10000;  // specifies the number of rounds the game will be played for
    static int pop_size = 100;  // even integer specifies how many players are in the population; also denoted by N
    static boolean reset1 = true; // indicates whether player scores are reset at a certain point of the program
    static boolean displayRoundMessages = false; // indicates whether some messages are printed each round

    public static void main(String[] args) throws IOException {
        for(int i=0; i<pop_size; i++){  // add players with random strategies to pop
            Player player = new Player(random.nextDouble(), random.nextDouble());
            population.add(player);
        }
        displayInfo(population, true);
        writeToCSV(starting_players, population);
        int round = 0;
        while(round != max_num_rounds){
            if(displayRoundMessages){
                System.out.println("\n=========== Round "+(round+1)+" ===========");
            }
            resetScores(population, reset1);
            ArrayList<Player> players_this_round = (ArrayList) population.clone();  // temp copy of pop
            while(players_this_round.size() != 0){  // while there are players left unassigned
                int rand_int = random.nextInt(players_this_round.size());  // assign a player as proposer
                int rand_int2 = random.nextInt(players_this_round.size());  // assign a player as responder
                while(rand_int == rand_int2){  // make sure that it is two different players
                    rand_int2 = random.nextInt(players_this_round.size());
                }
                Player proposer = players_this_round.get(rand_int);
                Player responder = players_this_round.get(rand_int2);
                proposer.playUG(responder, prize);  // playUG UG

                // start evolution
                if(proposer.getScore()>responder.getScore()){
                    responder.setP(ThreadLocalRandom.current().nextDouble(proposer.getP()-epsilon, proposer.getP()+epsilon));
                } else if(responder.getScore()>proposer.getScore()){
                    proposer.setQ(ThreadLocalRandom.current().nextDouble(responder.getQ()-epsilon, responder.getQ()+epsilon));
                }
                // end evolution

                players_this_round.remove(proposer);  // remove them from the temp list
                players_this_round.remove(responder);
            }
            round++; // move on to the next round of interactions
        }
        displayInfo(population, false);
        writeToCSV(results_csv, population);
    }
    public static void displayInfo(ArrayList<Player> player_list, boolean showSettings){
        if(showSettings){
            System.out.println("\nSettings:" +
                    "\n\tPrize: "+prize+
                    "\n\tMax number of rounds: "+max_num_rounds+
                    "\n\tPopulation size: "+pop_size);
        }
        System.out.println("\nPlayers:");
        for(Player p: player_list){
            System.out.println("\t"+p);
        }
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
        fw.append("DG.Player ID"+COMMA_DELIMITER
                + "p"+COMMA_DELIMITER
                + "q"+COMMA_DELIMITER
                + "Tester: "+tester+COMMA_DELIMITER
                + "Rounds: "+max_num_rounds+COMMA_DELIMITER
                + "Pop size: "+pop_size+COMMA_DELIMITER
                + "Reset scores start round: "+reset1+COMMA_DELIMITER
                + "Epsilon: "+epsilon
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
}
