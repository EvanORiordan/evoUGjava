import java.util.ArrayList;

public class Player {

    //private static int count = 1;
    private static int count = 0;
    private int id;                     // each Player has a unique id

    private double score = 0;           // amount of reward player has received from playing
    private double effective_average_payoff;

    // p and q are real numbers that lie within the range [0, 1]
    private double p = 0.0;             // fraction of prize that player will offer to give to responder as proposer
    private double q = 0.0;             // minimum fraction of the prize that player will accept as responder

    private int how_many_players_ive_played_against = 1; // including This player himself
    private ArrayList<Integer> players_ive_played_against = new ArrayList<>(); // including This player himself

    public Player(){}
    public Player(double p, double q){
        id=count++;
        this.p=p;
        this.q=q;
        players_ive_played_against.add(id);
    }


    // NOTE: method overloading for the play() method
    public void play(Player responder, double prize) {
        if(p >= responder.q){
            System.out.println(p+" >= "+ responder.q+" Responder accepts offer." +
                    "\n\tProposer earns "+(prize*(1-p))+"\tResponder earns "+(prize*p));

            increaseScore(prize*(1-p));
            responder.increaseScore(prize*p);
        }
        if(p < responder.q){
            System.out.println(p+" <  "+ responder.q+" Responder declines offer.");
        }
//        adjustPlayerLists(responder);
    }
    public void play(Player responder, double prize, boolean displayMessages){
        if(displayMessages){
            play(responder, prize);
        } else{
            if(p >= responder.q) {
                increaseScore(prize * (1 - p));
                responder.increaseScore(prize * p);
            }
        }
//        adjustPlayerLists(responder);
    }
    public void play(Player responder, double prize, boolean displayMessages, double w){
        if(displayMessages){
            play(responder, prize);
        } else{
//            if(p >= responder.q) {
//                increaseScore(prize * (1 - p));
//                responder.increaseScore(prize * p);
//            }
            play(responder, prize, false);
        }
        calculateEffectiveAveragePayoff(w);
        adjustPlayerLists(responder);
        responder.calculateEffectiveAveragePayoff(w);
        responder.adjustPlayerLists(this);
    }

    public void adjustPlayerLists(Player other_player){
        players_ive_played_against.add(other_player.id);
        how_many_players_ive_played_against++;
//        responder.players_ive_played_against.add(id);
//        responder.how_many_players_ive_played_against++;
    }
    public void calculateEffectiveAveragePayoff(double w){
        effective_average_payoff = Math.exp(w*score);
    }

    public double getScore(){
        return score;
    }
    public void setScore(double score){
        this.score=score;
    }
    public void increaseScore(double amount){
        score+=amount;
    }
    public double getP(){
        return p;
    }
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
        return id;
    }
    public ArrayList<Integer> getPlayers_ive_played_against(){
        return players_ive_played_against;
    }
    public double getEffective_average_payoff(){
        return effective_average_payoff;
    }

    @Override
    public String toString(){
//        return "Player ID: "+id+"\tScore: "+score+"\tp: "+p+"\tq: "+q;
        return "ID="+id+
                " p="+p+
                " q="+q+
                " PPA: "+how_many_players_ive_played_against+
                " Score="+score+
                " EAP="+effective_average_payoff;
    }
}
