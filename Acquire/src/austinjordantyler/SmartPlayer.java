package austinjordantyler;

import austinjordantyler.strategy.IStrategy;
import halladay.acquire.*;

import java.util.ArrayList;
import java.util.List;

public class SmartPlayer extends Player implements Game.Listener {
    private IStrategy currentStrategy;

    private boolean isRegistered = false;

    private List<Player> otherPlayers = new ArrayList<>();

    public SmartPlayer(String name, int startingCash, IStrategy startingStrategy) {
        super(name, startingCash);
        this.currentStrategy = startingStrategy;
    }

    @Override
    public void play(Game game) {
        if (!this.isRegistered) {
            game.register(this);
            this.isRegistered = true;
        }

        this.currentStrategy.placeTile(game, this, this.otherPlayers);
        this.currentStrategy.buyStock(game, this, this.otherPlayers);

        // Get next tile
        Hotel tile = game.getNextTile();
        this.tiles.add(tile);

        // Exchange unplayable tiles
        this.exchangeUnplayableTiles(game);

        if (game.isEndable() && this.isWinning(game)) {
            game.causeEnd();
        }
        this.currentStrategy.endTurn(game, this);
    }

    private boolean isWinning(Game game) {
        return false;
    }

    @Override
    public Chain selectWinner(List<Chain> chains) {
        return this.currentStrategy.selectWinner(chains, this, this.otherPlayers);
    }

    @Override
    public void resolveMergedStock(Chain winner, List<Chain> mergers) {
        this.currentStrategy.resolveMergedStock(winner, mergers, this, this.otherPlayers);
    }

    @Override
    public void playComplete(Player player) {
        if (player != this && !this.otherPlayers.contains(player)) {
            this.otherPlayers.add(player);
        }
    }
    
    @Override
    public void acquireStock(ChainType chain, int amount) {
    	super.acquireStock(chain, amount);
    }

    public void removeTile(Hotel tile) {
        super.tiles.remove(tile);
    }

    public IStrategy getCurrentStrategy() {
        return this.currentStrategy;
    }

    public void setCurrentStrategy(IStrategy strategy) {
        this.currentStrategy = strategy;
    }
}
