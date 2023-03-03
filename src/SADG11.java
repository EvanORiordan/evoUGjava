import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Excerpt from meeting from 28/2/23:
 * Do an experiment with a ``copy best neighbour'' update rule where all players have p < 0.3. This is
 * the same update rule type as we were using in earlier SADG program versions a few weeks ago. This
 * will just be a sanity check for us to see that rational non-abstaining should spread with such
 * constraints.
 *
 * This SADG program similar to / based on SADG9.java but with a "copy best neighbour" update rule
 * where for all players, p < 0.3.
 */
public class SADG11 extends Thread {

    // declare attributes
    static int rows;
    static int columns;
    static int N;
    static int max_gens;
    static int initial_num_abstainers;
    ArrayList<ArrayList<Player>> grid = new ArrayList<>(); // non-static attribute values may vary per instance
    double avg_p=0;
    int abstainers = 0;
    static DecimalFormat df = Player.getDf();

    // inherited from Thread.start()
    public void start(){
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

                // for each player in the pop, p < 0.3
                row.add(new Player(ThreadLocalRandom.current().nextDouble(0.3), 0.0, abstainer));

                pop_position++;
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
            for(ArrayList<Player> row: grid){
                for(Player player: row){
                    player.playSpatialAbstinenceUG();
                }
            }
            for(ArrayList<Player> row: grid){

                // for each player x in the pop, for each neighbour y in x's neighbourhood whose average
                // score is greater than x's, x copies the strategy of the neighbour with the greater
                // average score.
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
            reset();
            gen++;
        }
        getStats();
    }

    public static void main(String[] args) {
        System.out.println("Executing "+Thread.currentThread().getStackTrace()[1].getClassName()+"."
                +Thread.currentThread().getStackTrace()[1].getMethodName()+"()...");
        System.out.println("Timestamp:" + java.time.Clock.systemUTC().instant());
        int runs=10000;
        Player.setPrize(1.0);
        Player.setLoners_payoff(Player.getPrize() * 0.2);
        Player.setNeighbourhoodType("VN");
        df.setRoundingMode(RoundingMode.UP);
        rows = 30;
        columns = 30;
        N = rows * columns;
        max_gens = 10000;
        initial_num_abstainers = N / 10;
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
        Instant start = Instant.now();
        for(int i=0;i<runs;i++){
            SADG11 run = new SADG11();
            run.start();
            avg_p += run.avg_p;
            avg_p_values[i] = run.avg_p;
            avg_abstainers += run.abstainers;
            avg_abstainers_values[i] = run.abstainers;
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
        long secondsElapsed = Duration.between(start, finish).toSeconds();
        long minutesElapsed = Duration.between(start, finish).toMinutes();
        System.out.print("Time elapsed: "+minutesElapsed+" minutes, "+secondsElapsed%60+" seconds");
        System.out.println("Timestamp:" + java.time.Clock.systemUTC().instant());
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
