public class StorageObject1 {
    private double avg_p;
    private double highest_p;
    private double lowest_p;
    private int abstainers;

    public StorageObject1(double avg_p, double highest_p, double lowest_p, int abstainers){
        this.avg_p=avg_p;
        this.highest_p=highest_p;
        this.lowest_p=lowest_p;
        this.abstainers=abstainers;
    }

    public double getAvg_p() {
        return avg_p;
    }

    public double getHighest_p() {
        return highest_p;
    }

    public double getLowest_p() {
        return lowest_p;
    }

    public int getAbstainers() {
        return abstainers;
    }
}
