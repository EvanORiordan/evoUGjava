
/**
 * <p>Player class for instantiating player objects for different variants of the UG.</p>
 */
public class Player {
    private static int count = 0; // class-wide attribute that helps assign player ID
    private int id; // each player has a unique ID
    private double score = 0; // amount of reward player has received from playing; i.e. this player's fitness
    private double p = 0.0; // proposal value; real num within [0,1]
    private double q = 0.0; // acceptance threshold value; real num within [0,1]
    private int games_played = 0; // keep track of the number of games this player has played
    private double EAP;  // EAP; used by [rand2013evolution]

    public Player(){}  // empty constructor

    // constructor for instantiating a UG player
    public Player(double p, double q){
        id=count++; // assign this player's ID
        // assign this player's strategy
        this.p=p;
        this.q=q;
    }

    // constructor for instantiating a DG player
    public Player(double p){
        id=count++; // assign this player's ID
        this.p=p; // assign this player's strategy
    }

    // method for playing the UG
    public void playUG(Player responder, double prize) {
        if(p >= responder.q){
            score += (prize*(1-p));
            responder.score += (prize*p);
        }
        games_played++;
        responder.games_played++;
    }

    // method for playing the DG
    public void playDG(Player recipient, double prize){
        score += (prize*(1-p));
        recipient.score += (prize*p);
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
        return id;
    }

    public double getEAP_rand2013evolution(){
        return EAP;
    }

    // Method for calculating a player's effective average payoff, according to [rand2013evolution].
    public void setEAP_rand2013evolution(double w){
        double average_payoff = score / games_played; // i.e. pi_i
        EAP = Math.exp(w * average_payoff);
    }

    @Override
    public String toString(){
        // Uncomment the player describing method you want to have display.

//        return toStringUG();
        return toStringRand2013();
//        return toStringDG();
    }

    // method for returning the description of a standard UG player
    public String toStringUG(){
        return "ID="+id+
                " p="+p+
                " q="+q+
                " Score="+score+
                " Games played="+games_played;
    }

    // method for returning the description of a [rand2013evolution] UG player
    public String toStringRand2013(){
        return "ID="+id+
                " p="+p+
                " q="+q+
                " Score="+score+
                " Games played="+games_played+
                " EAP="+ EAP;
    }

    // method for returning the description of a DG player
    public String toStringDG(){
        return "ID="+id+
                "\tp="+p+
                "\tScore="+score+
                "\tGames played="+games_played;
    }
}
