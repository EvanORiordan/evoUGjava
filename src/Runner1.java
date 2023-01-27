/**
 * Program for running multiple instances of an experiment program. Supports SpatialAbstinenceDG4,
 * SpatialAbstinenceDG5, SpatialAbstinenceDG6.
 */
public class Runner1 {
    public static void main(String[] args) {
        System.out.println("Executing "+Thread.currentThread().getStackTrace()[1].getClassName()+"."
                +Thread.currentThread().getStackTrace()[1].getMethodName()+"()...\n");

        double mean_avg_p = 0.0; // change to mean avg p
        double mean_highest_p = 0.0;
        double mean_lowest_p = 0.0;
        int mean_abstainers = 0;
        int runs=5;
        for(int i=0;i<runs;i++){
//            System.out.println("Run "+i);
            SpatialAbstinenceDG6 run = new SpatialAbstinenceDG6();
            run.start();
            StorageObject1 so = run.giveStats();
            mean_avg_p+=so.getAvg_p();
            mean_highest_p+=so.getHighest_p();
            mean_lowest_p+=so.getLowest_p();
            mean_abstainers+=so.getAbstainers();
        }
        mean_avg_p /= runs;
        mean_highest_p /= runs;
        mean_lowest_p /= runs;
        mean_abstainers /= runs;
        System.out.println("Average value of p:\t\t\t"+Player.getDf().format(mean_avg_p)+
                        "\nAverage highest value of p:\t"+Player.getDf().format(mean_highest_p)+
                        "\nAverage lowest value of p:\t"+Player.getDf().format(mean_lowest_p)+
                        "\nAverage number of abstainers: "+Player.getDf().format(mean_abstainers));
    }
}
