package austinjordantyler;

import halladay.acquire.Chain;
import halladay.acquire.ChainType;
import halladay.acquire.Game;
import halladay.acquire.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public static Map<Player, Integer> netWorth(List<Player> players, Game game) {
        PlayerMoney playerMoney = new PlayerMoney(players);

        List<Chain> chains = game.getActiveChains();

        for (Chain c : chains) {
            ChainType t = c.getType();
            ArrayList<Player> first = new ArrayList<>();
            ArrayList<Player> second = new ArrayList<>();

            for (Player p : players)
            {
                if ((first.size() == 0) || (p.getStockSharesCount(t) > first.get(0).getStockSharesCount(t))) {
                    second = first;
                    first = new ArrayList<>();
                    first.add(p);
                }
                else if (p.getStockSharesCount(t) == first.get(0).getStockSharesCount(t)) {
                    first.add(p);
                }
                else if ((second.size() == 0) || (p.getStockSharesCount(t) > second.get(0).getStockSharesCount(t))) {
                    second.clear();
                    second.add(p);
                }
                else if (p.getStockSharesCount(t) == second.get(0).getStockSharesCount(t)) {
                    second.add(p);
                }
                int qty = p.getStockSharesCount(c.getType());
                int pricePerShare = c.getStockPrice();
                int money = pricePerShare * qty;

                playerMoney.add(money,p);
            }
            int hotelCount = c.getHotelCount();
            if ((second.size() == 0) || (first.size() > 0))
            {
                final int amount = (t.getFirstBonus(hotelCount) + t.getSecondBonus(hotelCount)) / first.size();
                first.forEach(p -> playerMoney.add(amount, p));
            }else{
                first.forEach(p-> playerMoney.add(t.getFirstBonus(hotelCount), p));
                second.forEach(p-> playerMoney.add(t.getSecondBonus(hotelCount), p));
            }
        }
        return playerMoney.getMap();
    }
}

class PlayerMoney{
    Map<Player, Integer> map;

    public PlayerMoney(List<Player> players) {
        map = new HashMap<>();
        players.forEach(p->map.put(p,0));
    }

    public void add(int money, Player p){
        map.put(p, map.getOrDefault(p, money) + money);
    }

    public Map<Player, Integer> getMap(){
        return map;
    }
}
























