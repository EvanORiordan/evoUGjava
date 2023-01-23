//import java.util.ArrayList;
//
///**
// * Idea from the to do list: Fair mutants invade greedy population.
// * INCOMPLETE.
// */
//public class EvoUG7 {
//    static final double prize = 10.0;
//    static final int N = 100;
//    static final int max_gens = 1000;
//    static final int mutant_cluster_size = 5;
//    static final double main_cluster_p = 0.01;
//    static final double main_cluster_q = 0.01;
//    static final double mutant_cluster_p = 0.5;
//    static final double mutant_cluster_q = 0.5;
//
//    public static void main(String[] args) {
//        System.out.println("Executing "+Thread.currentThread().getStackTrace()[1].getMethodName()+"()...");
//
//        Player.setPrize(prize);
//        ArrayList<Player> pop = new ArrayList<>();
//        for(int i=0; i < N - mutant_cluster_size; i++){
//            pop.add(new Player(main_cluster_p, main_cluster_q));
//        }
//        for(int i=0; i < mutant_cluster_size; i++){
//            pop.add(new Player(mutant_cluster_p, mutant_cluster_q));
//        }
//
//        int generation = 0;
//        while(generation!=max_gens){
//
//            //play UG...
//
//            //evolve...
//
//            generation++;
//        }
//    }
//}
