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
    private static double prize; // the prize amount being split in an interaction
    private static double loners_payoff; // payoff received for being part of an interaction where a party abstained
    private double old_p; // the p value held at the beginning of the gen; will be copied by imitators
    private double old_q; // the q value held at the beginning of the gen; will be copied by imitators
    private boolean old_abstainer; // the abstainer value held at start of gen; to be copied by imitators
    private boolean abstainer; // indicates whether this player is an abstainer; an abstainer always abstains
    // from playing the game, hence both interacting parties receive the loner's payoff.
    private int role1_games; // how many games this player has played as role1
    private int role2_games; // how many games this player has played as role2
    private double average_score; // average score of this player this gen
    private static DecimalFormat df = new DecimalFormat("0.00"); // format for printing doubles
    private static double edge_decay_factor; // EDF affects the rate of edge decay
    private double edge_decay_score; // EDS determines this player's probability of edge decay

    // 29/3/23: facilitates the selection of a player who has not been a dictator yet in a given gen.
    private boolean selected = false;

    // 29/3/23: tracks which players a player has left to play in a given gen.
    private ArrayList<Player> players_left_to_play_this_gen;



    public Player(){}  // empty constructor

    // constructor for instantiating a player.
    // if DG player, make sure to pass 0.0 double to q parameter.
    // if abstinence-less game, make sure to pass false boolean to abstainer parameter.
    public Player(double p, double q, boolean abstainer){
        ID=count++; // assign this player's ID
        this.p=p; // assign p value
        this.q=q; // assign q value
        this.abstainer=abstainer; // indicate whether this player initialises as an abstainer
        old_p=p;

        // edge decay mechanism: calculate initial EDS based on initial value of p
        edge_decay_score = edge_decay_factor * (1 / p);
//        System.out.println("p="+p+"\tEDS="+edge_decay_score);
    }

    // method for playing the UG.
    // if DG player, the offer is always accepted since the responder/recipient/role2 player has q=0.0.
    public void playUG(Player responder) {
        if(p >= responder.q){ // accept offer
            updateStats(prize*(1-p), true);
            responder.updateStats(prize*p, false);
        } else { // reject offer
            updateStats(0, true);
            responder.updateStats(0, false);
        }
    }

    // method for playing the UG with an abstinence option.
    // if the proposer or the responder is an abstainer, both parties receive the loner's payoff.
    // otherwise, play the regular UG.
    public void playAbstinenceUG(Player responder){
        if(abstainer || responder.abstainer){
            updateStats(loners_payoff, true);
            responder.updateStats(loners_payoff, false);
        } else {
            playUG(responder);
        }
    }

    // method for playing the UG, as the proposer, with each neighbour
    public void playSpatialUG(){
        for(Player neighbour: neighbourhood){
            playUG(neighbour);
        }
    }

    // method for playing the UG with an abstinence option, as the proposer, with each neighbour
    public void playSpatialAbstinenceUG(){
        for(Player neighbour: neighbourhood){
            playAbstinenceUG(neighbour);
        }
    }


    // method for playing the game asymmetrically, spatially and with abstinence.
    public void playAsymmSpatialAbstinenceUG(){
//        for(Player neighbour: neighbourhood){
//            if(games_played_this_gen != 4 || neighbour.games_played_this_gen != 4) {
//                playAbstinenceUG(neighbour);
//            }


//        for(int i=0;i<players_left_to_play_this_gen.size();i++){
//            Player neighbour = players_left_to_play_this_gen.get(i);
//            playAbstinenceUG(neighbour);
//
//            // remove this player from neighbour's players left to play this gen.
//            neighbour.players_left_to_play_this_gen.remove(this);
//        }

        for(Player neighbour: players_left_to_play_this_gen){
            playAbstinenceUG(neighbour);
            neighbour.players_left_to_play_this_gen.remove(this);
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

    /**
     * Update the status of this player after playing, including score and average score.
     * The average score calculation is usual for seeing what score a player accrued over
     * the gen.
     *
     * 6/3/23: I have changed this to dividing by games_played_this_gen to games_played_in_total.
     */
    public void updateStats(double payoff, boolean role1){
        score+=payoff;
        games_played_in_total++;
        games_played_this_gen++;
        if(role1){
            role1_games++;
        } else{
            role2_games++;
        }
        average_score = score / games_played_this_gen;
    }

    // copy another player's strategy.
    public void copyStrategy(Player model){
        setP(model.old_p);
        setQ(model.old_q);
        abstainer=model.old_abstainer;
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

    public static String getNeighbourhoodType(){
        return neighbourhood_type;
    }

    public static void setNeighbourhoodType(String s){
        neighbourhood_type=s;
    }

    public void setGamesPlayedThisGen(int games_played_this_gen){
        this.games_played_this_gen = games_played_this_gen;
    }


    // method for assigning the position of a player on a 1D space and
    // finding the neighbours when a player resides on a 1D line space.
    public void findNeighbours1D(ArrayList<Player> line, int position){
        neighbourhood = new ArrayList<>();
        if(neighbourhood_type.equals("line2")){
            int a=position;
            int b=line.size();
            neighbourhood.add(line.get(((a-1)%b+b)%b)); // (a%b+b)%b lets edge players reach other edge players
            neighbourhood.add(line.get(((a+1)%b+b)%b));
        }
    }

    // method for assigning the position of a player on a 2D space and
    // finding the neighbours when a player resides on a 2D space.
    // currently, this method handles programs using the von Neumann and the Moore neighbourhood types.
    // possible neighbourhood_type values: VN; M
    public void findNeighbours2D(ArrayList<ArrayList<Player>> grid, int row_position, int column_position){
        neighbourhood = new ArrayList<>();
        int a=row_position;
        int b=column_position;
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
        if(neighbourhood_type.equals("M")){
            neighbourhood.add(grid.get(up).get(left)); // up-left
            neighbourhood.add(grid.get(up).get(right)); // up-right
            neighbourhood.add(grid.get(down).get(left)); // down-left
            neighbourhood.add(grid.get(down).get(right)); // down-right
        }
    }

    /**
     * If threshold is overcome by an edge_decay_score, an edge is decayed.
     */
    public void edgeDecay(){
        if (!abstainer) { // only non-abstainers may remove edges
            ArrayList<Player> neighbourhood_copy = (ArrayList<Player>) neighbourhood.clone();
            for (Player neighbour : neighbourhood_copy) {
                if(neighbourhood.size() == 1 || neighbour.neighbourhood.size() == 1){
                    break;
                }
                double threshold = ThreadLocalRandom.current().nextDouble(); // should threshold be random
                if (threshold < neighbour.edge_decay_score) {
                    neighbourhood.remove(neighbour);
                    neighbour.neighbourhood.remove(this);
                }
            }
        }
    }

    public static double getPrize(){
        return prize;
    }

    public static void setPrize(double d){
        prize=d;
    }

    public ArrayList<Player> getNeighbourhood() {
        return neighbourhood;
    }

    public static double getLoners_payoff(){
        return loners_payoff;
    }

    public static void setLoners_payoff(double d){
        loners_payoff=d;
    }

    public boolean getAbstainer(){
        return abstainer;
    }

    public void setAbstainer(boolean abstainer){
        this.abstainer=abstainer;
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

    public double getOld_q(){
        return old_q;
    }

    public void setOld_q(double old_q){
        this.old_q=old_q;
    }

    public boolean getOldAbstainer(){
        return old_abstainer;
    }

    public void setOldAbstainer(boolean old_abstainer){
        this.old_abstainer=old_abstainer;
    }

    public static DecimalFormat getDf(){
        return df;
    }

    public static double getEdge_decay_factor(){
        return edge_decay_factor;
    }

    public static void setEdge_decay_factor(double d){
        edge_decay_factor = d;
    }

    public double getEdge_decay_score(){
        return edge_decay_score;
    }

    public void setEdge_decay_score(double d){
        edge_decay_score=d;
    }

    public boolean getSelected(){
        return selected;
    }

    public void setSelected(boolean b){
        selected=b;
    }

    public void setPlayers_left_to_play_this_gen(ArrayList<Player> al){
        players_left_to_play_this_gen=al;
    }





    @Override
    public String toString(){
        // comment out a variable if you don't want it to appear in the player description when debugging
        String description = "";
        description += "ID="+ID;
        description += " p="+df.format(p);
        description += " old p="+df.format(old_p);
//        description += " q="+df.format(q);
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
//        description += " GPTG="+ games_played_this_gen;
//        description += " GPIT="+games_played_in_total;
//        description += " R1G="+role1_games;
//        description += " R2G="+role2_games;
        return description;
    }




    // place BPs to debug and test Player method functionality using the simple test methods below.
    public static void main(String[] args) {
        System.out.println("Executing "+Thread.currentThread().getStackTrace()[1].getClassName()+"."
                +Thread.currentThread().getStackTrace()[1].getMethodName()+"()...\n");
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
        player1.playUG(player2);
    }

    // DG test to see if the player stats are updated correctly
    public static void DGTest2(){
        System.out.println("Executing "+Thread.currentThread().getStackTrace()[1].getClassName()+"."
                +Thread.currentThread().getStackTrace()[1].getMethodName()+"()...\n");
        Player.setPrize(1.0);
        Player player1 = new Player(0.3, 0.0, false);
        Player player2 = new Player(0.2, 0.0, false);
//        player1.playDG(player2);
        player1.playUG(player2);
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
        for(Player player: line){
            player.playSpatialUG();
        }
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
        for(ArrayList<Player> row: grid){
            for(Player player: row){
                player.playSpatialUG();
            }
        }
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
        for(ArrayList<Player> row: grid){
            for(Player player: row){
                player.playSpatialUG();
            }
        }
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
        for(ArrayList<Player> row: grid){
            for(Player player: row){
                player.playSpatialUG();
            }
        }
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
                player.playSpatialAbstinenceUG();
            }
        }
    }
}
