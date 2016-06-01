package austinjordantyler.strategy;

import austinjordantyler.PlayerState;
import austinjordantyler.PlayerUtils;
import austinjordantyler.SmartPlayer;
import austinjordantyler.TileUtils;
import halladay.acquire.*;

import java.util.*;
import java.util.stream.Collectors;

public class EndGameStrategy implements IStrategy {
    private static final double CENTER_WEIGHT = 0.05;
    private static double NEW_CHAIN_SCORE = 1.0;
    private static double GROW_CHAIN_SCORE = 1.25;
    private static double MERGE_CHAIN_SCORE = 2.0;

    List<Player> playerList;
    private Game theGame;
    private Player self;
    private static final double POSITION_MULTIPLIER = 2;

/*
    #### General Strategy
    - 1-2 large chains: defend your position as a majority stock holder in large chains.
    - 2+ large chains: acquire enough stock in as many as you can to secure the second place bonus.
    - If behind, prevent ending the game by creating new chains.
*/

    /**
     * Tile Placement
     * - If behind: create new chains
     * - If ahead: secure position by growing your large chains
     */
    @Override
    public void placeTile(Game game, Player me, List<Player> otherPlayers) {
        theGame = game;
        playerList = otherPlayers;
        self = me;

        List<Hotel> myTiles = me.getTiles();
        double highScore = 0.0;

        Hotel choice = null;
        for (Hotel tile : myTiles) {
            double score = this.scoreTile(tile);
            if (score > highScore) {
                highScore = score;
                choice = tile;
            }
        }
        game.placeTile(choice, me);
    }

    /**
     * 1-2 large chains: defend your position as a majority stock holder in large chains.
     * 2+ large chains: acquire enough stock in as many as you can to secure the second place bonus.
     *
     * Considers buying stock in largest chains first.
     * Will not buy stock unless it puts him in the majority share holders spot.
     */
    @Override
    public void buyStock(Game game, Player me, List<Player> otherPlayers) {
        int purchasesLeft = 3;
        int cashToSpend = me.getCash();
        List<Chain> sortedActiveChains = game.getActiveChains()
                .stream()
                .sorted((c1, c2) -> -Integer.valueOf(c1.getHotelCount()).compareTo(c2.getHotelCount()))
                .collect(Collectors.toList());


        //TODO get number of large chains and effect stock buy accordingly.
        for (Chain activeChain : sortedActiveChains) {
            ChainType chainType = activeChain.getType();

            int numToPurchase = 0;
            int price = chainType.getStockPrice(activeChain.getHotelCount());

            while (cashToSpend >= price // can afford stock
                    && numToPurchase < purchasesLeft // allowed to buy stock
                    && !PlayerUtils.willBeMajorityStockHolderAfterPurchasing(me, otherPlayers, chainType)) { // worth buying stock
                numToPurchase += 1;
                cashToSpend -= activeChain.getStockPrice();
            }
            if (numToPurchase > 0) {
                me.buyStock(chainType, numToPurchase, activeChain.getStockPrice());
            }
        }
    }

    /**
     * Directly compare the merge choices and choose the option that gives you the most net value
     */
    @Override
    public void resolveMergedStock(Chain winner, List<Chain> mergers, Player me, List<Player> otherPlayers) {
        ChainType tradingTo = winner.getType();

        List<ChainType> tradingFrom = mergers
                .stream()
                .map(Chain::getType)
                .filter(c->!c.equals(winner.getType()))
                .collect(Collectors.toList());

        PlayerState initialState = PlayerState.fromPlayer(me, theGame);


    }


    /**
     * Picks the one we have more stock in.
     */
    @Override
    public Chain selectWinner(List<Chain> chains, Player me, List<Player> otherPlayers) {
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


    /**
     * Ending the Game
     * - The game should end at the end of your turn if the game can end, and the player is winning.
     * - TODO Consider ending the game if a higher rank is unreachable (e.g. you are in second place, cannot reach first place, and do not want to drop into third place, so you end the game).
     *
     * @param game
     * @param me
     */
    @Override
    public void endTurn(Game game, SmartPlayer me) {
        if(isWinning(me) && game.isEndable()){
            game.causeEnd();
        }
    }

    private boolean isWinning(Player player) {
        Player winningPlayer = PlayerUtils.netWorth(playerList, theGame)
                .entrySet()
                .stream()
                .sorted(Comparator.comparing(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .findFirst()
                .get();
        assert (winningPlayer != null);
        return player.equals(winningPlayer);
    }

    /**
     * - If behind: create new chains
     * - If ahead: secure position by growing your large chains
     */
    private double scoreTile(Hotel tile) {
        double centerScore = this.getCenterScore(tile);
        double newChainScore = this.getNewChainScore(tile);
        double growthScore = this.getChainGrowthScore(tile);

        if(isWinning(self))
            growthScore *= POSITION_MULTIPLIER;
        else
            newChainScore *= POSITION_MULTIPLIER;

        return centerScore + newChainScore + growthScore;
    }

    private double getCenterScore(Hotel tile) {
        double score = TileUtils.MAX_DISTANCE_FROM_CENTER - TileUtils.tileDistanceFromCenter(theGame, tile);
        return CENTER_WEIGHT * score;
    }

    private double getNewChainScore(Hotel tile) {
        double score = 0.0;
        List<ChainType> startableChains = theGame.getStartableChains();
        if (startableChains.size() > 0){
            List<Hotel> connectingLoneTiles = TileUtils.getConnectingLoneTiles(theGame, tile);
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
    private double getChainGrowthScore(Hotel tile) {
        double score = 0.0;
        List<Chain> connectingChains = theGame.getConnections(tile);
        if (connectingChains.size() > 0) {
            if (connectingChains.size() == 1) {
                // growing chain
                Chain growingChain = connectingChains.get(0);
                // only counts if it benefits us
                // depends whether you can get into the majority stock holders
                if (PlayerUtils.willBeMajorityStockHolderAfterPurchasing(self, playerList, growingChain.getType())) {
                    score += GROW_CHAIN_SCORE; // TODO consider multiplying by the new size of the grown chain
                }
            } else {
                Chain winningChain = null;
                boolean participatingInMerge = false;


                for (Chain mergingChain : connectingChains) {
                    if (winningChain == null || mergingChain.getHotelCount() > winningChain.getHotelCount()) {
                        winningChain = mergingChain;
                    }
                    ChainType chainType = mergingChain.getType();
                    if (self.getStockSharesCount(chainType) > 0) {
                        participatingInMerge = true;
                    }
                }
                assert winningChain != null;
                if (participatingInMerge) {
                    // if it grows our chain, then it is a good move
                    if (PlayerUtils.willBeMajorityStockHolderAfterPurchasing(self, playerList, winningChain.getType())) {
                        score += GROW_CHAIN_SCORE;
                    }

                    Chain finalWinningChain = winningChain;
                    List<ChainType> defunctChains = connectingChains
                            .parallelStream()
                            .filter(mergingChain->mergingChain == finalWinningChain)
                            .map(c->c.getType())
                            .collect(Collectors.toList());


                    if (majorityAfterTrade(defunctChains, winningChain.getType() )) {
                        score += MERGE_CHAIN_SCORE;
                    }
                }
            }
        }
        return score;
    }
    private boolean majorityAfterTrade(List<ChainType> defunctChains, ChainType winningChainType){
        return PlayerUtils.willBeMajorityStockHolderAfterTrade(self, playerList, defunctChains, winningChainType);
    }
}
