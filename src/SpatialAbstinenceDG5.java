import java.math.RoundingMode;
import java.util.ArrayList;

public class SpatialAbstinenceDG5 extends Thread{
    double prize = 1.0;
    int rows = 10;
    int columns = 10;
    int N = rows * columns;
    String neighbourhood = "vonNeumann4";
    int max_gens = 100000;
    ArrayList<ArrayList<Player>> grid = new ArrayList<>();

    public void start(){
        System.out.println("Executing "+Thread.currentThread().getStackTrace()[1].getClassName()+"."
                +Thread.currentThread().getStackTrace()[1].getMethodName()+"()...\n");
        Player.setPrize(prize);
        Player.setLoners_payoff(prize * 0.1);
        Player.setNeighbourhoodType(neighbourhood);
        Player.getDf().setRoundingMode(RoundingMode.UP);
        for(int i = 0; i < rows; i++){
            ArrayList<Player> row = new ArrayList<>();
            for(int j=0;j<columns;j++){
                row.add(new Player(0.5,0.5,true));
            }
            grid.add(row);
        }
        grid.get(4).get(4).setAbstainer(false);
        grid.get(4).get(5).setAbstainer(false);
        for(int i=0;i<rows;i++){
            for(int j=0;j<columns;j++){
                grid.get(i).get(j).findNeighbours2D(grid, i, j);
            }
        }
        int gen = 0;
        while(gen != max_gens) {
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < columns; j++) {
                    grid.get(i).get(j).playSpatialAbstinenceUG();
                }
            }
            for(int i=0;i<rows;i++){
                for(int j=0;j<columns;j++){
                    Player player = grid.get(i).get(j);
                    Player parent = null;
                    double highest_score_in_neighbourhood = player.getScore();
                    for(Player neighbour: player.getNeighbourhood()){
                        if(neighbour.getScore() > highest_score_in_neighbourhood){
                            parent = neighbour;
                            highest_score_in_neighbourhood = parent.getScore();
                        }
                    }
                    if(parent != null){
                        player.copyStrategy(parent);
                    }
                }
            }
            gen++;
            reset();
        }
    }

    public void reset(){
        for(ArrayList<Player> row: grid){
            for(Player player: row){
                player.setScore(0);
                player.setGamesPlayedThisGen(0);
                player.setOld_p(player.getP());
            }
        }
    }
}
