import java.util.ArrayList;

/*
tester with ArrayList containing 4 hard-coded players where each player, as a proposer, plays the other players,
resulting in 12 rounds in total
 */
public class Tester2 {
    public static void main(String[] args){
        double prize = 10.0;
        ArrayList<Player> players = new ArrayList<>();
        Player player1 = new Player(0.25, 0.12);
        Player player2 = new Player(0.4, 0.22);
        Player player3 = new Player(0.2, 0.32);
        Player player4 = new Player(0.1, 0.42);
        players.add(player1);
        players.add(player2);
        players.add(player3);
        players.add(player4);
        int round = 1;
        for(Player proposer: players){
            System.out.println("\n=========== Round "+round+" ===========");
            ArrayList responders = (ArrayList) players.clone();
            responders.remove(proposer);
            for(Object responder: responders){
                proposer.play((Player) responder, prize);
            }
            round++;
        }
        displayTotals(players);
    }
    public static void displayTotals(ArrayList<Player> list){
        int i = 1;
        for(Player player: list){
            System.out.println("Total of player "+i+": "+player.getScore());
            i++;
        }
    }
}

