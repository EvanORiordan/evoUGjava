import java.util.Random;

/**
 * Evo UG. INCOMPLETE.
 */
public class EvoUG2 {
    static double prize = 10.0;
    public static void main(String[] args){
        Player.setPrize(prize);
        Random random = new Random();
        double prize = 10.0;
        Player player1 = new Player(random.nextDouble(), random.nextDouble());
        Player player2 = new Player();
        int numRounds = 5;
        for(int i=0;i<numRounds;i++){
            System.out.println("\n=========== Round "+(i+1)+" ===========");
            player1.playUG(player2);
            player2.playUG(player1);

            // APPLY EVOLUTIONARY PRESSURE
            if(player1.getScore()>player2.getScore()){
                System.out.println("player 1 is doing better than player 2");
                // ... what is a good way for the weaker player to mutate based off of the stronger player?
            } else{
                System.out.println("player 2 is doing better than player 1");
            }
        }
        displayTotals(player1, player2);
    }
    public static void displayTotals(Player player1, Player player2){
        System.out.println("\nTotal of player 1: "+player1.getScore());
        System.out.println("Total of player 2: "+player2.getScore());
    }
}
