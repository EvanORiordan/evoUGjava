import java.util.ArrayList;

/*
Basic java UG program of 2 hard-coded players with pre-determined strategies who playUG each other as both roles and
accumulate payoff.
 */
public class Tester1 {
    public static void main(String[] args){
        double prize = 10.0;
        ArrayList<Player> players = new ArrayList<>();
        Player player1 = new Player(0.4, 0.3);
        Player player2 = new Player(0.1, 0.25);
        players.add(player1);
        players.add(player2);
//        for(DG.Player player: players){
//            System.out.println("p = "+player.getp()+"\tq = "+player.getq());
//        }
        players.get(0).playUG(players.get(1), prize);
        players.get(1).playUG(players.get(0), prize);
//        System.out.println(players.get(0).getScore());
//        System.out.println(players.get(1).getScore());

    }
}
