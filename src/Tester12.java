import java.util.ArrayList;

/**
 * <p>Idea from the TO DO list: Fair mutants invade greedy population.</p>
 *
 */

public class Tester12 {
    static final double prize = 10.0;
    static final int N = 100;
    static final int max_gens = 1000;
    static final boolean reset1 = true;
    static final boolean displayRoundMessages = false;
    static final String results_csv="results.csv";
    static final String COMMA_DELIMITER = ",";
    static final String NEW_LINE_SEPARATOR = "\n";
    static final String tester = "12";
    static final int mutant_cluster_size = 5;
    static final double main_cluster_p = 0.01;
    static final double main_cluster_q = 0.01;
    static final double mutant_cluster_p = 0.5;
    static final double mutant_cluster_q = 0.5;

    public static void main(String[] args) {
        main1();
    }

    public static void main1(){
        System.out.println("Executing "+Thread.currentThread().getStackTrace()[1].getMethodName()+"()...");

        // start initialise population
        ArrayList<Player> pop = new ArrayList<>();

        for(int i=0; i < N - mutant_cluster_size; i++){
            pop.add(new Player(main_cluster_p, main_cluster_q));

        }
        for(int i=0; i < mutant_cluster_size; i++){
            pop.add(new Player(mutant_cluster_p, mutant_cluster_q));
        }
        // end initialise population

        int generation = 0;
        while(generation!=max_gens){

            //playUG UG...

            generation++;
        }

    }
}
