import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Spatial abstinence evo DG program. Update rule runs a probability check to see if a player will evolve.
 * If the check is passed, the rule specifies that the greater a neighbour's average score is in comparison
 * to the evolving player, the greater the likelihood of the player imitating that neighbour. The converse
 * is true for neighbour's with lesser average scores than the player.
 *
 * When making a new program based on this one in the future, feel free to clean up the code to
 * reduce the number of lines of code.
 */
public class SADG8 extends Thread{
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
                for(Player player: row){ // the "player" denoted here is the current player under inspection

                    // to introduce more stochasticity, there is a 50% chance that a player chooses
                    // not to copy anyone. if an additional mechanism such as this was not present,
                    // each player in the pop would always copy a neighbour each generation!
                    double chance_to_not_imitate = ThreadLocalRandom.current().nextDouble();
                    if(chance_to_not_imitate < 0.5){
                        Player parent = null;

                        // each neighbour calculates their "imitation score".
                        // the imitation scores are pitted against each other to determine the parent.
                        // each neighbour has a chance to be the parent.
                        // the greater a neighbour's avg score was in comparison to the current player,
                        // that neighbour is exponentially more likely to be selected as the parent.
                        double[] imitation_scores = new double[player.getNeighbourhood().size()];
                        double total_imitation_score = 0;
                        for(int i=0;i<player.getNeighbourhood().size();i++){
                            Player neighbour = player.getNeighbourhood().get(i);
                            double player_avg_score = player.getAverage_score();
                            double neighbours_avg_score = neighbour.getAverage_score();
                            double difference = neighbours_avg_score - player_avg_score;
                            double imitation_score = Math.exp(difference);
                            imitation_scores[i] = imitation_score;
                            total_imitation_score += imitation_score;
                        }

                        // "roulette wheel selection" approach, where stronger neighbours are weighted
                        // greater than weaker ones. imitation scores are assigned intervals with
                        // respect to the total imitation score. once the imitation score tally eclipses
                        // the random double to beat, the ith neighbour is assigned as the parent.
                        double imitation_score_tally = 0;
                        double random_double_to_beat = ThreadLocalRandom.current().nextDouble();
                        for(int i=0;i<imitation_scores.length;i++){
                            imitation_score_tally += imitation_scores[i];
                            double chance = imitation_score_tally / total_imitation_score;
                            if(random_double_to_beat < chance) {
                                parent = player.getNeighbourhood().get(i);
                                player.copyStrategy(parent);
                                break;
                            }
                        }
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
