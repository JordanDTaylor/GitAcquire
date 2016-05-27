import Strategy.IStrategy;
import halladay.acquire.*;

import java.util.ArrayList;
import java.util.List;

public class SmartPlayer extends Player implements Game.Listener {
    private IStrategy earlyGameStrategy;
    private IStrategy midGameStrategy;
    private IStrategy endGameStrategy;


    private IStrategy currentStrategy;


    public SmartPlayer(String name, int startingCash, IStrategy early, IStrategy mid, IStrategy end) {
        super(name, startingCash);
        earlyGameStrategy = early;
        midGameStrategy = mid;
        endGameStrategy = end;

        currentStrategy = early;
    }

    boolean isRegistered = false;

    @Override
    public void play(Game game) {
        if (!isRegistered) {
            game.register(this);
            isRegistered = true;
        }

        currentStrategy.placeTile(game, players);
        currentStrategy.buyStock(game, players);

        // Get next tile
        Hotel tile = game.getNextTile();
        tiles.add(tile);

        // Exchange unplayable tiles
        exchangeUnplayableTiles(game);

        if (game.isEndable() && isWinning(game)) {
            game.causeEnd();
        }
        currentStrategy.endTurn(this);
    }

    private boolean isWinning(Game game) {
        return false;
    }

    @Override
    public Chain selectWinner(List<Chain> chains) {
        return this.currentStrategy.selectWinner(chains, players);

    }

    @Override
    public void resolveMergedStock(Chain winner, List<Chain> mergers) {
        this.currentStrategy.resolveMergedStock(winner, mergers, players);
    }

    List<Player> players = new ArrayList<>();

    @Override
    public void playComplete(Player player) {
        if (!players.contains(player))
            players.add(player);
    }

    public IStrategy getCurrentStrategy() {
        return this.currentStrategy;
    }

    public void setCurrentStrategy(IStrategy currentStrategy) {
        this.currentStrategy = currentStrategy;
    }
}
