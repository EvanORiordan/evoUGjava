import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Similar to DG17.java except ED occurs each gen and evo occurs every
 * evo_phase_rate gens.
 */
public class DG18 extends Thread{
    static int rows;
    static int columns;
    static int N;
    static int max_gens;

    // how often the evolution phase occurs.
    static int evo_phase_rate;

    ArrayList<ArrayList<Player>> grid = new ArrayList<>();
    double avg_p;
    static DecimalFormat df = Player.getDf();
    int gen = 0;



    public void start(){

        /**
         * Space.
         *
         * Initialise population in the form of a square grid. A player situated at the start
         * of a row is neighbours with the player situated at the end of the row. The similar
         * case is also true for a player situated at the start of a column.
         */
        for(int i=0;i<rows;i++){
            ArrayList<Player> row = new ArrayList<>();
            for(int j=0;j<columns;j++){
                row.add(new Player(ThreadLocalRandom.current().nextDouble(), 0.0, false));
            }
            grid.add(row);
        }
        for(int i=0;i<rows;i++){
            for(int j=0;j<columns;j++){
                grid.get(i).get(j).findNeighbours2D(grid, i, j);

                // initialise edge weights for edge decay.
                grid.get(i).get(j).initialiseEdgeWeights();
            }
        }


        // the experiment begins
        while(gen != max_gens) {


            // playing phase
            for(ArrayList<Player> row: grid){
                for(Player player: row){
                    //player.playEdgeDecaySpatialAbstinenceUG();

                    player.playEdgeDecaySpatialUG();
                }
            }


            // edge decay phase
            for(ArrayList<Player> row: grid){
                for(Player player: row){
//                    player.edgeDecay2();

                    player.edgeDecay3();
                }
            }


            /**
             * Selection and Evolution phase.
             *
             * This phase occurs every evo_phase_rate gens.
             *
             * In this weighted roulette wheel selection approach, each player in the neighbourhood
             * of the evolving player compares their average payoff accrued this generation with
             * that of the evolver. The greater the payoff is in comparison to the evolver, the
             * exponentially more likely the neighbour is to be selected as the parent. A randomly
             * generated double acts as the metaphorical ball of the roulette wheel as it ultimately
             * determines the selection. The evolver copies the strategy of the parent. If the
             * evolver is selected, effectively, no evolution occurs.
             */
            if((gen + 1) % evo_phase_rate == 0) {
                for (ArrayList<Player> row : grid) {
                    for (Player player : row) {
                        ArrayList<Player> neighbourhood = player.getNeighbourhood();
                        double[] imitation_scores = new double[neighbourhood.size()];
                        double total_imitation_score = 0;
                        double player_avg_score = player.getAverage_score();
                        for (int i = 0; i < neighbourhood.size(); i++) {
                            imitation_scores[i] = Math.exp(neighbourhood.get(i).getAverage_score() - player_avg_score);
                            total_imitation_score += imitation_scores[i];
                        }
                        total_imitation_score += 1.0;
                        double imitation_score_tally = 0;
                        double random_double_to_beat = ThreadLocalRandom.current().nextDouble();
                        for (int j = 0; j < neighbourhood.size(); j++) {
                            imitation_score_tally += imitation_scores[j];
                            double percentage = imitation_score_tally / total_imitation_score;
                            if (random_double_to_beat < percentage) {
                                player.copyStrategy(neighbourhood.get(j));
                                break;
                            }
                        }
                    }
                }

                // this allows you to debug to observe the avg p at the end of a gen.
//                getStats();
//                System.out.println("gen="+gen+"\tavg p="+avg_p);
//
//
//                // this allows you to inspect the current state of the pop every x gens.
//                int x = 100;
//                if((gen + 1) % x == 0){
//                    try {
//                        writeToCSV("DG18_grid_diagram.csv");
//                    } catch (IOException e) {
//                        throw new RuntimeException(e);
//                    }
//                    System.out.println("place BP here!");
//                }
            }

            reset();

            gen++;
        }

        getStats();

//        try {
//            writeToCSV("DG18_grid_diagram.csv");
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
    }


    public static void main(String[] args) {
        System.out.println("Executing "+Thread.currentThread().getStackTrace()[1].getClassName()+"."
                +Thread.currentThread().getStackTrace()[1].getMethodName()+"()...");
        System.out.println("Timestamp:" + java.time.Clock.systemUTC().instant());
        df.setRoundingMode(RoundingMode.UP);


        // experiment parameters
        int runs=1000;
        Player.setPrize(1.0);
        Player.setNeighbourhoodType("M");
        Player.setRate_of_change(0.001);
        rows = 30;
        columns = 30;
        N = rows * columns;
        max_gens = 10000;

        // assign how often the evolution phase occurs.
        evo_phase_rate = 3;


        System.out.println("Runs="+runs
                + ", gens="+max_gens
                + ", neighbourhood="+Player.getNeighbourhoodType()
                + ", N="+N
                + ", ROC="+Player.getRate_of_change()
                + ", evo phase rate="+evo_phase_rate
                +": ");


        double avg_p = 0;
        double[] avg_p_values = new double[runs];
        double sd_avg_p = 0;


        Instant start = Instant.now();
        for(int i=0;i<runs;i++){
            DG18 run = new DG18();
            run.start();
            avg_p += run.avg_p;
            avg_p_values[i] = run.avg_p;

            // document which run just ended.
            System.out.println("run="+i+"\tavg p="+(avg_p/(i+1)));

        }
        Instant finish = Instant.now();


        avg_p /= runs;
        for(int i=0;i<runs;i++){
            sd_avg_p += Math.pow(avg_p_values[i] - avg_p, 2);
        }
        sd_avg_p = Math.pow(sd_avg_p / runs, 0.5);
        System.out.println("avg p="+df.format(avg_p)
                + ", avg p SD="+df.format(sd_avg_p)
        );


        long secondsElapsed = Duration.between(start, finish).toSeconds();
        long minutesElapsed = Duration.between(start, finish).toMinutes();
        System.out.println("Time elapsed: "+minutesElapsed+" minutes, "+secondsElapsed%60+" seconds");
        System.out.println("Timestamp:" + java.time.Clock.systemUTC().instant());
    }


    /**
     * Calculate the average value of p across the population.
     */
    public void getStats(){
        avg_p = 0.0;
        for(ArrayList<Player> row: grid){
            for(Player player: row){
                avg_p+=player.getP();
            }
        }
        avg_p /= N;
    }


    /**
     * Player scores, games played in a generation and old p values are reset to accommodate for the
     * upcoming generation.
     */
    public void reset(){
        for(ArrayList<Player> row: grid){
            for(Player player: row){
                player.setScore(0);
                player.setGamesPlayedThisGen(0);
                player.setOld_p(player.getP());
            }
        }
    }


    /**
     * Writes the p values of the pop into a csv file.
     */
    public void writeToCSV(String filename) throws IOException {
        FileWriter fw = new FileWriter(filename, false);
        for(ArrayList<Player> row: grid){
            for (Player player : row) {
                fw.append(df.format(player.getP()) + ",");
            }
            fw.append("\n");
        }
        fw.close();
    }
}
