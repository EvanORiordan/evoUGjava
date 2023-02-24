import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
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
 * This SADG version replaces its complementary runner class (Runner4.java) with SADG9.main().
 * This SADG version reintroduces reset() and getStats().
 */
public class SADG9 extends Thread{
    static int rows; // all instances of a class share the same value of a static attribute
    static int columns;
    static int N;
    static int max_gens;
    static int initial_num_abstainers;
    ArrayList<ArrayList<Player>> grid = new ArrayList<>(); // non-static attribute values may vary per instance
    double avg_p=0;
    int abstainers = 0;
    static DecimalFormat df = Player.getDf();

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
                boolean abstainer = false; // by default, a player initialises as a non-abstainer
                for(Integer abstainer_position: abstainer_positions){
                    if(pop_position == abstainer_position){ // if true, this player is an abstainer
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

//            displayScreenshotOfPop();

            // evolution
            for(ArrayList<Player> row: grid){
                for(Player player: row){ // the player here is the evolving player

                    // by 1.0 being added to the imitation score total, the player itself is effectively marked as a
                    // potential parent. if the random double is too big for the other neighbours to be selected then
                    // the player is effectively the parent therefore no change actually occurs.
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

            // end of generation.
            reset();
            gen++;
        }

        getStats();
    }

    // run experiments in this program's SADG environment
    public static void main(String[] args) {
        // display the name of the current program that is running
        System.out.println("Executing "+Thread.currentThread().getStackTrace()[1].getClassName()+"."
                +Thread.currentThread().getStackTrace()[1].getMethodName()+"()...");

        // display initial timestamp
        System.out.println("Timestamp:" + java.time.Clock.systemUTC().instant());

        // variables that define the characteristics/settings of the experiment
        int runs=5000;
        Player.setPrize(1.0);
        Player.setLoners_payoff(Player.getPrize() * 0.2);
        Player.setNeighbourhoodType("VN");
        df.setRoundingMode(RoundingMode.UP);
        SADG9.rows = 30;
        SADG9.columns = 30;
        SADG9.N = SADG9.rows * SADG9.columns;
        SADG9.max_gens = 10000;
        SADG9.initial_num_abstainers = SADG9.N / 2;

        // display settings
        System.out.println("Runs="+runs
                + ", gens="+SADG9.max_gens
                + ", l="+Player.getLoners_payoff()
                + ", neighbourhood="+Player.getNeighbourhoodType()
                + ", pop size="+SADG9.N
                + ", init abstainers="+SADG9.initial_num_abstainers
                +": ");

        // variables for storing experiment results
        double avg_p = 0; // technically, this is the mean average value of p across the experiment's runs
        double[] avg_p_values = new double[runs]; // average p values obtained from the runs
        double sd_avg_p = 0; // the standard deviation of the group of avg p values
        int avg_abstainers = 0;
        int[] avg_abstainers_values = new int[runs];
        double sd_avg_abstainers = 0;

        // run the experiment
        Instant start = Instant.now(); // start the stopwatch
        for(int i=0;i<runs;i++){
            SADG9 run = new SADG9();
            run.start();
            avg_p += run.avg_p;
            avg_p_values[i] = run.avg_p;
            avg_abstainers += run.abstainers;
            avg_abstainers_values[i] = run.abstainers;
        }
        Instant finish = Instant.now(); // stop the stopwatch

        // determine experiment results
        avg_p /= runs;
        avg_abstainers /= runs;
        for(int i=0;i<runs;i++){
            sd_avg_p += Math.pow(avg_p_values[i] - avg_p, 2);
            sd_avg_abstainers += Math.pow(avg_abstainers_values[i] - avg_abstainers, 2);
        }
        sd_avg_p = Math.pow(sd_avg_p / runs, 0.5);
        sd_avg_abstainers = Math.pow(sd_avg_abstainers / runs, 0.5);

        // display experiment results
        System.out.println("avg p="+df.format(avg_p)
                + ", standard deviation of avg p="+df.format(sd_avg_p)
                + ", avg abstainers="+avg_abstainers
                + ", standard deviation of avg abstainers="+df.format(sd_avg_abstainers)
        );

        // display the time taken by the experiment
        long secondsElapsed = Duration.between(start, finish).toSeconds();
        long minutesElapsed = Duration.between(start, finish).toMinutes();
        System.out.print("Time elapsed: "+minutesElapsed+" minutes, "+secondsElapsed%60+" seconds");
        System.out.println("Timestamp:" + java.time.Clock.systemUTC().instant());
    }


    // gets the average p value and the number of abstainers left.
    public void getStats(){
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

    // reset the players' scores, GPTG, old p value and old abstainer values.
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

    // displays a screenshot of some cluster of the population
    public void displayScreenshotOfPop(){

        // obtain a 3x3 cluster of the pop
        ArrayList<Player> cluster = new ArrayList<>();
        cluster.add(grid.get(0).get(0));
        cluster.add(grid.get(0).get(1));
        cluster.add(grid.get(0).get(2));
        cluster.add(grid.get(1).get(0));
        cluster.add(grid.get(1).get(1)); // central node of cluster
        cluster.add(grid.get(1).get(2));
        cluster.add(grid.get(2).get(0));
        cluster.add(grid.get(2).get(1));
        cluster.add(grid.get(2).get(2));

        System.out.print(df.format(cluster.get(0).getP()) + cluster.get(0).getAbstainer() + "\t");
        System.out.print(df.format(cluster.get(1).getP()) + cluster.get(1).getAbstainer() + "\t");
        System.out.println(df.format(cluster.get(2).getP()) + cluster.get(2).getAbstainer());
        System.out.print(df.format(cluster.get(3).getP()) + cluster.get(3).getAbstainer() + "\t");
        System.out.print(df.format(cluster.get(4).getP()) + cluster.get(4).getAbstainer() + "\t");
        System.out.println(df.format(cluster.get(5).getP()) + cluster.get(5).getAbstainer());
        System.out.print(df.format(cluster.get(6).getP()) + cluster.get(6).getAbstainer() + "\t");
        System.out.print(df.format(cluster.get(7).getP()) + cluster.get(7).getAbstainer() + "\t");
        System.out.println(df.format(cluster.get(8).getP()) + cluster.get(8).getAbstainer()+"\n");
    }
}
