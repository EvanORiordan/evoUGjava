import java.util.Random;

/*
Tester of two players who playUG a number of rounds, where one round is two instances of the UG where both players
got to playUG as both roles, and whose strategies are random each round.
 */
public class Tester4 {
    public static void main(String[] args){
        Random random = new Random();
        double prize = 10.0;
        Player player1 = new Player();
        Player player2 = new Player();
        int numRounds = 3;
        for(int i=0;i<numRounds;i++){
            player1.setP(random.nextDouble());
            player1.setQ(random.nextDouble());
            player2.setP(random.nextDouble());
            player2.setQ(random.nextDouble());
            System.out.println("\n=========== Round "+(i+1)+" ===========");
            player1.playUG(player2, prize);
            player2.playUG(player1, prize);
        }
        displayTotals(player1, player2);
    }
    public static void displayTotals(Player player1, Player player2){
        System.out.println("\nTotal of player 1: "+player1.getScore());
        System.out.println("Total of player 2: "+player2.getScore());
    }
}

