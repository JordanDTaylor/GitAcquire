package Strategy;

import halladay.acquire.Chain;
import halladay.acquire.Game;
import halladay.acquire.Player;

import java.util.List;

public interface IStrategy {
    void placeTile(Game game, Player me, List<Player> otherPlayers);
    void buyStock(Game game, Player me, List<Player> otherPlayers);
    void resolveMergedStock(Chain winner, List<Chain> mergers, Player me, List<Player> otherPlayers);
    Chain selectWinner(List<Chain> chains, Player me, List<Player> otherPlayers);
    void endTurn(Player me);
}
