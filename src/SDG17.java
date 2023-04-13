import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 *
 */
public class SDG17 extends Thread{
    static int rows;
    static int columns;
    static int N;
    static int max_gens;
    ArrayList<ArrayList<Player>> grid = new ArrayList<>();
    double avg_p;
    static DecimalFormat df = Player.getDf();
    int gen = 0;


    public void start(){

        // init pop
        for(int i=0;i<rows;i++){
            ArrayList<Player> row = new ArrayList<>();
            for(int j=0;j<columns;j++){
                row.add(new Player(ThreadLocalRandom.current().nextDouble(), 0.0, false));
            }
            grid.add(row);
        }


        // initialise space
        for(int i=0;i<rows;i++){
            for(int j=0;j<columns;j++){
                grid.get(i).get(j).findNeighbours2D(grid, i, j);

                // initialise edge weights for edge decay.
                grid.get(i).get(j).initialiseEdgeWeights();
            }
        }


        // gens begin
        while(gen != max_gens) {


            // playing phase
            for(ArrayList<Player> row: grid){
                for(Player player: row){
                    //player.playEdgeDecaySpatialAbstinenceUG();

                    //player.playEdgeDecaySpatialUG();
                }
            }


            // edge decay phase
            for(ArrayList<Player> row: grid){
                for(Player player: row){
//                    player.edgeDecay2();

                    //player.edgeDecay3();
                }
            }


            // evolution
            for(ArrayList<Player> row: grid){
                for(Player player: row){
                    ArrayList<Player> neighbourhood = player.getNeighbourhood();
                    double[] imitation_scores = new double[neighbourhood.size() + 1];
                    double total_imitation_score = 0;
                    double player_avg_score = player.getAverage_score();
                    for(int i=0;i<neighbourhood.size();i++){
                        imitation_scores[i] = Math.exp(neighbourhood.get(i).getAverage_score() - player_avg_score);
                        total_imitation_score += imitation_scores[i];
                    }
                    total_imitation_score += 1.0;
                    double imitation_score_tally = 0;
                    double random_double_to_beat = ThreadLocalRandom.current().nextDouble();
                    for(int j=0;j<neighbourhood.size();j++){
                        imitation_score_tally += imitation_scores[j];
                        if(random_double_to_beat < imitation_score_tally / total_imitation_score) {
                            player.copyStrategy(neighbourhood.get(j));
                            break;
                        }
                    }
                }
            }
            reset();
            gen++;
        }
        getStats();
    }


    public static void main(String[] args) {

    }



    public void getStats(){
        avg_p = 0.0;
        for(ArrayList<Player> row: grid){
            for(Player player: row){
                avg_p+=player.getP();
            }
        }
        avg_p /= N;
    }


    public void reset(){
        for(ArrayList<Player> row: grid){
            for(Player player: row){
                player.setScore(0);
                player.setGamesPlayedThisGen(0);
                player.setOld_p(player.getP());
            }
        }
    }
}
