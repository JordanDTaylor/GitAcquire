package Strategy;

import halladay.acquire.Chain;
import halladay.acquire.Game;
import halladay.acquire.Player;

import java.util.List;

public class MidGameStrategy implements IStrategy {

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
}
