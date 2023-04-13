import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created on 31/3/23
 *
 * This program copies SADG15.java but has no evolution phase and ED.
 *
 * Uses Player.edgeDecay2() for ED.
 */
public class SADG16 extends Thread {

    static int rows;
    static int columns;
    static int N;
    static int max_gens;
    static int initial_num_abstainers;
    ArrayList<ArrayList<Player>> grid = new ArrayList<>();
    int gen = 0;


    public static void main(String[] args) {

        // experiment parameters
        Player.setPrize(1.0);
        Player.setLoners_payoff(Player.getPrize() * 0.2);
        Player.setNeighbourhoodType("VN");
        Player.setRate_of_change(0.1);
        rows = 5;
        columns = 5;
        N = rows * columns;
        max_gens = 10000;
        initial_num_abstainers = N / 5;


        // run experiment
        SADG16 run = new SADG16();
        run.start();
    }


    // Thread.start();
    public void start(){

        // init pop
        Set<Integer> abstainer_positions = new HashSet<>();
        while(abstainer_positions.size() != initial_num_abstainers){
            abstainer_positions.add(ThreadLocalRandom.current().nextInt(0, N));
        }
        int pop_position=0;
        for(int i=0;i<rows;i++){
            ArrayList<Player> row = new ArrayList<>();
            for(int j=0;j<columns;j++){
                boolean abstainer = false;
                for(Integer abstainer_position: abstainer_positions){
                    if(pop_position == abstainer_position){
                        abstainer=true;
                        break;
                    }
                }
                row.add(new Player(ThreadLocalRandom.current().nextDouble(), 0.0, abstainer));
                pop_position++;
            }
            grid.add(row);
        }


        // init space
        for(int i=0;i<rows;i++){
            for(int j=0;j<columns;j++){
                grid.get(i).get(j).findNeighbours2D(grid, i, j);

                // initialise edge weights for edge decay.
                grid.get(i).get(j).initialiseEdgeWeights();
            }
        }


        while(gen != max_gens) {

            // each player plays the game (as dictator).
            for(ArrayList<Player> row: grid){
                for(Player player: row){
                    player.playEdgeDecaySpatialAbstinenceUG();
                }
            }

            // after playing, each player gets a chance to modify their edge weights.
            for(ArrayList<Player> row: grid){
                for(Player player: row){
                    player.edgeDecay2();
                }
            }

            // end-of-gen procedure
            reset();
            gen++;
        }
    }


    public void reset(){
        for(ArrayList<Player> row: grid){
            for(Player player: row){
                player.setScore(0);
                player.setGamesPlayedThisGen(0);
                player.setOld_p(player.getP());
                player.setOldAbstainer(player.getAbstainer());

                // if alone, eliminate player...?
//                player.aloneCheck();
            }
        }
    }
}
