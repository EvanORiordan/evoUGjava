//import java.math.RoundingMode;
//import java.util.ArrayList;
//
///**
// * Hard-coded pop initially consists of 98 abstainers and 2 non-abstainers. Evolution results in the complete
// * imitation of the neighbour that scored the highest amount over oneself.
// */
//public class SpatialAbstinenceDG5 extends Thread{
//    double prize = 1.0;
//    int rows = 10;
//    int columns = 10;
//    int N = rows * columns;
//    String neighbourhood = "vonNeumann4";
//    int max_gens = 100000;
//    ArrayList<ArrayList<Player>> grid = new ArrayList<>();
//
//    public void start(){
//        System.out.println("Executing "+Thread.currentThread().getStackTrace()[1].getClassName()+"."
//                +Thread.currentThread().getStackTrace()[1].getMethodName()+"()...\n");
//        Player.setPrize(prize);
//        Player.setLoners_payoff(prize * 0.1);
//        Player.setNeighbourhoodType(neighbourhood);
//        Player.getDf().setRoundingMode(RoundingMode.UP);
//        for(int i = 0; i < rows; i++){
//            ArrayList<Player> row = new ArrayList<>();
//            for(int j=0;j<columns;j++){
//                row.add(new Player(0.95,0.0,true));
//            }
//            grid.add(row);
//        }
//        grid.get(0).get(0).setAbstainer(false);
//        grid.get(0).get(1).setAbstainer(false);
//        for(int i=0;i<rows;i++){
//            for(int j=0;j<columns;j++){
//                grid.get(i).get(j).findNeighbours2D(grid, i, j);
//            }
//        }
//        int gen = 0;
//        while(gen != max_gens) {
//            for (int i = 0; i < rows; i++) {
//                for (int j = 0; j < columns; j++) {
//                    grid.get(i).get(j).playSpatialAbstinenceUG();
//                }
//            }
//            for(int i=0;i<rows;i++){
//                for(int j=0;j<columns;j++){
//                    Player player = grid.get(i).get(j);
//                    Player parent = null;
//                    double highest_avg_score_in_neighbourhood = player.getAverage_score();
//                    for(Player neighbour: player.getNeighbourhood()){
//                        if(neighbour.getAverage_score() > highest_avg_score_in_neighbourhood){
//                            parent = neighbour;
//                            highest_avg_score_in_neighbourhood = parent.getAverage_score();
//                        }
//                    }
//                    if(parent != null){
//                        player.copyStrategy(parent);
//                    }
//                }
//            }
//            gen++;
//            reset();
//        }
//    }
//
//    public void reset(){
//        for(ArrayList<Player> row: grid){
//            for(Player player: row){
//                player.setScore(0);
//                player.setGamesPlayedThisGen(0);
//                player.setOld_p(player.getP());
//            }
//        }
//    }
//
//    public StorageObject1 giveStats(){
//        double avg_p=0;
//        double highest_p = 0.0;
//        double lowest_p = 1.0;
//        int abstainers = 0;
//        for(ArrayList<Player> row: grid){
//            for(Player player: row){
//                if(player.getP() > highest_p){
//                    highest_p = player.getP();
//                } else if(player.getP() < lowest_p){
//                    lowest_p = player.getP();
//                }
//                avg_p+=player.getP();
//                if(player.getAbstainer()){
//                    abstainers++;
//                }
//            }
//        }
//        avg_p /= N;
//        return new StorageObject1(avg_p,highest_p,lowest_p,abstainers);
//    }
//}
