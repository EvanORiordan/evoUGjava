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
    private int max_games_per_gen;
    private int games_played_this_gen;
    private int[] position; // allows for dynamic assignment of position values regardless of number of dimensions
    private static double prize; // the prize amount being split in an interaction
    private static double loners_payoff; // payoff received for being part of an interaction where a party abstained
    private double old_p; // the p value held at the beginning of the gen; will be copied by imitators
    private boolean abstainer = false; // indicates whether this player is an abstainer; an abstainer always abstains
    // from playing the game, hence both parties receive the loner's payoff; default value: false
//    private static int max_role1_games_per_gen; // the limit of games a player can be assigned as some role1
    private int role1_games_this_gen; // how many games this player has played so far this gen as role1
//    private static int max_role2_games_per_gen; // the limit of games a player can be assigned as some role2
    private int role2_games_this_gen; // how many games this player has played so far this gen as role2
    private double average_score; // average score of this player this gen


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

    // constructor for instantiating a player in an environment where abstaining is possible
    public Player(double p, double q, boolean abstainer){
        ID=count++; // assign this player's ID
        this.p=p; // assign p value
        this.q=q; // assign q value
        this.abstainer=abstainer; // indicate whether this player initialises as an abstainer
    }

    // method for playing the UG
    public void playUG(Player responder) {
        if(p >= responder.q){
            score += (prize*(1-p));
            responder.score += (prize*p);
        }

        games_played_in_total++;
        responder.games_played_in_total++;

        games_played_this_gen++;
        responder.games_played_this_gen++;

        role1_games_this_gen++;
        responder.role2_games_this_gen++;

        average_score = score / games_played_in_total;
        responder.average_score = responder.score / responder.games_played_in_total;
    }

    // method for playing the DG
    public void playDG(Player recipient){
        score += (prize*(1-p));
        recipient.score += (prize*p);

        games_played_in_total++;
        recipient.games_played_in_total++;

        games_played_this_gen++;
        recipient.games_played_this_gen++;

        role1_games_this_gen++;
        recipient.role2_games_this_gen++;

        average_score = score / games_played_in_total;
        recipient.average_score = recipient.score / recipient.games_played_in_total;
    }

    // method for playing the UG with an abstinence option.
    // if the proposer or the responder is an abstainer, both parties receive the loner's payoff.
    // otherwise, play the regular UG.
    public void playAbstinenceUG(Player responder){
        if(abstainer || responder.abstainer){
            score += loners_payoff;
            responder.score += loners_payoff;

            games_played_in_total++;
            responder.games_played_in_total++;

            games_played_this_gen++;
            responder.games_played_this_gen++;

            role1_games_this_gen++;
            responder.role2_games_this_gen++;

            average_score = score / games_played_in_total;
            responder.average_score = responder.score / responder.games_played_in_total;
        } else {
            playUG(responder);
        }
    }

    // method for playing the DG with an abstinence option.
    // if the proposer or the responder is an abstainer, both parties receive the loner's payoff.
    // otherwise, play the regular DG.
    public void playAbstinenceDG(Player recipient){
        if(abstainer || recipient.abstainer){
            score += loners_payoff;
            recipient.score += loners_payoff;

            games_played_in_total++;
            recipient.games_played_in_total++;

            games_played_this_gen++;
            recipient.games_played_this_gen++;

            role1_games_this_gen++;
            recipient.role2_games_this_gen++;

            average_score = score / games_played_in_total;
            recipient.average_score = recipient.score / recipient.games_played_in_total;
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

//    // method for playing the UG with an abstinence option
//    // firstly check if proposal is acceptable
//    // else if the offer was not satisfactory, there is a chance to abstain where the players receive a loner's payoff
//    // else the responder rejects the offer and neither party receive a payoff
//    public void playAbstinenceUG(Player responder) {
//        double a = baseAbstainProb * p;
//        double b = abstainThreshold * responder.q;
//        if (p >= responder.q) {
//            score += (prize * (1 - p));
//            responder.score += (prize * p);
//        } else if (a > b) {
//            score += loners_payoff;
//            responder.score += loners_payoff;
//        }
//        games_played_in_total++;
//        responder.games_played_in_total++;
//    }

    // different mechanism for determining probability to abstain than that of playAbstinenceUG().
    // if offer is unsatisfactory, responder may abstain.
    // the worse the offer was, the more likely they abstain.
//    public void playAbstinenceUG2(Player responder) {
//        double rand_double = ThreadLocalRandom.current().nextDouble();
//        double difference = responder.q - p; // the greater the difference, the greater the chance to abstain
//        if (p >= responder.q) {
//            score += (prize * (1 - p));
//            responder.score += (prize * p);
//        } else if (rand_double < difference) {
//            score += loners_payoff;
//            responder.score += loners_payoff;
//        }
//        games_played_in_total++;
//        responder.games_played_in_total++;
//    }

    // another difference abstain mechanism.
    // if the amount offered to the responder is worse than the loner's payoff, the responder abstains.
    // this does mean that if a responder has a lower acceptance threshold than the loner's payoff,
    // they will never abstain.
//    public void playAbstinenceUG3(Player responder) {
//        double amount_offered_to_responder = p * prize;
//        if (p >= responder.q) {
//            score += (prize * (1 - p));
//            responder.score += (prize * p);
//        } else if (amount_offered_to_responder < loners_payoff) {
//            score += loners_payoff;
//            responder.score += loners_payoff;
//        }
//        games_played_in_total++;
//        responder.games_played_in_total++;
//    }

    // method for playing the spatial UG with the option of abstinence. uses playAbstinenceUG2()
//    public void playAbstinenceSpatialUG(){
//        for(Player neighbour: neighbourhood){
//            if(games_played_this_gen != max_games_per_gen
//                    && neighbour.games_played_this_gen != max_games_per_gen){
//                boolean rand_bool = ThreadLocalRandom.current().nextBoolean();
//                if(rand_bool){
//                    playAbstinenceUG2(neighbour);
//                } else {
//                    neighbour.playAbstinenceUG2(this);
//                }
//                games_played_this_gen++;
//                neighbour.games_played_this_gen++;
//            }
//        }
//    }

    // abstinence spatial UG method that uses playAbstinenceUG3().
//    public void playAbstinenceSpatialUG2(){
//        for(Player neighbour: neighbourhood){
//            if(games_played_this_gen != max_games_per_gen
//                    && neighbour.games_played_this_gen != max_games_per_gen){
//                boolean rand_bool = ThreadLocalRandom.current().nextBoolean();
//                if(rand_bool){
//                    playAbstinenceUG3(neighbour);
//                } else {
//                    neighbour.playAbstinenceUG3(this);
//                }
//                games_played_this_gen++;
//                neighbour.games_played_this_gen++;
//            }
//        }
//    }

    // method for playing the abstinence spatial DG
//    public void playAbstinenceSpatialDG(){
//        for(Player neighbour: neighbourhood){
//            if(games_played_this_gen != max_games_per_gen
//                    && neighbour.games_played_this_gen != max_games_per_gen){
//                boolean rand_bool = ThreadLocalRandom.current().nextBoolean();
//                if(rand_bool){
//                    playAbstinenceDG(neighbour);
//                } else {
//                    neighbour.playAbstinenceDG(this);
//                }
//                games_played_this_gen++;
//                neighbour.games_played_this_gen++;
//            }
//        }
//    }

    // method for playing the DG with abstinence.
    // if a dictator offers a recipient an offer that is less than the loner's payoff,
    // that dictator is placed on that recipient's abstain list.
    // from then on, if that recipient player is receiving from that dictator player,
    // the recipient abstains.
    // should abstaining mean that you refuse to play with a player entirely, or just that you
    // don't want to play if you are the recipient?
    // currently, it implies the latter.
//    public void playAbstinenceDG(Player recipient){
//        for(Player player: recipient.abstainList){
//            if(ID == player.ID){
//                score += loners_payoff;
//                recipient.score += loners_payoff;
//                return;
//            }
//        }
//        playDG(recipient);
//        if(loners_payoff > (prize * this.p)){
//            if(recipient.abstainList == null){
//                recipient.abstainList = new ArrayList<>();
//            }
//            recipient.abstainList.add(this);
//        }
//    }

    // second method for playing the abstinence spatial DG.
    // uses playAbstinenceDG2()
//    public void playAbstinenceSpatialDG2(){
//        for(Player neighbour: neighbourhood){
//            if(games_played_this_gen != max_games_per_gen
//                    && neighbour.games_played_this_gen != max_games_per_gen){
//                boolean rand_bool = ThreadLocalRandom.current().nextBoolean();
//                if(rand_bool){
//                    playAbstinenceDG2(neighbour);
//                } else {
//                    neighbour.playAbstinenceDG2(this);
//                }
//                games_played_this_gen++;
//                neighbour.games_played_this_gen++;
//            }
//        }
//    }

    // here, abstaining from playing someone means never playing with them, whether
    // you are the dictator or the recipient.
//    public void playAbstinenceDG2(Player recipient){
//        for(Player player: recipient.abstainList){
//            if(ID == player.ID){
//                score += loners_payoff;
//                recipient.score += loners_payoff;
//                return;
//            }
//        }
//        for(Player player: abstainList){
//            if(recipient.ID == player.ID){
//                score += loners_payoff;
//                recipient.score += loners_payoff;
//                return;
//            }
//        }
//        playDG(recipient);
//        if(loners_payoff > (prize * this.p)){
//            if(recipient.abstainList == null){
//                recipient.abstainList = new ArrayList<>();
//            }
//            recipient.abstainList.add(this);
//        }
//    }

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
//            max_games_per_gen = 2;
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
//            max_games_per_gen = 4;
        } else if(neighbourhood_type.equals("moore8")){
//            max_games_per_gen = 8;
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



    @Override
    public String toString(){
        // comment out a variable if you don't want it to appear in the player description
        String description = "";
        description += "ID="+ID;
        description += " p="+p;
        description += " q="+q;
        description += " Abstainer="+abstainer;
        description += " Score="+score;
        description += " AS="+average_score;
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
//        DGTest1();
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
        Player player1 = new Player(ThreadLocalRandom.current().nextDouble(),ThreadLocalRandom.current().nextDouble());
        Player player2 = new Player(ThreadLocalRandom.current().nextDouble(),ThreadLocalRandom.current().nextDouble());
        player1.playUG(player2);
    }

    // basic DG test
    public static void DGTest1(){
        System.out.println("Executing "+Thread.currentThread().getStackTrace()[1].getClassName()+"."
                +Thread.currentThread().getStackTrace()[1].getMethodName()+"()...\n");
        Player.setPrize(1.0);
        Player player1 = new Player(ThreadLocalRandom.current().nextDouble());
        Player player2 = new Player(ThreadLocalRandom.current().nextDouble());
        player1.playDG(player2);
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

    // abstinence UG test
    public static void AbstinenceUGTest1(){
        System.out.println("Executing "+Thread.currentThread().getStackTrace()[1].getClassName()+"."
                +Thread.currentThread().getStackTrace()[1].getMethodName()+"()...\n");
        Player.setPrize(1.0);
        Player.setLoners_payoff(prize * 0.1);
        Player player1 = new Player( // regular player
                ThreadLocalRandom.current().nextDouble(),
                ThreadLocalRandom.current().nextDouble());
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
                player.playSpatialAbstinenceDG();
            }
        }
    }

}
