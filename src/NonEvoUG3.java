import java.util.ArrayList;
import java.util.Random;

/**
 * Test program of players whose strategies are randomly generated
 * (strategy values \(p , q \in [0,1]\) and \(p, q \in \mathbb{R}\))
 */
public class NonEvoUG3 {
    public static void main(String[] args){
        Random random = new Random();
        Player.setPrize(10.0);
        ArrayList<Player> players = new ArrayList<>();
        int num_players = 4;
        for(int i=0; i<num_players; i++){
            Player player = new Player(random.nextDouble(), random.nextDouble());
            players.add(player);
        }
        int round = 1;
        for(Player proposer: players){
            System.out.println("\n=========== Round "+round+" ===========");
            ArrayList responders = (ArrayList) players.clone();
            responders.remove(proposer);
            for(Object responder: responders){
                proposer.playUG((Player) responder);
            }
            round++;
            displayTotals(players);
        }
    }
    public static void displayTotals(ArrayList<Player> list){
        int i = 1;
        for(Player player: list){
            System.out.println("Total of player "+i+": "+player.getScore());
            i++;
        }
    }
}

