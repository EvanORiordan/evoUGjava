import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Similar to DG18.java except this program extends its ability for experimentation.
 */
public class DG20 extends Thread{
    // experiment parameters
    static int rows;
    static int columns;
    static int N;
    static int max_gens;
    static int runs; // how many times this experiment will be run.
    static int evo_phase_rate; // how often the evolution phase occurs.


    ArrayList<ArrayList<Player>> grid = new ArrayList<>();
    double avg_p; // the average value of p across this run's population.
    static DecimalFormat df = Player.getDf();
    int gen = 0;


    static boolean per_gen_data; // indicates whether per gen data will be stored


    /**
     * Method for starting a run of an experiment.
     */
    public void start(){

        /**
         * Space:
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
                    player.playEdgeDecaySpatialUG();
                }
            }


            // edge weight learning phase
//            for(ArrayList<Player> row: grid){
//                for(Player player: row){
//                    player.edgeWeightLearning();
//                }
//            }


            /**
             * Selection and Evolution phase:
             *
             * Selection: weighted roulette wheel
             * Evolution: copy parent's strategy
             * This collective phase occurs every evo_phase_rate gens.
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
            }


            // collect per gen data
            if(per_gen_data){
                getStats();
                writeSingleGenStats("per_gen_data.csv");
            }

            reset();

            gen++;

        }

        // get stats at the end of the run
        getStats();

    }




    public static void main(String[] args) {
        // marks the beginning of the program's runtime
        System.out.println("Executing "+Thread.currentThread().getStackTrace()[1].getClassName()+"."
                +Thread.currentThread().getStackTrace()[1].getMethodName()+"()...");
        System.out.println("Timestamp:" + java.time.Clock.systemUTC().instant());

        df.setRoundingMode(RoundingMode.UP);

        // define name of .csv file for storing experiment data
        String data_filename = Thread.currentThread().getStackTrace()[1].getClassName()
                + "data.csv";

        // define parameters
        runs=5;
        Player.setPrize(1.0);
        Player.setNeighbourhoodType("VN");
        Player.setRate_of_change(new BigDecimal("0.2"));
        rows = 30;
        columns = 30;
        N = rows * columns;
        max_gens = 10000;
        evo_phase_rate = 5;



        /**
         * With this variable, you can define the amount by which one parameter's value will be
         * altered between one experiment and the next.
         */
        BigDecimal variation = new BigDecimal("-0.02");
        int num_experiments = 6; // define number of experiments to occur here
        per_gen_data = false;
        experimentSeries(data_filename, variation, num_experiments); // run multiple experiments


//        per_gen_data = true;
//        experiment(data_filename, 0); // run 1 experiment


        // marks the end of the program's runtime
        System.out.println("Timestamp:" + java.time.Clock.systemUTC().instant());
    }


    /**
     * Calculate the average value of p across the population at the current gen.
     *
     * The most important avg p is that of the final gen. That particular value is what is being
     * used to calculate the avg p of the experiment as a whole.
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
    public void writePop(String filename) throws IOException {
        FileWriter fw = new FileWriter(filename, false);
        for(ArrayList<Player> row: grid){
            for (Player player : row) {
                fw.append(df.format(player.getP()) + ",");
            }
            fw.append("\n");
        }
        fw.close();
    }


    /**
     * Displays experiment settings.
     */
    public static void displaySettings(){
        System.out.println("runs="+runs
                + ", gens="+max_gens
                + ", neighbourhood="+Player.getNeighbourhoodType()
                + ", N="+N
                + ", ROC="+Player.getRate_of_change()
                + ", EPR="+evo_phase_rate
                +": ");
    }


    /**
     * Displays the number of players for whom all associated edge weights are set to 0.0.
     */
    public void displayNeighbourhoodStatus(){
//        int num_players_with_all_associated_edge_weights_at_0 = 0;
//        int num_associated_edge_weights_per_player = grid.get(0).get(0).getEdge_weights().length * 2;
//        for(ArrayList<Player> row: grid){
//
//            for(Player player: row){
//
//                // look at player's own edge weights
//                int num_edge_weights_at_0 = 0;
//                for(int i=0;i<player.getEdge_weights().length;i++){
//                    if(player.getEdge_weights()[i] == 0.0){
//                        num_edge_weights_at_0++;
//                    }
//                }
//
//                // look at neighbours' edge weights associated to player
//                ArrayList<Player> neighbourhood = player.getNeighbourhood();
//                for(int i=0;i<neighbourhood.size();i++) {
//                    Player neighbour = neighbourhood.get(i);
//                    double edge_weight = 0.0;
//                    for (int j = 0; j < neighbour.getNeighbourhood().size(); j++){
//                        Player neighbours_neighbour = neighbour.getNeighbourhood().get(j);
//                        if (neighbours_neighbour.getId() == player.getId()) {
//                            edge_weight = neighbour.getEdge_weights()[j];
//                            break;
//                        }
//                    }
//                    if(edge_weight == 0.0){
//                        num_edge_weights_at_0++;
//                    }
//                }
//
//                if(num_edge_weights_at_0 == num_associated_edge_weights_per_player){
//                    num_players_with_all_associated_edge_weights_at_0++;
//                }
//
//            }
//        }
//        System.out.println("Number of players for whom all associated edge weights are set " +
//                "to 0.0: "+num_players_with_all_associated_edge_weights_at_0);
    }


    /**
     * Displays how much score is being accumulated across the whole pop in a given gen.
     */
    public void displayTotalScore(){
        double total_accumulated_score = 0.0;
        for(ArrayList<Player> row: grid){
            for(Player player: row){
                total_accumulated_score += player.getScore();
            }
        }
        System.out.println(total_accumulated_score);
    }


    /**
     * Allows for the visualisation of the avg p of a run with respect to gens, with gens on x-axis
     * and avg p on y-axis. Now also supports SD visualisation.
     *
     * Steps:
     * Export the data of a single run to a .csv file
     * Import the .csv data into an Excel sheet
     * Separate the data into columns: gen number, avg p and SD for that gen
     * Create a line chart with the data.
     *
     * Note: There is no point running multiple runs when your aim is to use this method.
     */
    public void writeSingleGenStats(String filename){
        FileWriter fw;
        double SD = calculateSD();

        try{
            // reset the .csv file for this run.
            if(gen == 0){
                fw = new FileWriter(filename, false); // append set to false means writing mode.
                fw.append("gen" + // heading 1
                        ",mean avg p" + // heading 2
                        ",avg p SD\n" // heading 3
                );
                fw.close();
            }

            // now, add the data to the .csv file.
            fw = new FileWriter(filename, true); // append set to true means append mode.
            fw.append(gen + "," + avg_p + "," + SD + "\n");
            fw.close();
        } catch(IOException e){
            e.printStackTrace();
        }

    }


    /**
     * Calculates SD of the pop wrt p.
     * @return double SD
     */
    public double calculateSD(){
        double SD = 0.0;
        for(ArrayList<Player> row: grid){
            for(Player player: row){
                SD += Math.pow(player.getP() - avg_p, 2);
            }
        }
        SD = Math.pow(SD / N, 0.5);

        return SD;
    }




    /**
     * Allows for the running of an experiment. Collects data after each experiment.
     */
    public static void experiment(String filename, int experiment_number){
        displaySettings(); // display settings of experiment

        // stats to be tracked
        double mean_avg_p_of_experiment = 0;
        double[] avg_p_values_of_experiment = new double[runs];
        double sd_avg_p_of_experiment = 0;

        // perform the experiment multiple times
        for(int i=0;i<runs;i++){
            DG20 run = new DG20();
            run.start();
            mean_avg_p_of_experiment += run.avg_p;
            avg_p_values_of_experiment[i] = run.avg_p;

            // display the final avg p of the pop of the run that just concluded.
            // this is a different print statement than the one in a similar position in DG18.java!
            System.out.println("final avg p of run "+i+" of experiment "+experiment_number+
                    ": "+run.avg_p);
        }

        // calculate stats
        mean_avg_p_of_experiment /= runs;
        for(int i=0;i<runs;i++){
            sd_avg_p_of_experiment +=
                    Math.pow(avg_p_values_of_experiment[i] - mean_avg_p_of_experiment, 2);
        }
        sd_avg_p_of_experiment = Math.pow(sd_avg_p_of_experiment / runs, 0.5);

        // display stats in console
        System.out.println("mean avg p="+df.format(mean_avg_p_of_experiment)
                + ", avg p SD="+df.format(sd_avg_p_of_experiment)
        );

        // write results and settings to the .csv data file.
        try{
            FileWriter fw;

            if(experiment_number == 0){
                fw = new FileWriter(filename, false);

                fw.append("mean avg p" + // heading 1
                        ",avg p SD" + // heading 2

                        // these are the settings of the experiment
                        ",runs="+runs+
                        ",gens="+max_gens+
                        ",neighbourhood="+Player.getNeighbourhoodType()+
                        ",N="+N+
                        ",ROC="+Player.getRate_of_change()+
                        ",EPR="+evo_phase_rate
                );



            } else {
                fw = new FileWriter(filename, true);
            }
            fw.append(mean_avg_p_of_experiment+","+sd_avg_p_of_experiment);

            fw.close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }


    /**
     * Allows for the running of multiple experiments.
     */
    public static void experimentSeries(String filename, BigDecimal variation, int num_experiments){
        for(int i=0;i<num_experiments;i++){
            // run the experiment and store its final data
            experiment(filename, i);

            // write settings
            try{
                FileWriter fw = new FileWriter(filename, true);

                fw.append("runs="+runs+
                        "\ngens="+max_gens+
                        "\nneighbourhood="+Player.getNeighbourhoodType()+
                        "\nN="+N+
                        "\nROC="+Player.getRate_of_change()+
                        "\nEPR="+evo_phase_rate);

                fw.close();
            } catch(IOException e){
                e.printStackTrace();
            }

            // alter a parameter's value in preparation for the next experiment.
            Player.setRate_of_change(Player.getRate_of_change().add(variation));
        }
    }
}
