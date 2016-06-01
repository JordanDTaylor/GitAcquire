package austinjordantyler.strategy;

import austinjordantyler.SmartPlayer;
import halladay.acquire.Chain;
import halladay.acquire.Game;
import halladay.acquire.Player;

import java.util.List;

public interface IStrategy {
    void placeTile(Game game, SmartPlayer me, List<Player> otherPlayers);
    void buyStock(Game game, SmartPlayer me, List<Player> otherPlayers);
    void resolveMergedStock(Chain winner, List<Chain> mergers, SmartPlayer me, List<Player> otherPlayers);
    Chain selectWinner(List<Chain> chains, SmartPlayer me, List<Player> otherPlayers);
    void endTurn(Game game, SmartPlayer me);
}
