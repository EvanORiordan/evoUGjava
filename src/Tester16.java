import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * UG on a 2D grid. For each gen, a player plays with their von Neumann neighbourhood. Scores and
 * number of games played in a gen is reset after each gen. NEXT TIME: debug to see if the players
 * are playing their neighbourhood properly, in particular the assignPosition2D() and
 * findNeighbours2D() methods.
 */

public class Tester16 {
    static double prize = 10.0;
    static int rows = 10;
    static int columns = 10;
    static int N = rows * columns;
    static String tester = "16";
    static int max_gens = 10000;
    static String neighbourhood = "vonNeumann4";

    public static void main(String[] args) {
        System.out.println("Executing Tester"+tester+"."+Thread.currentThread().getStackTrace()[1].getMethodName()+"()...");

        // construct grid of players
        ArrayList<ArrayList<Player>> grid = new ArrayList<>();
        for(int i = 0; i < rows; i++){
            ArrayList<Player> row = new ArrayList<>();
            for(int j = 0; j < columns; j++){
                row.add(new Player(
                        ThreadLocalRandom.current().nextDouble(),
                        ThreadLocalRandom.current().nextDouble(),
                        neighbourhood));
            }
            grid.add(row);
        }

        // assign neighbours
        for(int i=0;i<rows;i++){
            for(int j=0;j<columns;j++){
                grid.get(i).get(j).assignPosition2D(i,j);
                grid.get(i).get(j).findNeighbours2D(grid);
            }
        }

        // play UG
        int gen = 0;
        while(gen != max_gens){
            for(int i = 0; i < rows; i++){
                for(int j = 0; j < columns; j++){
                    grid.get(i).get(j).playSpatialUG(prize);
                }
            }
            gen++;
            reset(grid);
        }

        displayStats(grid);
    }

    public static void reset(ArrayList<ArrayList<Player>> grid){
        for(ArrayList<Player> row: grid){
            for(Player player: row){
                player.setScore(0);
                player.setGamesPlayedThisGen(0);
            }
        }
    }

    public static void displayStats(ArrayList<ArrayList<Player>> grid){
        int p_geq_q_tally=0;
        double avg_p=0;
        double avg_q=0;
        double highest_p = 0.0;
        double lowest_p = 1.0;
        double highest_q = 0.0;
        double lowest_q = 1.0;
        for(ArrayList<Player> row: grid){
            for(Player player: row){
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
