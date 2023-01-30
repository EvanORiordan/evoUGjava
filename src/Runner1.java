/**
 * Program for running multiple instances of an experiment program. Supports SpatialAbstinenceDG4,
 * SpatialAbstinenceDG5, SpatialAbstinenceDG6.
 */
public class Runner1 {
    public static void main(String[] args) {
        System.out.println("Executing "+Thread.currentThread().getStackTrace()[1].getClassName()+"."
                +Thread.currentThread().getStackTrace()[1].getMethodName()+"()...");

        double mean_avg_p = 0.0;
        double mean_highest_p = 0.0;
        double mean_lowest_p = 0.0;
        int mean_abstainers = 0;
        int runs=200;
        for(int i=0;i<runs;i++){
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
        System.out.println("Mean average value of p:\t"+Player.getDf().format(mean_avg_p)+
                        "\nMean highest value of p:\t"+Player.getDf().format(mean_highest_p)+
                        "\nMean lowest value of p:\t\t"+Player.getDf().format(mean_lowest_p)+
                        "\nMean number of abstainers:\t"+mean_abstainers);
    }
}
