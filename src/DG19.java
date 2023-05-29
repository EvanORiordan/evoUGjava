//import java.math.RoundingMode;
//import java.text.DecimalFormat;
//import java.time.Duration;
//import java.time.Instant;
//import java.util.ArrayList;
//import java.util.concurrent.ThreadLocalRandom;
//import java.io.FileWriter;
//import java.io.IOException;
//
///**
// * Description of program: DG18 but with copy best neighbour selection and evolution mechanism.
// */
//public class DG19 extends Thread{
//    static int rows;
//    static int columns;
//    static int N;
//    static int max_gens;
//    static int runs; // how many times this experiment will be run.
//
//    // how often the evolution phase occurs.
//    static int evo_phase_rate;
//
//    ArrayList<ArrayList<Player>> grid = new ArrayList<>();
//    double avg_p; // the average value of p across this run's population.
//    static DecimalFormat df = Player.getDf();
//    int gen = 0;
//
//
//    public void start(){
//
//        /**
//         * Space.
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
//
//        // the experiment begins
//        while(gen != max_gens) {
//
//
//            // playing phase
//            for(ArrayList<Player> row: grid){
//                for(Player player: row){
//                    //player.playEdgeDecaySpatialAbstinenceUG();
//
//                    player.playEdgeDecaySpatialUG();
//                }
//            }
//
//
//            // edge decay phase
//            for(ArrayList<Player> row: grid){
//                for(Player player: row){
////                    player.edgeDecay2();
//
//                    player.edgeWeightLearning();
//                }
//            }
//
//
//            /**
//             * Selection and Evolution phase.
//             *
//             * This phase occurs every evo_phase_rate gens.
//             *
//             * The evolving player copies their highest scoring neighbour.
//             */
//            if((gen + 1) % evo_phase_rate == 0) {
//                for (ArrayList<Player> row : grid) {
//                    for (Player player : row) {
//                        ArrayList<Player> neighbourhood = player.getNeighbourhood();
//                        Player parent = neighbourhood.get(0);
//                        for(int a=1;a<neighbourhood.size();a++){
//                            Player neighbour = neighbourhood.get(a);
//                            if(neighbour.getScore() > parent.getScore()){
//                                parent = neighbour;
//                            }
//                        }
//                        player.copyStrategy(parent);
//                    }
//                }
//
//
//
//                // this allows you to debug to observe the avg p at the end of a gen.
////                getStats();
////                System.out.println("gen="+gen+"\tavg p="+avg_p);
////
////
////                // this allows you to inspect the current state of the pop every x gens.
////                int x = 100;
////                if((gen + 1) % x == 0){
////                    try {
////                        writeToCSV("DG18_grid_diagram.csv");
////                    } catch (IOException e) {
////                        throw new RuntimeException(e);
////                    }
////                    System.out.println("place BP here!");
////                }
//
//
//            }
//
////            displayTotalScore();
//
//
//            // calculate the avg p and SD wrt p of the pop during this gen and export it to
//            // a .csv file.
////            getStats();
////            try {
////                writeSingleGenStats("DG19_per_gen_data.csv");
////            } catch (IOException e) {
////                throw new RuntimeException(e);
////            }
////
////
//            reset();
//
//            gen++;
//
//        }
//
//        getStats();
//
////        try {
////            writeToCSV("DG18_grid_diagram.csv");
////        } catch (IOException e) {
////            throw new RuntimeException(e);
////        }
//
//
////        displayNeighbourhoodStatus();
//
//    }
//
//
//    public static void main(String[] args) {
//        System.out.println("Executing "+Thread.currentThread().getStackTrace()[1].getClassName()+"."
//                +Thread.currentThread().getStackTrace()[1].getMethodName()+"()...");
//        System.out.println("Timestamp:" + java.time.Clock.systemUTC().instant());
//        df.setRoundingMode(RoundingMode.UP);
//
//
//        // experiment parameters
//        runs=1000;
//        Player.setPrize(1.0);
//        Player.setNeighbourhoodType("VN");
//        Player.setRate_of_change(0.01);
//        rows = 30;
//        columns = 30;
//        N = rows * columns;
//        max_gens = 10000;
//
//        // assign how often the evolution phase occurs.
//        evo_phase_rate = 5;
//
//
//        displaySettings();
//
//        // this should be the average of "the average p value at the end of the final gen of each run".
//        // "runs" many data points are used to calculate this stat.
//        // this variable is not the same variable as the avg p variable of a DG18 object!
//        double avg_p = 0;
//
//        double[] avg_p_values = new double[runs];
//        double sd_avg_p = 0;
//
//
//        Instant start = Instant.now();
//        for(int i=0;i<runs;i++){
//            DG19 run = new DG19();
//            run.start();
//            avg_p += run.avg_p;
//            avg_p_values[i] = run.avg_p;
//
//            // document which run just ended.
//            System.out.println("run="+i+"\tavg p="+(avg_p/(i+1)));
//
//        }
//        Instant finish = Instant.now();
//
//
//        displaySettings();
//        avg_p /= runs;
//        for(int i=0;i<runs;i++){
//            sd_avg_p += Math.pow(avg_p_values[i] - avg_p, 2);
//        }
//        sd_avg_p = Math.pow(sd_avg_p / runs, 0.5);
//        System.out.println("avg p="+df.format(avg_p)
//                + ", avg p SD="+df.format(sd_avg_p)
//        );
//
//
//        long secondsElapsed = Duration.between(start, finish).toSeconds();
//        long minutesElapsed = Duration.between(start, finish).toMinutes();
//        System.out.println("Time elapsed: "+minutesElapsed+" minutes, "+secondsElapsed%60+" seconds");
//        System.out.println("Timestamp:" + java.time.Clock.systemUTC().instant());
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
//    public void writeToCSV(String filename) throws IOException {
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
//                + ", gens="+max_gens
//                + ", neighbourhood="+Player.getNeighbourhoodType()
//                + ", N="+N
//                + ", ROC="+Player.getRate_of_change()
//                + ", EPR="+evo_phase_rate
//                +": ");
//    }
//
//
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
//     *
//     * Note: There is no point running multiple runs when your aim is to use this method.
//     */
//    public void writeSingleGenStats(String filename) throws IOException {
//        FileWriter fw;
//        double SD = calculateSD();
//
//        // reset the .csv file for this run.
//        if(gen == 0){
//            fw = new FileWriter(filename, false); // append set to false means writing mode.
//            fw.append("");
//            fw.close();
//        }
//
//        // now, add the data to the .csv file.
//        fw = new FileWriter(filename, true); // append set to true means append mode.
//        fw.append(gen + "," + avg_p + "," + SD + "\n");
//        fw.close();
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
//}
