package austinjordantyler.strategy;

import austinjordantyler.PlayerUtils;
import austinjordantyler.SmartPlayer;
import austinjordantyler.TileUtils;
import halladay.acquire.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The general strategy is to prepare for the end game by owning majority stocks in the upcoming large chains.
 */
public class MidGameStrategy implements IStrategy {
    private static final double CENTER_WEIGHT = 0.20;
    private static final double NEW_CHAIN_SCORE = 1.5;
    private static final double GROW_CHAIN_SCORE = 1.25;
    private static final double MERGE_CHAIN_SCORE = 2.0;

    // Priorities
    // ====================================
    // - grow your chains
    //  - prioritize by highest stock
    //  - cause merge into that chain
    //  - place tile on that chain
    // - create new chains
    // - place towards the center
    @Override
    public void placeTile(Game game, SmartPlayer me, List<Player> otherPlayers) {
        List<Hotel> myTiles = me.getTiles();
        double highScore = 0.0;
        Hotel choice = null;
        for (Hotel tile : myTiles) {
            double score = this.scoreTile(game, tile, me, otherPlayers);
            if (score > highScore) {
                highScore = score;
                choice = tile;
            }
        }
        TileUtils.placeTile(game, me, choice);
    }

    /**
     * Considers buying stock in largest chains first.
     * Will not buy stock unless it puts him in the majority share holders spot.
     */
    @Override
    public void buyStock(Game game, SmartPlayer me, List<Player> otherPlayers) {
        int purchasesLeft = 3;
        int cashToSpend = me.getCash();

        // consider chains ordered by largest
        for (Chain activeChain : game.getActiveChains().stream()
                .sorted((c1, c2) -> -Integer.valueOf(c1.getHotelCount()).compareTo(c2.getHotelCount()))
                .collect(Collectors.toList())) {
            ChainType chainType = activeChain.getType();

            int numToPurchase = 0;
            int price = chainType.getStockPrice(activeChain.getHotelCount());
            while (cashToSpend >= price // can afford stock
                    && numToPurchase < purchasesLeft // allowed to buy stock
                    && PlayerUtils.willBeMajorityStockHolderWithXMoreShares(me, otherPlayers, chainType, purchasesLeft)) { // worth buying stock
                numToPurchase += 1; // TODO make the above mean will _become_ majority stock holder
                cashToSpend -= activeChain.getStockPrice();
            }
            if (numToPurchase > 0) {
                me.buyStock(chainType, numToPurchase, activeChain.getStockPrice());
            }
        }
    }

    /**
     * Trades ALL stock if it reaches the majority. Otherwise sells. It does not hang onto inactive stocks right now.
     */
    @Override
    public void resolveMergedStock(Chain winner, List<Chain> mergers, SmartPlayer me, List<Player> otherPlayers) {
    	mergers.remove(winner);
        ChainType tradingTo = winner.getType();
        List<ChainType> tradingFrom = mergers.stream() // TODO does this include all chains? or only defunct
                .map(Chain::getType)
                .collect(Collectors.toList());

        boolean shouldTrade = PlayerUtils.willBeMajorityStockHolderAfterTrade(
                me, otherPlayers, tradingFrom, tradingTo);
        if (shouldTrade) {
            // trade ALL stock into winner
            for (ChainType defunct : tradingFrom) {
                int numICanTrade = me.getStockSharesCount(defunct);
                if (numICanTrade > 0) {
                    me.tradeStock(defunct, numICanTrade, tradingTo);
                }
            }
        } else {
            // sell ALL stock
            for (Chain defunct : mergers) {
                int numICanSell = me.getStockSharesCount(defunct.getType());
                if (numICanSell > 0) {
                    me.sellStock(defunct.getType(), numICanSell, defunct.getStockPrice());
                }
            }
        }
    }

    /**
     * Picks the one we have more stock in.
     */
    @Override
    public Chain selectWinner(List<Chain> chains, SmartPlayer me, List<Player> otherPlayers) {
        Chain winner = null;
        int highScoreStocks = 0;
        for (Chain mergingChain : chains) {
            int numStocks = me.getStockSharesCount(mergingChain.getType());
            if (winner == null || numStocks > highScoreStocks) {
                highScoreStocks = numStocks;
                winner = mergingChain;
            }
        }
        return winner;
    }

    @Override
    public void endTurn(Game game, SmartPlayer me) {
        int numSafeChains = (int) game.getActiveChains().stream()
                .filter(Chain::isSafe)
                .count();
        int numNonSafeChains = game.getActiveChains().size() - numSafeChains;
        if (numSafeChains >= numNonSafeChains) {
            //me.setCurrentStrategy(new EndGameStrategy());
        }
    }

    private double scoreTile(Game game, Hotel tile, SmartPlayer me, List<Player> otherPlayers) {
        double centerScore = this.getCenterScore(game, tile);
        double newChainScore = this.getNewChainScore(game, tile);
        double growthScore = this.getChainGrowthScore(game, tile, me, otherPlayers);
        return centerScore + newChainScore + growthScore;
    }

    private double getCenterScore(Game game, Hotel tile) {
        double score = TileUtils.MAX_DISTANCE_FROM_CENTER - TileUtils.tileDistanceFromCenter(tile);
        return CENTER_WEIGHT * score;
    }

    /**
     * Returns 0 if no new chains can be made with this tile
     * Returns 1.0 * NEW_CHAIN_WEIGHT if the tile connects to any lone tiles
     */
    private double getNewChainScore(Game game, Hotel tile) {
        double score = 0.0;
        // only evaluate if we can start a new chain
        List<ChainType> startableChains = game.getStartableChains();
        if (startableChains.size() > 0){
            // figure out if we can actually start a new chain
            List<Hotel> connectingLoneTiles = TileUtils.getConnectingLoneTiles(game, tile);
            if (connectingLoneTiles.size() > 0) {
                score += 1.0;
            }
        }
        return NEW_CHAIN_SCORE * score;
    }

    /**
     * Encompasses both a growing chain and merging chain occurrence, where a merging chain is scored higher.
     * Returns 0 if no chains would grow
     * growing chain => GROW_CHAIN_WEIGHT
     * merging chain, we lose but trade/sell stocks => MERGE_CHAIN_WEIGHT
     * merging chain, we win the merge => GROW_CHAIN_WEIGHT + MERGE_CHAIN_WEIGHT
     */
    private double getChainGrowthScore(Game game, Hotel tile, SmartPlayer me, List<Player> otherPlayers) {
        double score = 0.0;
        List<Chain> connectingChains = game.getConnections(tile);
        if (connectingChains.size() > 0) {
            if (connectingChains.size() == 1) {
                // growing chain
                Chain growingChain = connectingChains.get(0);
                // only counts if it benefits us
                // depends whether you can get into the majority stock holders
                if (PlayerUtils.willBeMajorityStockHolderAfterPurchasing(me, otherPlayers, growingChain.getType())) {
                    score += GROW_CHAIN_SCORE; // TODO consider multiplying by the new size of the grown chain
                }
            } else {
                // merging chain
                // only counts if it benefits us (whether we get to participate in the merging stocks)

                // determine winner
                Chain winningChain = null;
                boolean participatingInMerge = false;
                for (Chain mergingChain : connectingChains) {
                    if (winningChain == null || mergingChain.getHotelCount() > winningChain.getHotelCount()) {
                        // TODO could be equal... should delegate to selectWinner
                        winningChain = mergingChain;
                    }
                    ChainType chainType = mergingChain.getType();
                    if (me.getStockSharesCount(chainType) > 0) {
                        participatingInMerge = true;
                    }
                }
                assert winningChain != null;
                if (participatingInMerge) {
                    // if it grows our chain, then it is a good move
                    if (PlayerUtils.willBeMajorityStockHolderAfterPurchasing(me, otherPlayers, winningChain.getType())) {
                        score += GROW_CHAIN_SCORE;
                    }

                    // if it defuncts our chain,
                    // then it is only a good move if moving stocks up would put us in the majority
                    List<ChainType> defunctChains = new ArrayList<>();
                    for (Chain mergingChain : connectingChains) {
                        if (mergingChain != winningChain) {
                            defunctChains.add(mergingChain.getType());
                        }
                    }
                    if (PlayerUtils.willBeMajorityStockHolderAfterTrade(
                            me, otherPlayers, defunctChains, winningChain.getType())) {
                        score += MERGE_CHAIN_SCORE;
                    }
                }
            }
        }
        return score;
    }
}
