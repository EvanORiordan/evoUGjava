//import java.io.BufferedReader;
//import java.io.FileReader;
//import java.math.BigDecimal;
//import java.math.RoundingMode;
//import java.text.DecimalFormat;
//import java.time.Duration;
//import java.time.Instant;
//import java.util.ArrayList;
//import java.util.Scanner;
//import java.util.concurrent.ThreadLocalRandom;
//import java.io.FileWriter;
//import java.io.IOException;
//
///**
// * Description of program: DG20 but with scanner for reading user input for defining experimental settings.
// */
//public class DG21 extends Thread{
//    // experiment-wide parameters
//    static int rows;
//    static int columns;
//    static int N;
//    static int gens;
//    static int runs; // how many times this experiment will be run.
//    static int evo_phase_rate; // how often the evolutionary phase occurs.
//
//    // attributes of individual runs
//    ArrayList<ArrayList<Player>> grid = new ArrayList<>();
//    double avg_p; // the average value of p across this run's population.
//    static DecimalFormat df = Player.getDf();
//    int gen = 0;
//
//
//    // new variables of this version
//    static boolean per_gen_data; // indicates whether per gen data will be stored
//    static String varying_parameter; // indicates which parameter to be varied over experiments
//    static boolean experiment_series; // indicate whether to run an experiment or a series.
//
//
//    static Scanner scan = new Scanner(System.in); // read user input
//
//
//
//    /**
//     * Method for starting a run of an experiment.
//     */
//    public void start(){
//
//        /**
//         * Space:
//         *
//         * Initialise population in the form of a square grid. A player situated at the start
//         * of a row is neighbours with the player situated at the end of the row. The similar
//         * case is also true for a player situated at the start of a column.
//         */
//        for(int i=0;i<rows;i++){
//            ArrayList<Player> row = new ArrayList<>();
//            for(int j=0;j<columns;j++){
//                row.add(new Player(ThreadLocalRandom.current().nextDouble(), 0.0, false));
//            }
//            grid.add(row);
//        }
//        for(int i=0;i<rows;i++){
//            for(int j=0;j<columns;j++){
//                grid.get(i).get(j).findNeighbours2D(grid, i, j);
//
//                // initialise edge weights for edge decay.
//                grid.get(i).get(j).initialiseEdgeWeights();
//            }
//        }
//
//        // preparation over; the games begin
//        while(gen != gens) {
//
//
//            // playing phase
//            for(ArrayList<Player> row: grid){
//                for(Player player: row){
//                    player.playEdgeDecaySpatialUG();
//                }
//            }
//
//
//            // edge weight learning phase
//            for(ArrayList<Player> row: grid){
//                for(Player player: row){
//                    player.edgeWeightLearning();
//                }
//            }
//
//
//            /**
//             * Selection and evolutionary phase:
//             *
//             * Selection: weighted roulette wheel
//             * Evolution: copy parent's strategy
//             * This collective phase occurs every evo_phase_rate gens.
//             */
//            if((gen + 1) % evo_phase_rate == 0) {
//                for (ArrayList<Player> row : grid) {
//                    for (Player player : row) {
//                        ArrayList<Player> neighbourhood = player.getNeighbourhood();
//                        double[] imitation_scores = new double[neighbourhood.size()];
//                        double total_imitation_score = 0;
//                        double player_avg_score = player.getAverage_score();
//                        for (int i = 0; i < neighbourhood.size(); i++) {
//                            imitation_scores[i] = Math.exp(neighbourhood.get(i).getAverage_score() - player_avg_score);
//                            total_imitation_score += imitation_scores[i];
//                        }
//                        total_imitation_score += 1.0;
//                        double imitation_score_tally = 0;
//                        double random_double_to_beat = ThreadLocalRandom.current().nextDouble();
//                        for (int j = 0; j < neighbourhood.size(); j++) {
//                            imitation_score_tally += imitation_scores[j];
//                            double percentage = imitation_score_tally / total_imitation_score;
//                            if (random_double_to_beat < percentage) {
//                                player.copyStrategy(neighbourhood.get(j));
//                                break;
//                            }
//                        }
//                    }
//                }
//            }
//
//
//            // collect per gen data
//            if(per_gen_data){
//                getStats();
//                writeSingleGenStats("per_gen_data.csv");
//            }
//
//            reset();
//
//            gen++;
//
//        }
//
//        // get stats at the end of the run
//        getStats();
//
//    }
//
//
//
//
//    public static void main(String[] args) {
//        // marks the beginning of the program's runtime
//        Instant start = Instant.now();
//        System.out.println("Executing "+Thread.currentThread().getStackTrace()[1].getClassName()+"."
//                +Thread.currentThread().getStackTrace()[1].getMethodName()+"()...");
//        System.out.println("Timestamp:" + java.time.Clock.systemUTC().instant());
//
//        // define name of .csv file for storing experiment data.
//        String data_filename = Thread.currentThread().getStackTrace()[1].getClassName() + "data.csv";
//
//        // define initial parameter values.
//        runs=100;
//        Player.setPrize(1.0);
//        Player.setNeighbourhoodType("VN");
//        Player.setRate_of_change(0.2);
//        rows = 30;
//        columns = 30;
//        N = rows * columns;
//        gens = 10000;
//        evo_phase_rate = 1;
//
//
//        // user selects experiment or series.
//        while(true){ // beware of infinite loops!
//            System.out.println("experiment series (y/n)? ");
//            String input = scan.next();
//            if(input.equals("y")){
//                experiment_series = true;
//                break;
//            } else if(input.equals("n")){
//                experiment_series = false;
//                break;
//            } else {
//                System.out.println("invalid input");
//            }
//        }
//
//
//        if(experiment_series){ // for carrying out an experiment series.
//
//            // user selects the parameter to be varied across the experiment series.
//            while(true){
//                System.out.println("vary which parameter (enter a number of your choice)?" +
//                        "\n1: ROC" +
//                        "\n2: EPR" +
//                        "\n3: gens"
//                );
//                int input = scan.nextInt();
//                if(input == 1){
//                    varying_parameter = "ROC";
//                    break;
//                } else if (input == 2){
//                    varying_parameter = "EPR";
//                    break;
//                } else if (input == 3){
//                    varying_parameter = "gens";
//                    break;
//                } else {
//                    System.out.println("invalid input");
//                }
//            }
//
//
//            // user defines by how much the varying parameter will vary between subsequent experiments.
//            System.out.println("vary by how much per experiment? ");
//            double variation = scan.nextDouble();
//
//
//            System.out.println("how many experiments? ");
//            int num_experiments = scan.nextInt(); // define number of experiments to occur here
//
//            // display which parameter is being modified and by how much per experiment.
//            System.out.println("Varying "+varying_parameter+" by "+variation+" between "+num_experiments+
//                    " experiments with settings: ");
////
//            per_gen_data = false; // with an experiment series, I typically won't want to collect per gen data.
//            experimentSeries(data_filename, variation, num_experiments); // run multiple experiments
//        }
//
//        else { // for carrying out a single experiment.
//            per_gen_data = true;
//            experiment(data_filename, 0); // run 1 experiment
//        }
//
//
//        // marks the end of the program's runtime
//        System.out.println("Timestamp:" + java.time.Clock.systemUTC().instant());
//        Instant finish = Instant.now();
//        long secondsElapsed = Duration.between(start, finish).toSeconds();
//        long minutesElapsed = Duration.between(start, finish).toMinutes();
//        System.out.println("Time elapsed: "+minutesElapsed+" minutes, "+secondsElapsed%60+" seconds");
//        scan.close();
//    }
//
//
//    /**
//     * Calculate the average value of p across the population at the current gen.
//     *
//     * The most important avg p is that of the final gen. That particular value is what is being
//     * used to calculate the avg p of the experiment as a whole.
//     */
//    public void getStats(){
//        avg_p = 0.0;
//        for(ArrayList<Player> row: grid){
//            for(Player player: row){
//                avg_p+=player.getP();
//            }
//        }
//        avg_p /= N;
//    }
//
//
//    /**
//     * Player scores, games played in a generation and old p values are reset to accommodate for the
//     * upcoming generation.
//     */
//    public void reset(){
//        for(ArrayList<Player> row: grid){
//            for(Player player: row){
//                player.setScore(0);
//                player.setGamesPlayedThisGen(0);
//                player.setOld_p(player.getP());
//            }
//        }
//    }
//
//
//    /**
//     * Writes the p values of the pop into a csv file.
//     */
//    public void writePop(String filename) throws IOException {
//        FileWriter fw = new FileWriter(filename, false);
//        for(ArrayList<Player> row: grid){
//            for (Player player : row) {
//                fw.append(df.format(player.getP()) + ",");
//            }
//            fw.append("\n");
//        }
//        fw.close();
//    }
//
//
//    /**
//     * Displays experiment settings.
//     */
//    public static void displaySettings(){
//        System.out.println("runs="+runs
//                + ", gens="+gens
//                + ", neighbourhood="+Player.getNeighbourhoodType()
//                + ", N="+N
//                + ", ROC="+df.format(Player.getRate_of_change())
//                + ", EPR="+evo_phase_rate
//                +": ");
//    }
//
//
//    /**
//     * Displays the number of players for whom all associated edge weights are set to 0.0.
//     */
//    public void displayNeighbourhoodStatus(){
////        int num_players_with_all_associated_edge_weights_at_0 = 0;
////        int num_associated_edge_weights_per_player = grid.get(0).get(0).getEdge_weights().length * 2;
////        for(ArrayList<Player> row: grid){
////
////            for(Player player: row){
////
////                // look at player's own edge weights
////                int num_edge_weights_at_0 = 0;
////                for(int i=0;i<player.getEdge_weights().length;i++){
////                    if(player.getEdge_weights()[i] == 0.0){
////                        num_edge_weights_at_0++;
////                    }
////                }
////
////                // look at neighbours' edge weights associated to player
////                ArrayList<Player> neighbourhood = player.getNeighbourhood();
////                for(int i=0;i<neighbourhood.size();i++) {
////                    Player neighbour = neighbourhood.get(i);
////                    double edge_weight = 0.0;
////                    for (int j = 0; j < neighbour.getNeighbourhood().size(); j++){
////                        Player neighbours_neighbour = neighbour.getNeighbourhood().get(j);
////                        if (neighbours_neighbour.getId() == player.getId()) {
////                            edge_weight = neighbour.getEdge_weights()[j];
////                            break;
////                        }
////                    }
////                    if(edge_weight == 0.0){
////                        num_edge_weights_at_0++;
////                    }
////                }
////
////                if(num_edge_weights_at_0 == num_associated_edge_weights_per_player){
////                    num_players_with_all_associated_edge_weights_at_0++;
////                }
////
////            }
////        }
////        System.out.println("Number of players for whom all associated edge weights are set " +
////                "to 0.0: "+num_players_with_all_associated_edge_weights_at_0);
//    }
//
//
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
//
//
//    /**
//     * Allows for the visualisation of the avg p of a run with respect to gens, with gens on x-axis
//     * and avg p on y-axis. Now also supports SD visualisation.
//     *
//     * Steps:
//     * Export the data of a single run to a .csv file
//     * Import the .csv data into an Excel sheet
//     * Separate the data into columns: gen number, avg p and SD for that gen
//     * Create a line chart with the data.
//     */
//    public void writeSingleGenStats(String filename){
//        FileWriter fw;
//        double SD = calculateSD();
//
//        try{
//            // reset the .csv file for this run.
//            if(gen == 0){
//                fw = new FileWriter(filename, false); // append set to false means writing mode.
//                fw.append("gen"
//                        + ",avg p"
//                        + ",p SD"
//                        + ",gens="+gens
//                        + ",neighbourhood="+Player.getNeighbourhoodType()
//                        + ",N="+N
//                        + ",ROC="+Player.getRate_of_change()
//                        + ",EPR="+evo_phase_rate
//                        + "\n"
//                );
//                fw.close();
//            }
//
//            // now, add the data to the .csv file.
//            fw = new FileWriter(filename, true); // append set to true means append mode.
//            fw.append(gen + "," + df.format(avg_p) + "," + df.format(SD) + "\n");
//            fw.close();
//        } catch(IOException e){
//            e.printStackTrace();
//        }
//
//    }
//
//
//    /**
//     * Calculates SD of the pop wrt p.
//     * @return double SD
//     */
//    public double calculateSD(){
//        double SD = 0.0;
//        for(ArrayList<Player> row: grid){
//            for(Player player: row){
//                SD += Math.pow(player.getP() - avg_p, 2);
//            }
//        }
//        SD = Math.pow(SD / N, 0.5);
//
//        return SD;
//    }
//
//
//
//
//    /**
//     * Allows for the running of an experiment. Collects data after each experiment.
//     */
//    public static void experiment(String filename, int experiment_number){
//        displaySettings(); // display settings of experiment
//
//        // stats to be tracked
//        double mean_avg_p_of_experiment = 0;
//        double[] avg_p_values_of_experiment = new double[runs];
//        double sd_avg_p_of_experiment = 0;
//
//        // perform the experiment multiple times
//        for(int i=0;i<runs;i++){
//            DG21 run = new DG21();
//            run.start();
//            mean_avg_p_of_experiment += run.avg_p;
//            avg_p_values_of_experiment[i] = run.avg_p;
//
//            // display the final avg p of the pop of the run that just concluded.
//            // this is a different print statement than the one in a similar position in DG18.java!
//            System.out.println("final avg p of run "+i+" of experiment "+experiment_number+
//                    ": "+run.avg_p);
//        }
//
//        // calculate stats
//        mean_avg_p_of_experiment /= runs;
//        for(int i=0;i<runs;i++){
//            sd_avg_p_of_experiment +=
//                    Math.pow(avg_p_values_of_experiment[i] - mean_avg_p_of_experiment, 2);
//        }
//        sd_avg_p_of_experiment = Math.pow(sd_avg_p_of_experiment / runs, 0.5);
//
//        // display stats in console
//        System.out.println("mean avg p="+df.format(mean_avg_p_of_experiment)
//                + ", avg p SD="+df.format(sd_avg_p_of_experiment)
//        );
//
//        // write stats/results and settings to the .csv data file.
//        try{
//            FileWriter fw;
//
//            if(experiment_number == 0){
//                fw = new FileWriter(filename, false);
//                fw.append("experiment"
//                        + ",mean avg p"
//                        + ",avg p SD"
//                        + ",runs"
//                        + ",gens"
//                        + ",neighbourhood"
//                        + ",N"
//                        + ",ROC"
//                        + ",EPR"
//                );
//            } else {
//                fw = new FileWriter(filename, true);
//            }
//            fw.append("\n" + experiment_number
//                    + "," + mean_avg_p_of_experiment
//                    + "," + sd_avg_p_of_experiment
//                    + "," + runs
//                    + "," + gens
//                    + "," + Player.getNeighbourhoodType()
//                    + "," + N
//                    + "," + Player.getRate_of_change()
//                    + "," + evo_phase_rate
//            );
//            fw.close();
//        } catch(IOException e){
//            e.printStackTrace();
//        }
//    }
//
//
//    /**
//     * Allows for the running of multiple experiments, i.e. the running of a series of
//     * experiments, i.e. the running of an experiment series.
//     */
//    public static void experimentSeries(String filename, double variation, int num_experiments){
//        for(int i=0;i<num_experiments;i++){
//            experiment(filename, i); // run the experiment and store its final data
//
//            // change the value of the parameter
//            if(varying_parameter.equals("ROC")){
//                Player.setRate_of_change(Player.getRate_of_change() + variation);
//            } else if(varying_parameter.equals("EPR")){
//                evo_phase_rate += variation;
//            } else if(varying_parameter.equals("gens")){
//                gens += variation;
//            }
//            // else if ...
//
//
//        }
//
//
//        /**
//         * displayExperimentSeriesSummary(); // you could put this code into a modular method.
//         *
//         * in console, print out all the macro experiment info in a nice format that I can copy
//         * and paste into my experiment notebook. achieve this by reading from the data file.
//         */
//
//        String summary = "";
//        ArrayList<String> experiment_number = new ArrayList<>();
//        ArrayList<Double> mean_avg_p = new ArrayList<>();
//        ArrayList<Double> avg_p_SD = new ArrayList<>();
//        ArrayList<Integer> runs = new ArrayList<>();
//        ArrayList<Integer> gens = new ArrayList<>();
//        ArrayList<String> neighbourhood = new ArrayList<>();
//        ArrayList<Integer> N = new ArrayList<>();
//        ArrayList<Double> ROC = new ArrayList<>();
//        ArrayList<Integer> EPR = new ArrayList<>();
//        int row_count = 0;
//        try {
//            BufferedReader br = new BufferedReader(new FileReader(filename));
//            String line = "";
//            while((line = br.readLine()) != null){
//                String[] row_contents = line.split(",");
//                if(row_count != 0){
//                    experiment_number.add(row_contents[0]);
//                    mean_avg_p.add(Double.valueOf(row_contents[1]));
//                    avg_p_SD.add(Double.valueOf(row_contents[2]));
//
////                    runs.add(Integer.valueOf(row_contents[3]));
////                    neighbourhood.add(String.valueOf(row_contents[5]));
////                    N.add(Integer.valueOf(row_contents[6]));
//
//                    if(varying_parameter.equals("gens")){
//                        gens.add(Integer.valueOf(row_contents[4]));
//                    } else if(varying_parameter.equals("ROC")){
//                        ROC.add(Double.valueOf(row_contents[7]));
//                    } else if(varying_parameter.equals("EPR")){
//                        EPR.add(Integer.valueOf(row_contents[8]));
//                    } // else if ...
//
//                }
//                row_count++;
//            }
//
//        } catch(IOException e){
//            e.printStackTrace();
//        }
//
//        for(int i=0;i<row_count-1;i++){
//            summary += "experiment="+experiment_number.get(i)
//                    + "\tmean avg p="+df.format(mean_avg_p.get(i))
//                    + "\tavg p SD="+df.format(avg_p_SD.get(i))
//            ;
//
//            if(varying_parameter.equals("gens")){
//                summary += "\tgens=" + gens.get(i);
//            } else if(varying_parameter.equals("ROC")){
//                summary += "\tROC=" + df.format(ROC.get(i));
//            } else if(varying_parameter.equals("EPR")){
//                summary += "\tEPR=" + EPR.get(i);
//            } // else if...
//
//            summary += "\n";
//        }
//
//        System.out.println(summary);
//
//
//    }
//}
