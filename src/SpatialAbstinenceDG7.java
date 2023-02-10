import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Spatial abstinence evo DG program that is capable of assigning a fixed number of initial abstainers.
 * Some variables of this program are now static and are assigned values at runtime by Runner2.
 */
public class SpatialAbstinenceDG7 extends Thread{
    static int rows;
    static int columns;
    static int N;
    static int max_gens;
    static int initial_num_abstainers;
    ArrayList<ArrayList<Player>> grid = new ArrayList<>();
    double avg_p=0;
    double highest_p = 0.0;
    double lowest_p = 1.0;
    int abstainers = 0;

    public void start(){
        // generate unique random abstainer positions
        Set<Integer> abstainer_positions = new HashSet<>();
        while(abstainer_positions.size() != initial_num_abstainers){
            abstainer_positions.add(ThreadLocalRandom.current().nextInt(0, N));
        }

        // place players into the grid
        int pop_position=0;
        for(int i=0;i<rows;i++){
            ArrayList<Player> row = new ArrayList<>();
            for(int j=0;j<columns;j++){
                boolean abstainer = false; // by default, a player is a non-abstainer
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

        // find neighbours
        for(int i=0;i<rows;i++){
            for(int j=0;j<columns;j++){
                grid.get(i).get(j).findNeighbours2D(grid, i, j);
            }
        }

        // play spatial DG with abstinence
        int gen = 0;
        while(gen != max_gens) {
            for(ArrayList<Player> row: grid){
                for(Player player: row){
                    player.playSpatialAbstinenceUG();
                }
            }

            // evolution
            for(ArrayList<Player> row: grid){
                for(Player player: row){
                    Player parent = null;
                    double highest_avg_score_in_neighbourhood = player.getAverage_score();
                    for(Player neighbour: player.getNeighbourhood()){
                        if(neighbour.getAverage_score() > highest_avg_score_in_neighbourhood){
                            parent = neighbour;
                            highest_avg_score_in_neighbourhood = parent.getAverage_score();
                        }
                    }
                    if(parent != null){
                        player.copyStrategy(parent);
                    }
                }
            }
            gen++;
            reset();
        }
        getStats();
    }

    public void reset(){
        for(ArrayList<Player> row: grid){
            for(Player player: row){
                player.setScore(0);
                player.setGamesPlayedThisGen(0);
                player.setOld_p(player.getP());
                player.setOldAbstainer(player.getAbstainer());
            }
        }
    }

    public void getStats(){
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
    }
}
