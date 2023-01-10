import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * <p>Player class for instantiating player objects for different variants of the UG.</p>
 */
public class Player {
    private static int count = 0; // class-wide attribute that helps assign player ID
    private int ID; // each player has a unique ID
    private double score = 0; // amount of reward player has received from playing; i.e. this player's fitness
    private double p = 0.0; // proposal value; real num within [0,1]
    private double q = 0.0; // acceptance threshold value; real num within [0,1]
    private int games_played_in_total = 0; // keep track of the total number of games this player has played
    private double EAP;  // EAP; used by [rand2013evolution]
    private String neighbourhood_type; // neighbourhood type of this player
    private ArrayList<Player> neighbourhood; // contains the players in this player's neighbourhood
    private int max_games_per_gen;
    private int games_played_this_gen = 0;
    // allows for the dynamic assignment of position values regardless of the number of dimensions
    private int[] position;
    private static double prize; // the prize amount being split in an interaction

//    private int row_position;
//    private int column_position;


    public Player(){}  // empty constructor

    // constructor for instantiating a UG player
    public Player(double p, double q){
        ID=count++; // assign this player's ID
        // assign this player's strategy
        this.p=p;
        this.q=q;
    }

    // constructor for instantiating a DG player
    public Player(double p){
        ID=count++; // assign this player's ID
        this.p=p; // assign this player's strategy
    }

    //constructor for instantiating a spatial UG player
    public Player(double p, double q, String neighbourhood_type){
        ID=count++;
        this.p=p;
        this.q=q;
        this.neighbourhood_type=neighbourhood_type;
        neighbourhood = new ArrayList<>();
    }

    // constructor for instantiating a spatial DG player
    public Player(double p, String neighbourhood_type){
        ID=count++; // assign this player's ID
        this.p=p; // assign this player's strategy
        this.neighbourhood_type=neighbourhood_type;
        neighbourhood = new ArrayList<>();
    }

    // method for playing the UG
    public void playUG(Player responder) {
        if(p >= responder.q){
            score += (prize*(1-p));
            responder.score += (prize*p);
        }
        games_played_in_total++;
        responder.games_played_in_total++;
    }

    // method for playing the DG
    public void playDG(Player recipient){
        score += (prize*(1-p));
        recipient.score += (prize*p);
        games_played_in_total++;
        recipient.games_played_in_total++;
    }

    // method for playing the spatial UG. dictator role is randomly assigned
    public void playSpatialUG(){
        for(Player neighbour: neighbourhood){ // play spatial UG with each neighbour
            // do not play if you have reached your limit for this gen
            if(games_played_this_gen != max_games_per_gen
                    && neighbour.games_played_this_gen != max_games_per_gen){
                boolean rand_bool = ThreadLocalRandom.current().nextBoolean(); // assign roles
                if(rand_bool){
                    playUG(neighbour);
                } else {
                    neighbour.playUG(this);
                }
                games_played_this_gen++;
                neighbour.games_played_this_gen++;
            }
        }
    }

    // method for playing the spatial DG. dictator role is randomly assigned
    public void playSpatialDG(){
        for(Player neighbour: neighbourhood){
            if(games_played_this_gen != max_games_per_gen
                    && neighbour.games_played_this_gen != max_games_per_gen){
                boolean rand_bool = ThreadLocalRandom.current().nextBoolean();
                if(rand_bool){
                    playDG(neighbour);
                } else {
                    neighbour.playDG(this);
                }
                games_played_this_gen++;
                neighbour.games_played_this_gen++;
            }
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

    public void setStrategy(double p, double q){
        this.p=p;
        this.q=q;
    }

    public int getId(){
        return ID;
    }

    public double getEAP_rand2013evolution(){
        return EAP;
    }

    // Method for calculating a player's effective average payoff, according to [rand2013evolution].
    public void setEAP_rand2013evolution(double w){
        double average_payoff = score / games_played_in_total; // i.e. pi_i
        EAP = Math.exp(w * average_payoff);
    }

    public String getNeighbourhoodType(){
        return neighbourhood_type;
    }

    public void setGamesPlayedThisGen(int games_played_this_gen){
        this.games_played_this_gen = games_played_this_gen;
    }


    // method for finding the neighbours when a player resides on a 1D line space
    public void findNeighbours1D(ArrayList<Player> line){
        if(neighbourhood_type.equals("line2")){
            max_games_per_gen = 2;
            int a=position[0];
            int b=line.size();
            neighbourhood.add(line.get(((a-1)%b+b)%b)); // (a%b+b)%b lets edge players reach other edge players
            neighbourhood.add(line.get(((a+1)%b+b)%b));
        }
    }

    // method for finding the neighbours when a player resides on a 2D space
    public void findNeighbours2D(ArrayList<ArrayList<Player>> grid){
        if(neighbourhood_type.equals("vonNeumann4")){
            max_games_per_gen = 4;
            int a=position[0];
            int b=position[1];
            int c=grid.size();
            int d=grid.get(0).size();
            neighbourhood.add(grid.get(((a-1)%c+c)%c).get((b%d+d)%d));
            neighbourhood.add(grid.get(((a+1)%c+c)%c).get((b%d+d)%d));
            neighbourhood.add(grid.get((a%c+c)%c).get(((b-1)%d+d)%d));
            neighbourhood.add(grid.get((a%c+c)%c).get(((b+1)%d+d)%d));
        } else if(neighbourhood_type.equals("moore8")){
//            max_games_per_gen = 8;
            // play spatial UG with respect to the moore neighbourhood...
        }
    }

    // method for assigning the position of a player on a 1D space
    public void assignPosition1D(int position_value1){
        position = new int[] {position_value1};
    }

    // method for assigning the position of a player on a 2D space
    public void assignPosition2D(int row_position, int column_position){
        position = new int[] {row_position, column_position};
    }

    public static void setPrize(double d){
        prize=d;
    }

    public ArrayList<Player> getNeighbourhood() {
        return neighbourhood;
    }



    @Override
    public String toString(){
        // Uncomment the player-describing method you want to have display.
//        return toStringUG();
//        return toStringRand2013();
//        return toStringDG();
//        return toStringSpatialUG();
        return toStringSpatialDG();
    }

    // method for returning the description of a standard UG player
    public String toStringUG(){
        return "ID="+ID+
                " p="+p+
                " q="+q+
                " Score="+score+
                " GPIT="+games_played_in_total;
    }

    // method for returning the description of a [rand2013evolution] UG player
    public String toStringRand2013(){
        return "ID="+ID+
                " p="+p+
                " q="+q+
                " Score="+score+
                " GPIT="+games_played_in_total+
                " EAP="+ EAP;
    }

    // method for returning the description of a DG player
    public String toStringDG(){
        return "ID="+ID+
                " p="+p+
                " Score="+score+
                " GPIT="+games_played_in_total;
    }

    // method for returning the description of a spatial UG player
    public String toStringSpatialUG(){
        String neighbours = "[";
        int j=0;
        for(int i=0;i<neighbourhood.size();i++){
            neighbours+=neighbourhood.get(i).getId();
            j++;
            if(j < neighbourhood.size()){
                neighbours+=", ";
            }
        }
        neighbours+="]";
        return "ID="+ID+
                " p="+p+
                " q="+q+
                " Score="+score+
                " Neighbourhood="+neighbours+
                " GPTG="+ games_played_this_gen +
                " GPIT="+games_played_in_total;
    }

    public String toStringSpatialDG(){
        String neighbours = "[";
        int j=0;
        for(int i=0;i<neighbourhood.size();i++){
            neighbours+=neighbourhood.get(i).getId();
            j++;
            if(j < neighbourhood.size()){
                neighbours+=", ";
            }
        }
        neighbours+="]";
        return "ID="+ID+
                " p="+p+
                " Score="+score+
                " Neighbourhood="+neighbours+
                " GPTG="+ games_played_this_gen +
                " GPIT="+games_played_in_total;
    }



    // place BPs to debug and test Player method functionality
    public static void main(String[] args) {
        test1();
//        test2();
    }
    public static void test1(){
        System.out.println("Executing "+Thread.currentThread().getStackTrace()[1].getClassName()+"."
                +Thread.currentThread().getStackTrace()[1].getMethodName()+"()...\n");
        Player.setPrize(1.0);
        Player player1 = new Player(ThreadLocalRandom.current().nextDouble(),ThreadLocalRandom.current().nextDouble());
        Player player2 = new Player(ThreadLocalRandom.current().nextDouble(),ThreadLocalRandom.current().nextDouble());
        player1.playUG(player2);
    }
    public static void test2(){
        System.out.println("Executing "+Thread.currentThread().getStackTrace()[1].getClassName()+"."
                +Thread.currentThread().getStackTrace()[1].getMethodName()+"()...\n");
        Player.setPrize(1.0);
        Player player1 = new Player(ThreadLocalRandom.current().nextDouble());
        Player player2 = new Player(ThreadLocalRandom.current().nextDouble());
        player1.playDG(player2);
    }
}
