import java.util.Random;

/**
 * Test program of two players who play a number of rounds, where one round is two instances of the UG where
 * both players get to play as both roles, and whose strategies are random each round.
 */
public class NonEvoUG4 {
    static int numRounds = 30; // set this value before running the experiment
    public static void main(String[] args){
        Random random = new Random();
        Player.setPrize(10.0);
        Player player1 = new Player();
        Player player2 = new Player();
        for(int i=0;i<numRounds;i++){
            player1.setP(random.nextDouble()); // here strategy is randomly set before playing
            player1.setQ(random.nextDouble());
            player2.setP(random.nextDouble());
            player2.setQ(random.nextDouble());
            System.out.println("\n=========== Round "+(i+1)+" ===========");
            player1.playUG(player2);
            player2.playUG(player1);
            displayTotals(player1, player2);
            // resetScore(player1, player2); // do you want to reset the score after each generation?
        }
    }
    public static void displayTotals(Player player1, Player player2){
        System.out.println("Total of player 1: "+player1.getScore());
        System.out.println("Total of player 2: "+player2.getScore());
    }
    public static void resetScore(Player player1, Player player2){
        player1.setScore(0);
        player2.setScore(0);
    }
}

