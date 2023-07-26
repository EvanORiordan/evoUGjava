import java.io.BufferedReader;
import java.io.FileReader;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.io.FileWriter;
import java.io.IOException;


/**
 * Programmed by Evan O'Riordan.
 *
 * If recreating the results obtained from DGAlgorithmPaper1.java, please cite the following paper:
 * insertLinkToPaperHere
 *
 * Algorithm for the Dictator Game subject to a square grid, evolution, edge weights.
 * File was previously named DG20.
 */
public class DGAlgorithmPaper1 extends Thread{

    static int rows; // how many rows in the square grid.
    static int columns; // how many rows in the square grid.
    static int N; // population size.
    static int gens; // how many generations occur per experiment run.
    static int runs; // how many times this experiment will be run.
    static int evo_phase_rate; // how often the evolutionary phase occurs.
    ArrayList<ArrayList<Player>> grid = new ArrayList<>(); // contains the population.
    double avg_p; // the average value of p across the population.
    static DecimalFormat df = Player.getDf(); // formats numbers.
    int gen = 0; // indicates which generation is currently running.
    static boolean per_gen_data; // indicates whether "per gen data" will be stored.
    static String varying_parameter; // indicates which parameter to be varied in an experiment series.
    static boolean experiment_series; // indicate whether to run an experiment or an experiment series.



    /**
     * Method for starting a run of an experiment.
     */
    public void start(){

        // initialise the population
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
                grid.get(i).get(j).initialiseEdgeWeights();
            }
        }


        // players begin playing the game
        while(gen != gens) {
            // playing phase
            for(ArrayList<Player> row: grid){
                for(Player player: row){
                    player.playEdgeDecaySpatialUG();
                }
            }


            // edge weight learning phase
            for(ArrayList<Player> row: grid){
                for(Player player: row){
                    player.edgeWeightLearning();
                }
            }


            /**
             * Selection and Evolutionary phase:
             *
             * Selection method: weighted roulette wheel.
             * Evolution method: copy parent's strategy.
             *
             * This whole phase occurs every evo_phase_rate gens.
             */
            if((gen + 1) % evo_phase_rate == 0) {
                for (ArrayList<Player> row : grid) {
                    for (Player player : row) {
                        ArrayList<Player> neighbourhood = player.getNeighbourhood();
                        double[] imitation_scores = new double[neighbourhood.size()];
                        double total_imitation_score = 0;
                        double player_avg_score = player.getAverage_score();
                        for (int i = 0; i < neighbourhood.size(); i++) {
                            imitation_scores[i] =
                                    Math.exp(neighbourhood.get(i).getAverage_score() - player_avg_score);
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


            // collect "per gen data" if enabled.
            if(per_gen_data){
                getStats();
                writeSingleGenStats("per_gen_data.csv");
            }

            reset(); // reset certain player attributes.

            gen++; // move on to the next generation
        }
        getStats(); // get stats at the end of the run
    }




    // main method for executing the program/algorithm
    public static void main(String[] args) {

        // marks the beginning of the program's runtime
        Instant start = Instant.now();
        System.out.println("Executing "+Thread.currentThread().getStackTrace()[1].getClassName()+"."
                +Thread.currentThread().getStackTrace()[1].getMethodName()+"()...");
        System.out.println("Timestamp:" + java.time.Clock.systemUTC().instant());


        // define name of .csv file for storing experiment data.
        String data_filename = Thread.currentThread().getStackTrace()[1].getClassName() + "data.csv";


        // define initial parameter values.
        runs=1000;
        Player.setRate_of_change(0.2);
        rows = 30;
        gens = 10000;
        evo_phase_rate = 1;


        // below are attributes of the experiment that should not be changed.
        Player.setPrize(1.0);
        Player.setNeighbourhoodType("VN");
        columns = rows;
        N = rows * columns;


        // define whether an experiment or an experiment series will be conducted.
        experiment_series = true;
//        experiment_series = false;

        if(experiment_series){ // for carrying out an experiment series

            // define the parameter to be varied across the experiment series.
//            varying_parameter = "ROC"; // vary the rate of change.
            varying_parameter = "EPR"; // vary the evolutionary phase rate.
//            varying_parameter = "gens"; // vary the number of generations.
            varying_parameter = "rows_columns"; // vary the number of rows and columns.


            // define the amount by which the parameter will vary between subsequent experiments.
            // note: the double type here also works for varying integer type params such as gens.
            double variation = 1;


            int num_experiments = 8; // define number of experiments to occur here


            // display which parameter is being modified and by how much per experiment.
            System.out.println("Varying "+varying_parameter+" by "+variation+" between "+num_experiments+
                    " experiments with settings: ");
//
            per_gen_data = false; // for experiment series, there is little use for collecting per gen data.
            experimentSeries(data_filename, variation, num_experiments); // run the experiment series.
        }

        else { // for carrying out a single experiment.
            per_gen_data = true; // collect per gen data. then you can visualise the experiment results.
            experiment(data_filename, 0); // run an experiment
        }


        // marks the end of the program's runtime
        System.out.println("Timestamp:" + java.time.Clock.systemUTC().instant());
        Instant finish = Instant.now();
        long secondsElapsed = Duration.between(start, finish).toSeconds();
        long minutesElapsed = Duration.between(start, finish).toMinutes();
        System.out.println("Time elapsed: "+minutesElapsed+" minutes, "+secondsElapsed%60+" seconds");
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
                + ", gens="+gens
                + ", neighbourhood="+Player.getNeighbourhoodType()
                + ", N="+N
                + ", ROC="+df.format(Player.getRate_of_change())
                + ", EPR="+evo_phase_rate
                +": ");
    }


//    /**
//     * Displays the number of players for whom all associated edge weights are set to 0.0.
//     */
//    public void displayNeighbourhoodStatus(){
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
//    }


//    /**
//     * Displays how much score is being accumulated across the whole pop in a given gen.
//     */
//    public void displayTotalScore(){
//        double total_accumulated_score = 0.0;
//        for(ArrayList<Player> row: grid){
//            for(Player player: row){
//                total_accumulated_score += player.getScore();
//            }
//        }
//        System.out.println(total_accumulated_score);
//    }


    /**
     * Allows for the visualisation of the avg p of a run with respect to gens, with gens on x-axis
     * and avg p on y-axis. Now also collects standard deviation (SD) data.
     *
     * Steps:
     * Export the data of a single run to a .csv file
     * Import the .csv data into an Excel sheet
     * Separate the data into columns: gen number, avg p and SD for that gen
     * Create a line chart with the data.
     */
    public void writeSingleGenStats(String filename){
        FileWriter fw;
        double SD = calculateSD();

        try{
            // reset the .csv file for this run.
            if(gen == 0){
                fw = new FileWriter(filename, false); // append set to false means writing mode.
                fw.append("gen"
                        + ",avg p"
                        + ",p SD"
                        + ",gens="+gens
                        + ",neighbourhood="+Player.getNeighbourhoodType()
                        + ",N="+N
                        + ",ROC="+Player.getRate_of_change()
                        + ",EPR="+evo_phase_rate
                        + "\n"
                );
                fw.close();
            }

            // add the data to the .csv file.
            fw = new FileWriter(filename, true); // append set to true means append mode.
            fw.append(gen + "," + df.format(avg_p) + "," + df.format(SD) + "\n");
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
     * Allows for the running of an experiment. Collects data after each experiment into .csv file.
     */
    public static void experiment(String filename, int experiment_number){
        displaySettings(); // display settings of experiment

        // stats to be tracked
        double mean_avg_p_of_experiment = 0;
        double[] avg_p_values_of_experiment = new double[runs];
        double sd_avg_p_of_experiment = 0;

        // perform the experiment multiple times
        for(int i=0;i<runs;i++){
            DGAlgorithmPaper1 run = new DGAlgorithmPaper1();
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

        // write stats/results and settings to the .csv data file.
        try{
            FileWriter fw;

            if(experiment_number == 0){
                fw = new FileWriter(filename, false);
                fw.append("experiment"
                        + ",mean avg p"
                        + ",avg p SD"
                        + ",runs"
                        + ",gens"
                        + ",neighbourhood"
                        + ",N"
                        + ",ROC"
                        + ",EPR"
                );
            } else {
                fw = new FileWriter(filename, true);
            }
            fw.append("\n" + experiment_number
                    + "," + df.format(mean_avg_p_of_experiment)
                    + "," + df.format(sd_avg_p_of_experiment)
                    + "," + runs
                    + "," + gens
                    + "," + Player.getNeighbourhoodType()
                    + "," + N
                    + "," + Player.getRate_of_change()
                    + "," + evo_phase_rate
            );
            fw.close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }


    /**
     * Allows for the running of multiple experiments, i.e. the running of a series of
     * experiments, i.e. the running of an experiment series.
     */
    public static void experimentSeries(String filename, double variation, int num_experiments){

        // run the experiment series.
        for(int i=0;i<num_experiments;i++){
            experiment(filename, i); // run the experiment and store its final data

            // change the value of the parameter
            if(varying_parameter.equals("ROC")){
                Player.setRate_of_change(Player.getRate_of_change() + variation);
            } else if(varying_parameter.equals("EPR")){
                evo_phase_rate += variation;
            } else if(varying_parameter.equals("gens")){
                gens += variation;
            } else if(varying_parameter.equals("rows_columns")){
                rows += variation;
                columns += variation;
            }
        }


        // display a summary of the experiment series on the console.
        String summary = "";
        ArrayList<String> experiment_number = new ArrayList<>();
        ArrayList<Double> mean_avg_p = new ArrayList<>();
        ArrayList<Double> avg_p_SD = new ArrayList<>();
        ArrayList<Integer> gens = new ArrayList<>();
        ArrayList<Integer> N = new ArrayList<>();
        ArrayList<Double> ROC = new ArrayList<>();
        ArrayList<Integer> EPR = new ArrayList<>();

//        ArrayList<Integer> runs = new ArrayList<>();
//        ArrayList<String> neighbourhood = new ArrayList<>();

        int row_count = 0;
        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String line = "";
            while((line = br.readLine()) != null){
                String[] row_contents = line.split(",");
                if(row_count != 0){
                    experiment_number.add(row_contents[0]);
                    mean_avg_p.add(Double.valueOf(row_contents[1]));
                    avg_p_SD.add(Double.valueOf(row_contents[2]));

//                    runs.add(Integer.valueOf(row_contents[3]));
//                    neighbourhood.add(String.valueOf(row_contents[5]));

                    if(varying_parameter.equals("gens")){
                        gens.add(Integer.valueOf(row_contents[4]));
                    } else if(varying_parameter.equals("rows_columns")){
                        N.add(Integer.valueOf(row_contents[6]));
                    } else if(varying_parameter.equals("ROC")){
                        ROC.add(Double.valueOf(row_contents[7]));
                    } else if(varying_parameter.equals("EPR")){
                        EPR.add(Integer.valueOf(row_contents[8]));
                    }
                }
                row_count++;
            }
        } catch(IOException e){
            e.printStackTrace();
        }

        for(int i=0;i<row_count-1;i++){
            summary += "experiment="+experiment_number.get(i)
                    + "\tmean avg p="+df.format(mean_avg_p.get(i))
                    + "\tavg p SD="+df.format(avg_p_SD.get(i))
            ;

            if(varying_parameter.equals("gens")){
                summary += "\tgens=" + gens.get(i);
            } else if(varying_parameter.equals("rows_columns")){
                summary += "\tN=" + N.get(i);
            } else if(varying_parameter.equals("ROC")){
                summary += "\tROC=" + df.format(ROC.get(i));
            } else if(varying_parameter.equals("EPR")){
                summary += "\tEPR=" + EPR.get(i);
            }
            summary += "\n";
        }
        System.out.println(summary);
    }
}
