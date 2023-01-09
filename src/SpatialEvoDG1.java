import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class SpatialDG1 {
    static double prize = 10.0;
    static int rows = 10;
    static int columns = 10;
    static int N = rows * columns;
    static int max_gens = 10000;
    static String neighbourhood = "vonNeumann4";
    static String results_csv="results.csv";
    static String COMMA_DELIMITER = ",";
    static String NEW_LINE_SEPARATOR = "\n";

    public static void main(String[] args) throws IOException {
        System.out.println("Executing "+Thread.currentThread().getStackTrace()[1].getClassName()+"."
                +Thread.currentThread().getStackTrace()[1].getMethodName()+"()...\n");

        // construct grid of players
        ArrayList<ArrayList<Player>> grid = new ArrayList<>();
        for(int i = 0; i < rows; i++){
            ArrayList<Player> row = new ArrayList<>();
            for(int j = 0; j < columns; j++){
                row.add(new Player(ThreadLocalRandom.current().nextDouble(), neighbourhood));
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

        // play DG
        int gen = 0;
        while(gen != max_gens){
            for(int i = 0; i < rows; i++){
                for(int j = 0; j < columns; j++){
                    grid.get(i).get(j).playSpatialUG(prize);
                }
            }

            // NEXT TIME: implement evolution!

            gen++;
            reset(grid);
        }

        writeToCSV(results_csv, grid);
    }

    public static void reset(ArrayList<ArrayList<Player>> grid){
        for(ArrayList<Player> row: grid){
            for(Player player: row){
                player.setScore(0);
                player.setGamesPlayedThisGen(0);
            }
        }
    }

    public static void writeToCSV(String filename, ArrayList<ArrayList<Player>> grid) throws IOException {
        FileWriter fw = new FileWriter(filename, false);
        fw.append("Player ID"+COMMA_DELIMITER
                + "p"+COMMA_DELIMITER
                + "Program: "+Thread.currentThread().getStackTrace()[1].getClassName()+COMMA_DELIMITER
                + "Gens: "+max_gens+COMMA_DELIMITER
                + "N: "+N+COMMA_DELIMITER
                + NEW_LINE_SEPARATOR);
        for(ArrayList<Player> row: grid){
            for(Player player: row){
                fw.append(player.getId()+COMMA_DELIMITER
                        + player.getP()+NEW_LINE_SEPARATOR);
            }
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
