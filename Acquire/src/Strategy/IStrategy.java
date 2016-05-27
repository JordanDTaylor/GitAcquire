package Strategy;

import halladay.acquire.Chain;
import halladay.acquire.Game;
import halladay.acquire.Player;

import java.util.List;

public interface IStrategy {
    void placeTile(Game game, List<Player> players);
    void buyStock(Game game, List<Player> players);
    void resolveMergedStock(Chain game, List<Chain> players, List<Player> playerList);
    Chain selectWinner(List<Chain> chains, List<Player> players);
    void endTurn(Player Player);
}
