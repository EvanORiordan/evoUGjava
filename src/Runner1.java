public class Runner1 {
    public static void main(String[] args) {
        System.out.println("Executing "+Thread.currentThread().getStackTrace()[1].getClassName()+"."
                +Thread.currentThread().getStackTrace()[1].getMethodName()+"()...\n");

        double avg_avg_p = 0.0; // change to mean avg p
        double avg_highest_p = 0.0;
        double avg_lowest_p = 0.0;
        int avg_abstainers = 0;
        int runs=5;
        for(int i=0;i<runs;i++){
            System.out.println("Run "+i);
            SpatialAbstinenceDG5 run = new SpatialAbstinenceDG5();
            run.start();
            StorageObject1 so = run.gatherStats();
            avg_avg_p+=so.getAvg_p();
            avg_highest_p+=so.getHighest_p();
            avg_lowest_p+=so.getLowest_p();
            avg_abstainers+=so.getAbstainers();
        }
        avg_avg_p /= runs;
        avg_highest_p /= runs;
        avg_lowest_p /= runs;
        avg_abstainers /= runs;
        System.out.println("Average value of p:\t\t\t"+Player.getDf().format(avg_avg_p)+
                        "\nAverage highest value of p:\t"+Player.getDf().format(avg_highest_p)+
                        "\nAverage lowest value of p:\t"+Player.getDf().format(avg_lowest_p)+
                        "\nAverage number of abstainers: "+Player.getDf().format(avg_abstainers));
    }
}
