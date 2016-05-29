package austinjordantyler.Strategy;

import austinjordantyler.TileUtils;
import halladay.acquire.Chain;
import halladay.acquire.Game;
import halladay.acquire.Hotel;
import halladay.acquire.Player;

import java.util.List;

public class MidGameStrategy implements austinjordantyler.Strategy.IStrategy {
    @Override
    public void placeTile(Game game, Player me, List<Player> otherPlayers) {
        
    }

    @Override
    public void buyStock(Game game, Player me, List<Player> otherPlayers) {

    }

    @Override
    public void resolveMergedStock(Chain winner, List<Chain> mergers, Player me, List<Player> otherPlayers) {

    }

    @Override
    public Chain selectWinner(List<Chain> chains, Player me, List<Player> otherPlayers) {
        return null;
    }

    @Override
    public void endTurn(Player Player) {

    }
}
