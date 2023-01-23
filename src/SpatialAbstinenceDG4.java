import java.io.FileWriter;
import java.io.IOException;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 *  Uses updated Player functionality.
 *  Spatial evo DG with abstinence. A percentage of the initial population are abstainers. An abstainer
 *  always abstains from playing, regardless of the partner. At the end of each generation, each player
 *  measures their score against their neighbours. Each player copies the strategy that their highest
 *  scoring neighbour had at the beginning of the generation (i.e., the update rule is synchronous). A
 *  player's strategy is both their p value and their abstainer status.
 */
public class SpatialAbstinenceDG4 {
    static double abstainer_prob = 0.98; // the probability that a player initialises as an abstainer
    static double prize = 1.0;
    static int rows = 50;
    static int columns = 50;
    static int N = rows * columns;
    static int max_gens = 100000;
    static String neighbourhood = "moore8";
    static String results_csv="results.csv";
    static String grid_diagram_csv = "grid_diagram.csv";
    static String COMMA_DELIMITER = ",";
    static String NEW_LINE_SEPARATOR = "\n";

    public static void main(String[] args) throws IOException {
        System.out.println("Executing "+Thread.currentThread().getStackTrace()[1].getClassName()+"."
                +Thread.currentThread().getStackTrace()[1].getMethodName()+"()...\n");
        ArrayList<ArrayList<Player>> grid = new ArrayList<>();
        Player.setPrize(prize);
        Player.setLoners_payoff(prize * 0.1);
        Player.setNeighbourhoodType(neighbourhood);
        Player.getDf().setRoundingMode(RoundingMode.UP);
        for(int i = 0; i < rows; i++){
            ArrayList<Player> row = new ArrayList<>();
            for(int j = 0; j < columns; j++){
                boolean abstainer = false;
                double random_double = ThreadLocalRandom.current().nextDouble();
                if(random_double < abstainer_prob){
                    abstainer=true;
                }
                row.add(new Player(ThreadLocalRandom.current().nextDouble(), 0.0, abstainer));
            }
            grid.add(row);
        }
        for(int i=0;i<rows;i++){
            for(int j=0;j<columns;j++){
                grid.get(i).get(j).findNeighbours2D(grid, i, j);
            }
        }
        int gen = 0;
        while(gen != max_gens) {
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < columns; j++) {
                    grid.get(i).get(j).playSpatialAbstinenceUG();
                }
            }
            for(int i=0;i<rows;i++){
                for(int j=0;j<columns;j++){
                    Player player = grid.get(i).get(j);
                    Player parent = null;
                    double highest_score_in_neighbourhood = player.getScore();
                    for(Player neighbour: player.getNeighbourhood()){
                        if(neighbour.getScore() > highest_score_in_neighbourhood){
                            parent = neighbour;
                            highest_score_in_neighbourhood = parent.getScore();
                        }
                    }
                    if(parent != null){
                        player.copyStrategy(parent);
                    }
                }
            }
            gen++;
            reset(grid);
        }
        displayStats(grid);
        writeResults(results_csv, grid);
        writeGridDiagram(grid_diagram_csv, grid);
    }

    public static void reset(ArrayList<ArrayList<Player>> grid){
        for(ArrayList<Player> row: grid){
            for(Player player: row){
                player.setScore(0);
                player.setGamesPlayedThisGen(0);
                player.setOld_p(player.getP());
            }
        }
    }

    public static void writeResults(String filename, ArrayList<ArrayList<Player>> grid) throws IOException {
        FileWriter fw = new FileWriter(filename, false);
        fw.append("Player ID"+COMMA_DELIMITER
                + "p"+COMMA_DELIMITER
                + "Program: "+Thread.currentThread().getStackTrace()[1].getClassName()+COMMA_DELIMITER
                + "Gens: "+max_gens+COMMA_DELIMITER
                + "N: "+N
                + NEW_LINE_SEPARATOR);
        for(ArrayList<Player> row: grid){
            for(Player player: row){
                fw.append(player.getId()+COMMA_DELIMITER+player.getP()+NEW_LINE_SEPARATOR);
            }
        }
        fw.close();
        System.out.println("Completed writing to "+filename);
    }

    public static void displayStats(ArrayList<ArrayList<Player>> grid){
        double avg_p=0;
        double highest_p = 0.0;
        double lowest_p = 1.0;
        int abstainers = 0;
        for(ArrayList<Player> row: grid){
            for(Player player: row){
                if(player.getP() > highest_p){
                    highest_p = player.getP();
                } else if(player.getP() < lowest_p){
                    lowest_p = player.getP();
                }
                avg_p+=player.getP();
                if(player.getAbstainer()){
                    abstainers++;
                }
            }
        }
        avg_p /= N;
        System.out.println("Average value of p:\t"+avg_p);
        System.out.println("Highest value of p:\t"+highest_p);
        System.out.println("Lowest value of p:\t"+lowest_p);
        System.out.println("Number of abstainers: "+abstainers);
    }

    public static void writeGridDiagram(String filename, ArrayList<ArrayList<Player>> grid) throws IOException {
        FileWriter fw = new FileWriter(filename, false);
        for(ArrayList<Player> row: grid){
            for(Player player: row){
                if(player.getAbstainer()){
                    fw.append("(A)"+COMMA_DELIMITER);
                } else {
//                    fw.append("("+Player.getDf().format(player.getP())+")"+COMMA_DELIMITER);
                    fw.append("("+player.getP()+")"+COMMA_DELIMITER);
                }
            }
            fw.append(NEW_LINE_SEPARATOR);
        }
        fw.close();
        System.out.println("Completed writing to "+filename);
    }
}
