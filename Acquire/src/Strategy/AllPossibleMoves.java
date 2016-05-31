package Strategy;

import halladay.acquire.*;

import java.util.HashMap;
import java.util.List;

public class AllPossibleMoves implements IStrategy {
    @Override
    public void placeTile(Game game, List<Player> players) {

    }

    @Override
    public void buyStock(Game game, List<Player> players) {

    }

    @Override
    public void resolveMergedStock(Chain game, List<Chain> players, List<Player> playerList) {

    }

    @Override
    public Chain selectWinner(List<Chain> chains, List<Player> players) {
        return null;
    }

    @Override
    public void endTurn(Player Player) {

    }

    private Stats getPlayerStats(List<Player> players){
        Stats stats = new Stats();
        for (Player player: players) {
            player.getCash();
//            stats
        }
        return stats;
    }

    class Stats{

    }

}
