import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Spatial abstinence evo DG program. Update rule specifies that the greater a neighbour's average score is in
 * comparison to the evolving player, the greater the likelihood of the player imitating that neighbour. The
 * converse is true for neighbour's with lesser average scores than the evolving player. They also include
 * themselves as a player that could be copied hence the player may choose themselves as parent and therefore
 * remain unchanged.
 *
 * This SADG version does not keep track of the highest or lowest value of p in the pop.
 */
public class SADG9 extends Thread{
    static int rows;
    static int columns;
    static int N;
    static int max_gens;
    static int initial_num_abstainers;
    ArrayList<ArrayList<Player>> grid = new ArrayList<>();
    double avg_p=0;
    int abstainers = 0;

    public void start(){
        // generate fixed number of unique random abstainer positions;
        // the Set collection used here helps ensure that the generated ints are unique.
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
                for(Player player: row){ // the player here is the evolving player

                    // the player throws their own imitation score into the mix by adding a 1.0 to the end of
                    // the array. when j is equal to the neighbourhood size, this indicates that the current
                    // imitation score in consideration is that of the evolving player therefore there are no
                    // further neighbours to consider as parents therefore no evolution occurs here.
                    ArrayList<Player> neighbourhood = player.getNeighbourhood();
                    double[] imitation_scores = new double[neighbourhood.size() + 1];
                    double total_imitation_score = 0;
                    double player_avg_score = player.getAverage_score();
                    for(int i=0;i<neighbourhood.size();i++){
                        imitation_scores[i] = Math.exp(neighbourhood.get(i).getAverage_score() - player_avg_score);
                        total_imitation_score += imitation_scores[i];
                    }
                    imitation_scores[neighbourhood.size()] = 1.0;
                    total_imitation_score += 1.0;
                    double imitation_score_tally = 0;
                    double random_double_to_beat = ThreadLocalRandom.current().nextDouble();
                    for(int j=0;j<imitation_scores.length;j++){
                        if(j != neighbourhood.size()){
                            imitation_score_tally += imitation_scores[j];
                            if(random_double_to_beat < imitation_score_tally / total_imitation_score) {
                                player.copyStrategy(neighbourhood.get(j));
                                break;
                            }
                        }
                    }
                }
            }

            // reset the players' scores, GPTG, old p value and old abstainer values.
            for(ArrayList<Player> row: grid){
                for(Player player: row){
                    player.setScore(0);
                    player.setGamesPlayedThisGen(0);
                    player.setOld_p(player.getP());
                    player.setOldAbstainer(player.getAbstainer());
                }
            }

            double current_abstainers = 0;
            for(ArrayList<Player> row: grid){
                for(Player player: row){
                    if(player.getAbstainer()){
                        current_abstainers++;
                    }
                }
            }
            System.out.println(current_abstainers);

            gen++;
        }

        // gets stats
        for(ArrayList<Player> row: grid){
            for(Player player: row){
                avg_p+=player.getP();
                if(player.getAbstainer()){
                    abstainers++;
                }
            }
        }
        avg_p /= N;
    }
}
