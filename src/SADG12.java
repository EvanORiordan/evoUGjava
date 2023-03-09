import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Excerpt from meeting notes from 28/2/23:
 * Do a roulette wheel selection experiment (use SADG9 probably) with a cluster of abstainers at
 * gen 0 (hard code it in). See if abstaining or non-abstaining spreads! For instance, insert the
 * 3x3 cluster and observe the nearby 5x5 of players and observe the results.
 *
 * This program is the same as SADG9 except for, before the first gen begins, a cluster of
 * abstainer is hard-coded, i.e. inserted into the pop. displayScreenshotOfPop() has also
 * been tailored to produce a screenshot of the cluster and its neighbours.
 */
public class SADG12 extends Thread {
    static int rows;
    static int columns;
    static int N;
    static int max_gens;
    static int initial_num_abstainers;
    ArrayList<ArrayList<Player>> grid = new ArrayList<>();
    double avg_p=0;
    int abstainers = 0;
    static DecimalFormat df = Player.getDf();

    public void start(){
        Set<Integer> abstainer_positions = new HashSet<>();
        while(abstainer_positions.size() != initial_num_abstainers){
            abstainer_positions.add(ThreadLocalRandom.current().nextInt(0, N));
        }
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

        // insert 3x3 abstainer cluster into the population
        int inner_cluster_height = 3;
        int inner_cluster_width = 3;
        for(int i=1;i<inner_cluster_height+1;i++){
            for(int j=1;j<inner_cluster_width+1;j++) {
                grid.get(i).get(j).setAbstainer(true);
            }
        }

        for(int i=0;i<rows;i++){
            for(int j=0;j<columns;j++){
                grid.get(i).get(j).findNeighbours2D(grid, i, j);
            }
        }
        int gen = 0;
        while(gen != max_gens) {
            for(ArrayList<Player> row: grid){
                for(Player player: row){
                    player.playSpatialAbstinenceUG();
                }
            }

            // print cluster info into the console
//            displayScreenshotOfPop();

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
        System.out.println("Executing "+Thread.currentThread().getStackTrace()[1].getClassName()+"."
                +Thread.currentThread().getStackTrace()[1].getMethodName()+"()...");
        System.out.println("Timestamp:" + java.time.Clock.systemUTC().instant());
        int runs=5000;
        Player.setPrize(1.0);
        Player.setLoners_payoff(Player.getPrize() * 0.2);
        Player.setNeighbourhoodType("VN");
        df.setRoundingMode(RoundingMode.UP);
        rows = 30;
        columns = 30;
        N = rows * columns;
        max_gens = 10000;
        initial_num_abstainers = N / 5;
        System.out.println("Runs="+runs
                + ", gens="+max_gens
                + ", l="+Player.getLoners_payoff()
                + ", neighbourhood="+Player.getNeighbourhoodType()
                + ", pop size="+N
                + ", init abstainers="+initial_num_abstainers
                +": ");
        double avg_p = 0;
        double[] avg_p_values = new double[runs];
        double sd_avg_p = 0;
        int avg_abstainers = 0;
        int[] avg_abstainers_values = new int[runs];
        double sd_avg_abstainers = 0;
        int[] avg_abstainers_value_occurrences = new int[N+1];
        Instant start = Instant.now();
        for(int i=0;i<runs;i++){
            SADG12 run = new SADG12();
            run.start();
            avg_p += run.avg_p;
            avg_p_values[i] = run.avg_p;
            avg_abstainers += run.abstainers;
            avg_abstainers_values[i] = run.abstainers;
            avg_abstainers_value_occurrences[run.abstainers]++;
        }
        Instant finish = Instant.now();
        avg_p /= runs;
        avg_abstainers /= runs;
        for(int i=0;i<runs;i++){
            sd_avg_p += Math.pow(avg_p_values[i] - avg_p, 2);
            sd_avg_abstainers += Math.pow(avg_abstainers_values[i] - avg_abstainers, 2);
        }
        sd_avg_p = Math.pow(sd_avg_p / runs, 0.5);
        sd_avg_abstainers = Math.pow(sd_avg_abstainers / runs, 0.5);
        System.out.println("avg p="+df.format(avg_p)
                + ", avg p SD="+df.format(sd_avg_p)
                + ", avg abstainers="+avg_abstainers
                + ", avg abstainers SD="+df.format(sd_avg_abstainers)
        );
        for(int i=0;i<avg_abstainers_value_occurrences.length;i++){
            if(avg_abstainers_value_occurrences[i] > 0){
                System.out.println("How many times was there "+i+" abstainers left: "
                        +avg_abstainers_value_occurrences[i]);
            }
        }
        long secondsElapsed = Duration.between(start, finish).toSeconds();
        long minutesElapsed = Duration.between(start, finish).toMinutes();
        System.out.print("Time elapsed: "+minutesElapsed+" minutes, "+secondsElapsed%60+" seconds");
        System.out.println("Timestamp:" + java.time.Clock.systemUTC().instant());
    }

    /**
     * this method aids in the observation of the activity of a cluster of the population and the
     * adjacent nodes that interact with it. this means there's really an inner cluster of initially
     * homogenous nodes and an outer cluster consisting of the inner cluster and its neighbours.
     * e.g. with 3x3 inner, the minimum outer is 5x5.
     */
    public void displayScreenshotOfPop(){
        System.out.println("p    S    A");
        int outer_cluster_height = 5;
        int outer_cluster_width = 5;
        for(int i=0;i<outer_cluster_height;i++){
            for(int j=0;j<outer_cluster_width;j++){
                Player player = grid.get(i).get(j);
                double p = player.getP();
                double avg_score = player.getAverage_score();
                String abstainer = "NA"; // indicates "non-abstainer"
                if(player.getAbstainer()){
                    abstainer = "A ";
                }
                System.out.print(df.format(p) + " " + df.format(avg_score) + " " + abstainer + "      ");
            }
            System.out.println();
        }
        System.out.println();
    }

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
}
