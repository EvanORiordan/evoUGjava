import java.util.concurrent.ThreadLocalRandom;

/**
 * Non-evolutionary Dictator Game (DG) program with two players.
 */
public class NonEvoDG1 {
    public static void main(String[] args) {
        Player.setPrize(10.0);
        Player player1 = new Player(ThreadLocalRandom.current().nextDouble());
        Player player2 = new Player(ThreadLocalRandom.current().nextDouble());
        display(player1, player2);
        player1.playDG(player2);
        display(player1, player2);
        player2.playDG(player1);
        display(player1, player2);
    }
    public static void display(Player player1, Player player2){
        System.out.println(player1);
        System.out.println(player2);
    }
}
