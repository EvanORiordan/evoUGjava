import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Programmed by Evan O'Riordan.
 *
 * Player class for instantiating player objects for different variants of the UG.
 */
public class Player {

    private static int count = 0; // class-wide attribute that helps assign player ID
    private int ID; // each player has a unique ID
    private double score; // amount of reward player has received from playing; i.e. this player's fitness
    private double p; // proposal value; real num within [0,1]
    private double q; // acceptance threshold value; real num within [0,1]
    private int games_played_in_total; // keep track of the total number of games this player has played
    private static String neighbourhood_type; // neighbourhood type of this player
    private ArrayList<Player> neighbourhood; // this player's neighbourhood
    private int games_played_this_gen;
    private static double prize; // the prize amount being split in an interaction
    private static double loners_payoff; // payoff received for being part of an interaction where a party abstained
    private double old_p; // the p value held at the beginning of the gen; will be copied by imitators
    private double old_q; // the q value held at the beginning of the gen; will be copied by imitators
    private boolean old_abstainer; // the abstainer value held at start of gen; to be copied by imitators
    private boolean abstainer; // indicates whether this player is an abstainer; an abstainer always abstains from playing the game, hence both interacting parties receive the loner's payoff.
    private int role1_games; // how many games this player has played as role1
    private int role2_games; // how many games this player has played as role2
    private double average_score; // average score of this player this gen
    private static DecimalFormat DF1 = new DecimalFormat("0.0"); // 1 decimal point DecimalFormat
    private static DecimalFormat DF4 = new DecimalFormat("0.0000"); // 4 decimal point DecimalFormat
    private static double edge_decay_factor; // EDF affects the rate of edge decay
    private double edge_decay_score; // EDS determines this player's probability of edge decay
    private ArrayList<Player> players_left_to_play_this_gen; // tracks which players a player has left to play in a given gen.
    private double[] edge_weights; // stores weights of edges belonging to the player.
    private static double rate_of_change; // fixed amount by which edge weight is modified.





    public Player(){} // empty constructor

    /**
     * constructor for instantiating a player.
     * if DG, make sure to pass 0.0 double as q argument.
     * if abstinence-less game, make sure to pass false boolean to abstainer parameter.
     *
     * @param p
     * @param q
     * @param abstainer
     */
    public Player(double p, double q, boolean abstainer){
        ID=count++; // assign this player's ID
        this.p=p; // assign p value
        this.q=q; // assign q value
        this.abstainer=abstainer; // indicate whether this player initialises as an abstainer
        old_p=p;
    }


    /**
     * method for playing the UG.
     * receives a Player argument to play with.
     * if DG, the offer is always accepted since the responder/recipient/role2 player has q=0.0.
     *
     * @param responder
     */
    public void playUG(Player responder) {
        if(p >= responder.q){ // accept offer
            updateStats(prize*(1-p), true);
            responder.updateStats(prize*p, false);
        } else { // reject offer
            updateStats(0, true);
            responder.updateStats(0, false);
        }
    }


    /**
     * method for playing the UG with an abstinence option.
     * if the proposer or the responder is an abstainer, both parties receive the loner's payoff.
     * otherwise, play the regular UG.
     *
     * @param responder
     */
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


    /**
     * Method for playing with space and abstinence alongside edge weights.
     *
     * Works alongside edgeDecay2().
     *
     * If either player is an abstainer, the interaction goes ahead regardless of the edge weight.
     * Else, when the player is the dictator, the neighbour's edge weight determines the chance of
     * interaction of receiving from the dictator. Else if the neighbour's edge weight beats the
     * double, you get to play with them. Else, the neighbour does not even arrive at the table to
     * play with the dictator, hence neither player gets any payoff.
     */
    public void playEdgeDecaySpatialAbstinenceUG(){
        for(int i=0;i<neighbourhood.size();i++){
            Player neighbour = neighbourhood.get(i);
            double random_double = ThreadLocalRandom.current().nextDouble();

            // ensures that the correct edge weight is retrieved from the neighbour
            double edge_weight = 0.0;

            for(int j=0;j<neighbour.neighbourhood.size();j++){
                if(neighbour.getNeighbourhood().get(j).getId() == ID){
                    edge_weight = neighbour.getEdge_weights()[j];
                    break;
                }
            }
            if(edge_weight > random_double || abstainer || neighbour.getAbstainer()){
                playAbstinenceUG(neighbour);
            }
        }
    }


    /**
     * Play the game with respect to space and edge weights.
     *
     * For each neighbour in your neighbourhood, propose to them if the weight of their edge
     * to you, which represents their likelihood of receiving from you, is greater than a
     * randomly generated double between 0 and 1.
     *
     * If the game is DG, you can mentally replace the word "propose" with "dictate".
     */
    public void playEWSpatialUG(){
        for(int i=0;i<neighbourhood.size();i++){
            Player neighbour = neighbourhood.get(i);
            double random_double = ThreadLocalRandom.current().nextDouble();
            double edge_weight = 0.0;
            for(int j=0;j<neighbour.neighbourhood.size();j++){ // find the edge weight.
                Player neighbours_neighbour = neighbour.getNeighbourhood().get(j);
                if(neighbours_neighbour.getId() == ID){
                    edge_weight = neighbour.getEdge_weights()[j];
                    break;
                }
            }
            if(edge_weight > random_double){
                playUG(neighbour);
            }
//            else {
//                System.out.println("(place BP here) EW too low");
//            }
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


    /**
     * method for assigning the position of a player on a 2D space and finding the neighbours when a
     * player resides on a 2D space. possible neighbourhood_type values: VN, M
     *
     * @param grid
     * @param row_position
     * @param column_position
     */
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

    /**
     * Allows edgeDecay2() to work by initialising edge_weights with respect to the
     * neighbourhood size.
     */
    public void initialiseEdgeWeights() {
        edge_weights = new double[neighbourhood.size()];
        for(int i=0;i<neighbourhood.size();i++){
            edge_weights[i] = 1.0;
//            edge_weights[i] = 0.5;
        }
    }



    /**
     * Method that allows players to perform a form of edge weight learning.
     * Here, a player x's edge weights is supposed to represent x's neighbours' relationship towards x.
     * If a neighbour y has a higher value of p than x, x raises the weight of their edge to y.
     * If y has a lower value of p than x, x reduces the weight of their edge to y.
     * The amount by which an edge is modified is determined by the rate_of_change parameter.
     */
    public void edgeWeightLearning(){
        for (int i = 0; i < neighbourhood.size(); i++) {
            Player neighbour = neighbourhood.get(i);
            if (neighbour.p > p) { // if neighbour is more generous than you, increase EW
                edge_weights[i] += rate_of_change;
                if(edge_weights[i] > 1.0){
                    edge_weights[i] = 1.0;
                }
            } else if(neighbour.p < p){ // if neighbour is less generous, decrease EW
                edge_weights[i] -= rate_of_change;
                if(edge_weights[i] < 0.0){
                    edge_weights[i] = 0.0;
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

    public static DecimalFormat getDF1() { return DF1; }

    public static DecimalFormat getDF4(){
        return DF4;
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


    public void setPlayers_left_to_play_this_gen(ArrayList<Player> al){
        players_left_to_play_this_gen=al;
    }

    public double[] getEdge_weights(){
        return edge_weights;
    }
    public static double getRate_of_change(){
        return rate_of_change;
    }
    public static void setRate_of_change(double d){
        rate_of_change=d;
    }





    @Override
    public String toString(){
        // comment out a variable if you don't want it to appear in the player description when debugging
        String description = "";
        description += "ID="+ID;
        description += " p="+DF4.format(p);
//        description += " oldp="+DF4.format(old_p);
//        description += " q="+DF4.format(q);
//        description += " A="+abstainer;
        description += " score="+DF4.format(score);
        description += " avgscore="+DF4.format(average_score);
//        description += " EAP="+ EAP;
        if(neighbourhood.size() != 0){
            description += " neighbours=[";
            for(int i=0;i<neighbourhood.size();i++){
                description += neighbourhood.get(i).getId();
                if((i+1) < neighbourhood.size()){ // are there more neighbours?
                    description +=", ";
                }
            }
            description +="]";
        }
        if(edge_weights.length != 0){
            description += " EW=[";
            for(int i=0;i<edge_weights.length;i++){
                description += DF1.format(edge_weights[i]);
                if((i+1) < neighbourhood.size()){ // are there more neighbours?
                    description +=", ";
                }
            }
            description +="]";
        }
        description += " GPTG="+ games_played_this_gen;
        description += " GPIT="+games_played_in_total;
        description += " R1G="+role1_games;
        description += " R2G="+role2_games;
        return description;
    }
}
