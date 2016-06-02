package austinjordantyler;

import halladay.acquire.ChainType;
import halladay.acquire.Game;
import halladay.acquire.Player;

import java.util.List;

public class PlayerUtils {
    public static boolean willBeMajorityStockHolderWithXMoreShares(Player me,
                                                                   List<Player> otherPlayers,
                                                                   ChainType chain,
                                                                   int xMoreShares) {
        int numMyCurrentShares = me.getStockSharesCount(chain);

        Player majorityShareHolder = me;
        int numMajorityShareHolderShares = numMyCurrentShares;

        Player secondShareHolder = me;
        int numSecondaryShareHolderShares = numMyCurrentShares;

        for (Player otherPlayer : otherPlayers) {
            int numOtherPlayerShares = otherPlayer.getStockSharesCount(chain);
            if (numOtherPlayerShares >= numMajorityShareHolderShares) {
                // shift current majority into secondary
                secondShareHolder = majorityShareHolder;
                numSecondaryShareHolderShares = numMajorityShareHolderShares;

                // update majority
                majorityShareHolder = otherPlayer;
                numMajorityShareHolderShares = numOtherPlayerShares;
            }
        }

        return numMyCurrentShares + xMoreShares >= numMajorityShareHolderShares;
    }

    public static boolean willBeMajorityStockHolderAfterPurchasing(Player me,
                                                                   List<Player> otherPlayers,
                                                                   ChainType chain) {
        int numICanBuy = getNumPurchasableSharesInChain(me, chain);
        return willBeMajorityStockHolderWithXMoreShares(me, otherPlayers, chain, numICanBuy);
    }

    public static int getNumPurchasableSharesInChain(Player me, ChainType chainType) {
        int availableShares = chainType.getOutstandingSharesCount();
        int stockPrice = chainType.getStockPrice(1); // TODO The stock could increase
        int myCash = me.getCash();
        int numICanAfford = myCash / stockPrice;
        return Math.min(numICanAfford, availableShares);
    }

    public static boolean willBeMajorityStockHolderAfterTrade(Player me,
                                                              List<Player> otherPlayers,
                                                              List<ChainType> tradingFrom,
                                                              ChainType tradingTo) {
        int numMyShares = 0;
        for (ChainType defunctChain : tradingFrom) {
            numMyShares += me.getStockSharesCount(defunctChain);
        }
        int numICanTrade = numMyShares / 2;
        return willBeMajorityStockHolderWithXMoreShares(me, otherPlayers, tradingTo, numICanTrade);
    }
}
