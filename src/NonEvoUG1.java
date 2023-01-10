import java.util.ArrayList;

/**
 * Basic test UG program of 2 hard-coded players with pre-determined strategies who play each other as both roles and
 * accumulate payoff.
 */
public class NonEvoUG1 {
    public static void main(String[] args){
        Player.setPrize(10.0);
        ArrayList<Player> players = new ArrayList<>();
        Player player1 = new Player(0.4, 0.3);
        Player player2 = new Player(0.1, 0.25);
        players.add(player1);
        players.add(player2);
        players.get(0).playUG(players.get(1));
        players.get(1).playUG(players.get(0));
    }
}
