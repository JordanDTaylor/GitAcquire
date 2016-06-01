package austinjordantyler.strategy;

import austinjordantyler.SmartPlayer;
import halladay.acquire.Chain;
import halladay.acquire.Game;
import halladay.acquire.Player;

import java.util.List;

public class EndGameStrategy implements IStrategy {
    @Override
    public void placeTile(Game game, SmartPlayer me, List<Player> otherPlayers) {

    }

    @Override
    public void buyStock(Game game, SmartPlayer me, List<Player> otherPlayers) {

    }

    @Override
    public void resolveMergedStock(Chain winner, List<Chain> mergers, SmartPlayer me, List<Player> otherPlayers) {

    }

    @Override
    public Chain selectWinner(List<Chain> chains, SmartPlayer me, List<Player> otherPlayers) {
        return null;
    }

    @Override
    public void endTurn(Game game, SmartPlayer me) {

    }
}
