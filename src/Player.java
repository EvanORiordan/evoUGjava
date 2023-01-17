import java.sql.Array;
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
    private static String neighbourhood_type; // neighbourhood type of this player
    private ArrayList<Player> neighbourhood = new ArrayList<>(); // this player's neighbourhood
    private int max_games_per_gen;
    private int games_played_this_gen = 0;
    private int[] position; // allows for dynamic assignment of position values regardless of number of dimensions
    private static double prize; // the prize amount being split in an interaction

    // variables pertaining to abstinence
    private static double baseAbstainProb; // base probability that a player abstains
    private static double abstainThreshold; // the benchmark required to reach to abstain
    private static double loners_payoff; // payoff received for being part of an interaction where a party abstained

    // players that this player abstains from playing with
    private ArrayList<Player> abstainList = new ArrayList<>();



    public Player(){}  // empty constructor

    // constructor for instantiating a UG player
    public Player(double p, double q){
        ID=count++; // assign this player's ID
        this.p=p; // assign p value
        this.q=q; // assign q value
    }

    // constructor for instantiating a DG player
    public Player(double p){
        ID=count++;
        this.p=p;
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

    // method for playing the UG with an abstinence option
    // firstly check if proposal is acceptable
    // else if the offer was not satisfactory, there is a chance to abstain where the players receive a loner's payoff
    // else the responder rejects the offer and neither party receive a payoff
    public void playAbstinenceUG(Player responder) {
        double a = baseAbstainProb * p;
        double b = abstainThreshold * responder.q;
        if (p >= responder.q) {
            score += (prize * (1 - p));
            responder.score += (prize * p);
        } else if (a > b) {
            score += loners_payoff;
            responder.score += loners_payoff;
        }
        games_played_in_total++;
        responder.games_played_in_total++;
    }

    // different mechanism for determining probability to abstain than that of playAbstinenceUG().
    // if offer is unsatisfactory, responder may abstain.
    // the worse the offer was, the more likely they abstain.
    public void playAbstinenceUG2(Player responder) {
        double rand_double = ThreadLocalRandom.current().nextDouble();
        double difference = responder.q - p; // the greater the difference, the greater the chance to abstain
        if (p >= responder.q) {
            score += (prize * (1 - p));
            responder.score += (prize * p);
        } else if (rand_double < difference) {
            score += loners_payoff;
            responder.score += loners_payoff;
        }
        games_played_in_total++;
        responder.games_played_in_total++;
    }

    // another difference abstain mechanism.
    // if the amount offered to the responder is worse than the loner's payoff, the responder abstains.
    // this does mean that if a responder has a lower acceptance threshold than the loner's payoff,
    // they will never abstain.
    public void playAbstinenceUG3(Player responder) {
        double amount_offered_to_responder = p * prize;
        if (p >= responder.q) {
            score += (prize * (1 - p));
            responder.score += (prize * p);
        } else if (amount_offered_to_responder < loners_payoff) {
            score += loners_payoff;
            responder.score += loners_payoff;
        }
        games_played_in_total++;
        responder.games_played_in_total++;
    }

    // method for playing the spatial UG with the option of abstinence. uses playAbstinenceUG2()
    public void playAbstinenceSpatialUG(){
        for(Player neighbour: neighbourhood){
            if(games_played_this_gen != max_games_per_gen
                    && neighbour.games_played_this_gen != max_games_per_gen){
                boolean rand_bool = ThreadLocalRandom.current().nextBoolean();
                if(rand_bool){
                    playAbstinenceUG2(neighbour);
                } else {
                    neighbour.playAbstinenceUG2(this);
                }
                games_played_this_gen++;
                neighbour.games_played_this_gen++;
            }
        }
    }

    // abstinence spatial UG method that uses playAbstinenceUG3().
    public void playAbstinenceSpatialUG2(){
        for(Player neighbour: neighbourhood){
            if(games_played_this_gen != max_games_per_gen
                    && neighbour.games_played_this_gen != max_games_per_gen){
                boolean rand_bool = ThreadLocalRandom.current().nextBoolean();
                if(rand_bool){
                    playAbstinenceUG3(neighbour);
                } else {
                    neighbour.playAbstinenceUG3(this);
                }
                games_played_this_gen++;
                neighbour.games_played_this_gen++;
            }
        }
    }

    // method for playing the abstinence spatial DG
    public void playAbstinenceSpatialDG(){
        for(Player neighbour: neighbourhood){
            if(games_played_this_gen != max_games_per_gen
                    && neighbour.games_played_this_gen != max_games_per_gen){
                boolean rand_bool = ThreadLocalRandom.current().nextBoolean();
                if(rand_bool){
                    playAbstinenceDG(neighbour);
                } else {
                    neighbour.playAbstinenceDG(this);
                }
                games_played_this_gen++;
                neighbour.games_played_this_gen++;
            }
        }
    }

    // method for playing the DG with abstinence.
    // if a dictator offers a recipient an offer that is less than the loner's payoff,
    // that dictator is placed on that recipient's abstain list.
    // from then on, if that recipient player is receiving from that dictator player,
    // the recipient abstains.
    // should abstaining mean that you refuse to play with a player entirely, or just that you
    // don't want to play if you are the recipient?
    // currently, it implies the latter.
    public void playAbstinenceDG(Player recipient){
        for(Player player: recipient.abstainList){
            if(ID == player.ID){
                score += loners_payoff;
                recipient.score += loners_payoff;
                return;
            }
        }
        playDG(recipient);
        if(loners_payoff > (prize * this.p)){
            if(recipient.abstainList == null){
                recipient.abstainList = new ArrayList<>();
            }
            recipient.abstainList.add(this);
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
        double average_payoff = score / games_played_in_total; // i.e. pi_i
        EAP = Math.exp(w * average_payoff);
    }

    public static void setNeighbourhoodType(String s){
        neighbourhood_type=s;
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

    // method for finding the neighbours when a player resides on a 2D space.
    // currently, this method handles programs using the von Neumann and the Moore neighbourhood types.
    public void findNeighbours2D(ArrayList<ArrayList<Player>> grid){
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
            max_games_per_gen = 4;
        } else if(neighbourhood_type.equals("moore8")){
            max_games_per_gen = 8;
            neighbourhood.add(grid.get(up).get(left)); // up-left
            neighbourhood.add(grid.get(up).get(right)); // up-right
            neighbourhood.add(grid.get(down).get(left)); // down-left
            neighbourhood.add(grid.get(down).get(right)); // down-right
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

    public static void setBaseAbstainProb(double d){
        baseAbstainProb=d;
    }

    public static void setAbstainThreshold(double d){
        abstainThreshold=d;
    }

    public static void setLoners_payoff(double d){
        loners_payoff=d;
    }



    @Override
    public String toString(){
        // Uncomment the player-describing method you want to have display.
//        return toStringUG();
//        return toStringRand2013();
//        return toStringDG();
        return toStringSpatialUG();
//        return toStringSpatialDG();
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



    // place BPs to debug and test Player method functionality using the simple test methods below.
    public static void main(String[] args) {
//        test1();
//        test2();
//        abstainUGTest1();
        abstainUGTest2();
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
    public static void abstainUGTest1(){
        System.out.println("Executing "+Thread.currentThread().getStackTrace()[1].getClassName()+"."
                +Thread.currentThread().getStackTrace()[1].getMethodName()+"()...\n");
        double local_prize = 1.0;
        Player.setPrize(prize);
        Player.setBaseAbstainProb(50.0);
        Player.setAbstainThreshold(25.0);
        Player.setLoners_payoff(local_prize * 0.1);
        Player player1 = new Player(0.3, 0.01);
        Player player2 = new Player(0.3, 0.40);
        player1.playAbstinenceUG(player2);
    }
    public static void abstainUGTest2(){
        System.out.println("Executing "+Thread.currentThread().getStackTrace()[1].getClassName()+"."
                +Thread.currentThread().getStackTrace()[1].getMethodName()+"()...\n");
        double local_prize = 1.0;
        Player.setPrize(prize);
        Player.setLoners_payoff(local_prize * 0.1);
        Player player1 = new Player(0.3, 0.01);
        Player player2 = new Player(0.3, 0.40);
        player1.playAbstinenceUG2(player2);
    }
}
