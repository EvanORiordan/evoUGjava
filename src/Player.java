import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Player class for instantiating player objects for different variants of the UG.
 */
public class Player {
    private static int count = 0; // class-wide attribute that helps assign player ID
    private int ID; // each player has a unique ID
    private double score; // amount of reward player has received from playing; i.e. this player's fitness
    private double p; // proposal value; real num within [0,1]
    private double q; // acceptance threshold value; real num within [0,1]
    private int games_played_in_total; // keep track of the total number of games this player has played
    private double EAP;  // EAP; used by [rand2013evolution]
    private static String neighbourhood_type; // neighbourhood type of this player
    private ArrayList<Player> neighbourhood; // this player's neighbourhood
    private int games_played_this_gen;
    private int[] position; // allows for dynamic assignment of position values regardless of number of dimensions
    private static double prize; // the prize amount being split in an interaction
    private static double loners_payoff; // payoff received for being part of an interaction where a party abstained
    private double old_p; // the p value held at the beginning of the gen; will be copied by imitators
    private boolean abstainer = false; // indicates whether this player is an abstainer; an abstainer always abstains
    // from playing the game, hence both parties receive the loner's payoff; default value: false
    private int role1_games_this_gen; // how many games this player has played so far this gen as role1
    private int role2_games_this_gen; // how many games this player has played so far this gen as role2
    private double average_score; // average score of this player this gen
    private final DecimalFormat df = new DecimalFormat("0.000000");


    public Player(){}  // empty constructor

    // constructor for instantiating a player.
    // if DG player, make sure to pass 0.0 double to q parameter.
    // if abstinence-less game, make sure to pass false boolean to abstainer parameter.
    public Player(double p, double q, boolean abstainer){
        ID=count++; // assign this player's ID
        this.p=p; // assign p value
        this.q=q; // assign q value
        this.abstainer=abstainer; // indicate whether this player initialises as an abstainer
    }

    // method for playing the UG
    public void playUG(Player responder) {
        if(p >= responder.q){ // if offer is satisfactory
            updateStats(prize*(1-p), true);
            responder.updateStats(prize*p, false);
        } else { // if offer is not satisfactory
            updateStats(0, true);
            responder.updateStats(0, false);
        }
    }

    // method for playing the DG
    public void playDG(Player recipient){
        updateStats(prize*(1-p), true);
        recipient.updateStats(prize*p, false);
    }

    // method for playing the UG with an abstinence option.
    // if the proposer or the responder is an abstainer, both parties receive the loner's payoff.
    // otherwise, play the regular UG.
    public void playAbstinenceUG(Player responder){
        if(abstainer || responder.abstainer){
//            score += loners_payoff;
//            responder.score += loners_payoff;
            updateStats(loners_payoff, true);
            responder.updateStats(loners_payoff, false);
        } else {
            playUG(responder);
        }
    }

    // method for playing the DG with an abstinence option.
    // if the proposer or the responder is an abstainer, both parties receive the loner's payoff.
    // otherwise, play the regular DG.
    public void playAbstinenceDG(Player recipient){
        if(abstainer || recipient.abstainer){
//            score += loners_payoff;
//            recipient.score += loners_payoff;
            updateStats(loners_payoff, true);
            recipient.updateStats(loners_payoff, false);
        } else {
            playDG(recipient);
        }
    }

    // method for playing the UG, as the proposer, with each neighbour
    public void playSpatialUG(){
        for(Player neighbour: neighbourhood){
            playUG(neighbour);
        }
    }

    // method for playing the UG, as the dictator, with each neighbour
    public void playSpatialDG(){
        for(Player neighbour: neighbourhood){
            playDG(neighbour);
        }
    }

    // method for playing the UG with an abstinence option, as the proposer, with each neighbour
    public void playSpatialAbstinenceUG(){
        for(Player neighbour: neighbourhood){
            playAbstinenceUG(neighbour);
        }
    }

    // method for playing the DG with an abstinence option, as the dictator, with each neighbour
    public void playSpatialAbstinenceDG(){
        for(Player neighbour: neighbourhood){
            playAbstinenceDG(neighbour);
        }
    }

    public double getScore(){
        return score;
    }

    public void setScore(double score){
        this.score=score;
    }

    public double getP(){
        return p;
    }

    // recall that p must lie within the range [0,1]
    public void setP(double p){
        this.p=p;
        if(this.p>1){
            this.p=1;
        } else if(this.p<0){
            this.p=0;
        }
    }

    public double getQ(){
        return q;
    }

    // recall that q must lie within the range [0,1]
    public void setQ(double q){
        this.q=q;
        if(this.q>1){
            this.q=1;
        } else if(this.q<0){
            this.q=0;
        }
    }

    // update the status of this player after playing, including score.
    public void updateStats(double payoff, boolean role1){
        score+=payoff;
        games_played_in_total++;
        games_played_this_gen++;
        if(role1){
            role1_games_this_gen++;
        } else{
            role2_games_this_gen++;
        }
        average_score = score / games_played_in_total;
    }

    public void setStrategy(double p, double q){
        setP(p);
        setQ(q);
    }

    public int getId(){
        return ID;
    }

    public double getEAP_rand2013evolution(){
        return EAP;
    }

    // Method for calculating a player's effective average payoff, according to [rand2013evolution].
    public void setEAP_rand2013evolution(double w){
        EAP = Math.exp(w * average_score);
    }

    public static void setNeighbourhoodType(String s){
        neighbourhood_type=s;
    }

    public void setGamesPlayedThisGen(int games_played_this_gen){
        this.games_played_this_gen = games_played_this_gen;
    }


    // method for assigning the position of a player on a 1D space and
    // finding the neighbours when a player resides on a 1D line space.
    public void findNeighbours1D(ArrayList<Player> line, int position_value1){
        position = new int[] {position_value1};
        neighbourhood = new ArrayList<>();
        if(neighbourhood_type.equals("line2")){
            int a=position[0];
            int b=line.size();
            neighbourhood.add(line.get(((a-1)%b+b)%b)); // (a%b+b)%b lets edge players reach other edge players
            neighbourhood.add(line.get(((a+1)%b+b)%b));
        }
    }

    // method for assigning the position of a player on a 2D space and
    // finding the neighbours when a player resides on a 2D space.
    // currently, this method handles programs using the von Neumann and the Moore neighbourhood types.
    public void findNeighbours2D(ArrayList<ArrayList<Player>> grid, int row_position, int column_position){
        position = new int[] {row_position, column_position};
        neighbourhood = new ArrayList<>();
        int a=position[0];
        int b=position[1];
        int c=grid.size();
        int d=grid.get(0).size();
        int up=((a-1)%c+c)%c; // go up one node (on the square grid)
        int down=((a+1)%c+c)%c; // down
        int left=((b-1)%d+d)%d; // left
        int right=((b+1)%d+d)%d; // right
        neighbourhood.add(grid.get(up).get((b%d+d)%d));
        neighbourhood.add(grid.get(down).get((b%d+d)%d));
        neighbourhood.add(grid.get((a%c+c)%c).get(left));
        neighbourhood.add(grid.get((a%c+c)%c).get(right));
        if(neighbourhood_type.equals("vonNeumann4")){
        } else if(neighbourhood_type.equals("moore8")){
            neighbourhood.add(grid.get(up).get(left)); // up-left
            neighbourhood.add(grid.get(up).get(right)); // up-right
            neighbourhood.add(grid.get(down).get(left)); // down-left
            neighbourhood.add(grid.get(down).get(right)); // down-right
        }
    }

    public static void setPrize(double d){
        prize=d;
    }

    public ArrayList<Player> getNeighbourhood() {
        return neighbourhood;
    }

    public static void setLoners_payoff(double d){
        loners_payoff=d;
    }

    public boolean getAbstainer(){
        return abstainer;
    }

    public double getAverage_score(){
        return average_score;
    }

    public double getOld_p(){
        return old_p;
    }

    public void setOld_p(double old_p){
        this.old_p=old_p;
    }




    @Override
    public String toString(){
        // comment out a variable if you don't want it to appear in the player description
        String description = "";
        description += "ID="+ID;
        description += " p="+df.format(p);
        description += " q="+df.format(q);
        description += " Abstainer="+abstainer;
        description += " Score="+df.format(score);
        description += " AS="+df.format(average_score);
//        description += " EAP="+ EAP;
        if(neighbourhood.size() != 0){
            description += " Neighbourhood=[";
            for(int i=0;i<neighbourhood.size();i++){
                description += neighbourhood.get(i).getId();
                if((i+1) < neighbourhood.size()){ // are there more neighbours?
                    description +=", ";
                }
            }
            description +="]";
        }
        description += " GPTG="+ games_played_this_gen;
        description += " GPIT="+games_played_in_total;
        description += " R1GTG="+role1_games_this_gen;
        description += " R2GTG="+role2_games_this_gen;
        return description;
    }




    // place BPs to debug and test Player method functionality using the simple test methods below.
    public static void main(String[] args) {
//        UGTest1();
//        UGTest2();
//        DGTest1();
//        DGTest2();
//        SpatialUGTest1();
//        SpatialUGTest2();
//        SpatialUGTest3();
//        SpatialDGTest1();
//        AbstinenceUGTest1();
//        SpatialAbstinenceUGTest1();
        SpatialAbstinenceDGTest1();
    }

    // basic UG test
    public static void UGTest1(){
        System.out.println("Executing "+Thread.currentThread().getStackTrace()[1].getClassName()+"."
                +Thread.currentThread().getStackTrace()[1].getMethodName()+"()...\n");
        Player.setPrize(1.0);
        Player player1 = new Player(
                ThreadLocalRandom.current().nextDouble(),
                ThreadLocalRandom.current().nextDouble(),
                false);
        Player player2 = new Player(
                ThreadLocalRandom.current().nextDouble(),
                ThreadLocalRandom.current().nextDouble(),
                false);
        player1.playUG(player2);
    }

    // UG test to see if the player stats are updated correctly
    public static void UGTest2(){
        System.out.println("Executing "+Thread.currentThread().getStackTrace()[1].getClassName()+"."
                +Thread.currentThread().getStackTrace()[1].getMethodName()+"()...\n");
        Player.setPrize(1.0);
        Player player1 = new Player(0.3,0.9999, false);
        Player player2 = new Player(0.0001, 0.2, false);
        player1.playUG(player2);
        System.out.println(player1.score == 0.7);
        System.out.println(player2.score == 0.3);
    }

    // basic DG test
    public static void DGTest1(){
        System.out.println("Executing "+Thread.currentThread().getStackTrace()[1].getClassName()+"."
                +Thread.currentThread().getStackTrace()[1].getMethodName()+"()...\n");
        Player.setPrize(1.0);
        Player player1 = new Player(ThreadLocalRandom.current().nextDouble(), 0.0, false);
        Player player2 = new Player(ThreadLocalRandom.current().nextDouble(), 0.0, false);
        player1.playDG(player2);
    }

    // DG test to see if the player stats are updated correctly
    public static void DGTest2(){
        System.out.println("Executing "+Thread.currentThread().getStackTrace()[1].getClassName()+"."
                +Thread.currentThread().getStackTrace()[1].getMethodName()+"()...\n");
        Player.setPrize(1.0);
        Player player1 = new Player(0.3, 0.0, false);
        Player player2 = new Player(0.2, 0.0, false);
        player1.playDG(player2);
        System.out.println(player1.score == 0.7);
        System.out.println(player2.score == 0.3);
    }

    // 1D line UG test
    public static void SpatialUGTest1(){
        System.out.println("Executing "+Thread.currentThread().getStackTrace()[1].getClassName()+"."
                +Thread.currentThread().getStackTrace()[1].getMethodName()+"()...\n");
        Player.setPrize(1.0);
        Player.setNeighbourhoodType("line2"); // 1D line neighbourhood with 2 neighbours
        int N=100;
        ArrayList<Player> line = new ArrayList<>();
        for(int i=0;i<N;i++){
            line.add(new Player(
                    ThreadLocalRandom.current().nextDouble(),
                    ThreadLocalRandom.current().nextDouble(),
                    false));
        }
        for(int i=0;i<N;i++){
            line.get(i).findNeighbours1D(line, i);
        }
        System.out.println("place a BP at this line of code!");
        for(Player player: line){
            player.playSpatialUG();
        }
        System.out.println("place a BP at this line of code!");
    }

    // 2D grid von Neumann neighbourhood UG test
    public static void SpatialUGTest2(){
        System.out.println("Executing "+Thread.currentThread().getStackTrace()[1].getClassName()+"."
                +Thread.currentThread().getStackTrace()[1].getMethodName()+"()...\n");
        Player.setPrize(1.0);
        Player.setNeighbourhoodType("vonNeumann4"); // von Neumann neighbourhood with 4 neighbours
        ArrayList<ArrayList<Player>> grid = new ArrayList<>();
        int rows=10;
        int columns=10;
        for(int i=0;i<rows;i++) {
            ArrayList<Player> row = new ArrayList<>();
            for (int j=0;j<columns;j++) {
                row.add(new Player(
                        ThreadLocalRandom.current().nextDouble(),
                        ThreadLocalRandom.current().nextDouble(),
                        false)); // no abstainers are present in the population
            }
            grid.add(row);
        }
        for(int i=0;i<rows;i++){
            for(int j=0;j<columns;j++){
                grid.get(i).get(j).findNeighbours2D(grid, i, j);
            }
        }
        System.out.println("place a BP at this line of code!");
        for(ArrayList<Player> row: grid){
            for(Player player: row){
                player.playSpatialUG();
            }
        }
        System.out.println("place a BP at this line of code!");
    }

    // 2D grid Moore neighbourhood UG test
    public static void SpatialUGTest3(){
        System.out.println("Executing "+Thread.currentThread().getStackTrace()[1].getClassName()+"."
                +Thread.currentThread().getStackTrace()[1].getMethodName()+"()...\n");
        Player.setPrize(1.0);
        Player.setNeighbourhoodType("moore8"); // moore neighbourhood with 8 neighbours
        ArrayList<ArrayList<Player>> grid = new ArrayList<>();
        int rows=10;
        int columns=10;
        for(int i=0;i<rows;i++) {
            ArrayList<Player> row = new ArrayList<>();
            for (int j=0;j<columns;j++) {
                row.add(new Player(
                        ThreadLocalRandom.current().nextDouble(),
                        ThreadLocalRandom.current().nextDouble(),
                        false)); // no abstainers are present in the population
            }
            grid.add(row);
        }
        for(int i=0;i<rows;i++){
            for(int j=0;j<columns;j++){
                grid.get(i).get(j).findNeighbours2D(grid, i, j);
            }
        }
        System.out.println("place a BP at this line of code!");
        for(ArrayList<Player> row: grid){
            for(Player player: row){
                player.playSpatialUG();
            }
        }
        System.out.println("place a BP at this line of code!");
    }

    // 2D grid von Neumann neighbourhood DG test
    public static void SpatialDGTest1(){
        System.out.println("Executing "+Thread.currentThread().getStackTrace()[1].getClassName()+"."
                +Thread.currentThread().getStackTrace()[1].getMethodName()+"()...\n");
        Player.setPrize(1.0);
        Player.setNeighbourhoodType("vonNeumann4"); // von Neumann neighbourhood with 4 neighbours
        ArrayList<ArrayList<Player>> grid = new ArrayList<>();
        int rows=10;
        int columns=10;
        for(int i=0;i<rows;i++) {
            ArrayList<Player> row = new ArrayList<>();
            for (int j=0;j<columns;j++) {
                row.add(new Player(ThreadLocalRandom.current().nextDouble(), 0.0, false));
            }
            grid.add(row);
        }
        for(int i=0;i<rows;i++){
            for(int j=0;j<columns;j++){
                grid.get(i).get(j).findNeighbours2D(grid, i, j);
            }
        }
        System.out.println("place a BP at this line of code!");
        for(ArrayList<Player> row: grid){
            for(Player player: row){
                player.playSpatialUG();
            }
        }
        System.out.println("place a BP at this line of code!");
    }

    // abstinence UG test
    public static void AbstinenceUGTest1(){
        System.out.println("Executing "+Thread.currentThread().getStackTrace()[1].getClassName()+"."
                +Thread.currentThread().getStackTrace()[1].getMethodName()+"()...\n");
        Player.setPrize(1.0);
        Player.setLoners_payoff(prize * 0.1);
        Player player1 = new Player( // regular player
                ThreadLocalRandom.current().nextDouble(),
                ThreadLocalRandom.current().nextDouble(),
                false);
        Player player2 = new Player( // abstainer
                ThreadLocalRandom.current().nextDouble(),
                ThreadLocalRandom.current().nextDouble(),
                true);
        player1.playAbstinenceUG(player2);
    }

    // 2D grid von Neumann neighbourhood UG abstinence test
    public static void SpatialAbstinenceUGTest1(){
        System.out.println("Executing "+Thread.currentThread().getStackTrace()[1].getClassName()+"."
                +Thread.currentThread().getStackTrace()[1].getMethodName()+"()...\n");
        Player.setPrize(1.0);
        Player.setLoners_payoff(prize * 0.1);
        Player.setNeighbourhoodType("vonNeumann4"); // von Neumann neighbourhood with 4 neighbours
        double abstainer_prob = 0.1; // the probability that a player initialises as an abstainer
        ArrayList<ArrayList<Player>> grid = new ArrayList<>();
        int rows=10;
        int columns=10;
        for(int i=0;i<rows;i++) {
            ArrayList<Player> row = new ArrayList<>();
            for (int j=0;j<columns;j++) {
                boolean abstainer = false;
                double random_double = ThreadLocalRandom.current().nextDouble();
                if(random_double < abstainer_prob){
                    abstainer=true;
                }
                row.add(new Player(
                        ThreadLocalRandom.current().nextDouble(),
                        ThreadLocalRandom.current().nextDouble(),
                        abstainer));
            }
            grid.add(row);
        }
        for(int i=0;i<rows;i++){
            for(int j=0;j<columns;j++){
                grid.get(i).get(j).findNeighbours2D(grid, i, j);
            }
        }
        for(ArrayList<Player> row: grid){
            for(Player player: row){
                player.playSpatialAbstinenceUG();
            }
        }
    }

    // 2D grid von Neumann neighbourhood DG abstinence test
    public static void SpatialAbstinenceDGTest1(){
        System.out.println("Executing "+Thread.currentThread().getStackTrace()[1].getClassName()+"."
                +Thread.currentThread().getStackTrace()[1].getMethodName()+"()...\n");
        Player.setPrize(1.0);
        Player.setLoners_payoff(prize * 0.1);
        Player.setNeighbourhoodType("vonNeumann4"); // von Neumann neighbourhood with 4 neighbours
        double abstainer_prob = 0.1; // the probability that a player initialises as an abstainer
        ArrayList<ArrayList<Player>> grid = new ArrayList<>();
        int rows=10;
        int columns=10;
        for(int i=0;i<rows;i++) {
            ArrayList<Player> row = new ArrayList<>();
            for (int j=0;j<columns;j++) {
                boolean abstainer = false;
                double random_double = ThreadLocalRandom.current().nextDouble();
                if(random_double < abstainer_prob){
                    abstainer=true;
                }
                row.add(new Player(ThreadLocalRandom.current().nextDouble(), 0.0, abstainer));
            }
            grid.add(row);
        }
        for(int i=0;i<rows;i++){
            for(int j=0;j<columns;j++){
                grid.get(i).get(j).findNeighbours2D(grid, i, j);
            }
        }
        for(ArrayList<Player> row: grid){
            for(Player player: row){
                player.playSpatialAbstinenceDG();
            }
        }
        System.out.println("hello");
    }

}
