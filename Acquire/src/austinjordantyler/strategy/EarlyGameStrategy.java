package austinjordantyler.strategy;

import austinjordantyler.SmartPlayer;
import austinjordantyler.TileUtils;
import halladay.acquire.Chain;
import halladay.acquire.ChainType;
import halladay.acquire.Game;
import halladay.acquire.Hotel;
import halladay.acquire.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EarlyGameStrategy implements IStrategy {
    @Override
    public void placeTile(Game game, SmartPlayer me, List<Player> otherPlayers) {
        List<Hotel> tiles = me.getTiles();
        
        //sorts the tiles by their distance to center to guarantee closes center placement
        tiles.sort((x,y) -> (int)TileUtils.tileDistanceFromCenter(x) - (int)TileUtils.tileDistanceFromCenter(y));
        
        //If we can create a chain, we do it  No matter what
    	if (game.getStartableChains().size() > 0){
    		for(Hotel tile : tiles){
            	if(TileUtils.getConnectingLoneTiles(game, tile).size() > 0) {
            		game.placeTile(tile, me);
            		((SmartPlayer)me).removeTile(tile);
            		startChain(game, tile, me);
            		return;
            	}
        	}
    	}

    	//At this point we know we cant create a new chain so we must get the closest to the center
		game.placeTile(tiles.get(0), me);
		((SmartPlayer)me).removeTile(tiles.get(0));
    }

	private void startChain(Game game, Hotel closestTile, SmartPlayer me) {
		List<ChainType> types = game.getStartableChains();
		ChainType bestType = getMostValuableChainType(types);
		game.startChain(bestType, closestTile);
		if (bestType.getOutstandingSharesCount() > 1) {	
			me.acquireStock(bestType, 1);			//obtains initial stock
		}
	}

	private ChainType getMostValuableChainType(List<ChainType> types) {
		ChainType bestType = types.get(0);
		for(ChainType type : types){
			if(type.getStockPrice(2) > bestType.getStockPrice(2)){
				bestType = type;
			}
		}
		return bestType;
	}

	@Override
    public void buyStock(Game game, SmartPlayer me, List<Player> otherPlayers) {
		//this gets all available chain types in order of value
		//
		List<ChainType> availableTypes = new ArrayList<>();
		game.getActiveChains().stream().map((chain) -> chain.getType()).sorted((x,y) -> x.getStockPrice(1)).forEach((type) -> availableTypes.add(type));
		
		//three concerns: cost, value, available
		
		//calculate the value of all potential options
		List<Option> options = calculateCurrentStockOptions(me, otherPlayers, availableTypes);

		//determine most valuable triple and double buys and three best single buys
		selectBestStockOption(me, options);
    }

	private List<Option> calculateCurrentStockOptions(Player me, List<Player> otherPlayers, List<ChainType> availableTypes) {
		List<Option> options = new ArrayList<>();
		
		for(ChainType type : availableTypes){
			int totalStockBought = 0;
			int majorityAmount = 0;
			int minorityAmount = 0;
			
			for(Player player : otherPlayers){
				int playerAmount = player.getStockSharesCount(type);
				if(playerAmount > majorityAmount){
					minorityAmount = majorityAmount;
					majorityAmount = playerAmount;
				}else if(playerAmount > minorityAmount){
					minorityAmount = playerAmount;
				}
				totalStockBought += playerAmount;
			}
			int myStocks = me.getStockSharesCount(type);
			totalStockBought += myStocks;
			int costOfShare = type.getStockPrice(totalStockBought);
			
			//buy 1
			int value = determineValue(type, totalStockBought, majorityAmount, minorityAmount, myStocks, 1);
			options.add(new Option(costOfShare, 1, value, type, costOfShare));
			
			//buy 2
			value = determineValue(type, totalStockBought, majorityAmount, minorityAmount, myStocks, 2);
			options.add(new Option(costOfShare * 2, 2, value, type, costOfShare));
			
			//buy 3
			value = determineValue(type, totalStockBought, majorityAmount, minorityAmount, myStocks, 3);
			options.add(new Option(costOfShare * 3, 3, value, type, costOfShare));
			
			//determine cost to obtain status upgrade
			//determine value based upon actual direct value multiplied by the distance away from the edge the stock is
		}
		return options;
	}

	private void selectBestStockOption(Player me, List<Option> options) {
		if(options.size() == 0){
			return;
		}
		//Sort the stocks and determine the best 3, 21, and 111 combos
		Collections.sort(options);
		Option three = null;
		Option two = null;
		List<Option> ones = new ArrayList<>();
		
		for(Option option : options){
			if(option.getAmountToBuy() == 3 && three == null && me.getCash() > costOf(option) * 3){
				three = option;
			}else if(option.getAmountToBuy() == 2 && two == null){
				two = option;
			}else if(option.getAmountToBuy() == 1 && ones.size() < 3){
				ones.add(option);
			}
		}
		
		//select the stocks to buy with the best total cost:value that fits in with the available stock to buy
		
		Option[] onesArray = ones.toArray(new Option[0]);
		
		if(costValue(three) > costValue(two, ones.get(0)) && costValue(three) > costValue(onesArray) && me.getCash() > costOf(three) * 3){
			buyStocks(me, three);
		}else if(costValue(two, ones.get(0)) > costValue(onesArray) && me.getCash() > costOf(two, two, ones.get(0))){
			buyStocks(me, two, ones.get(0));
		}else if(me.getCash() > costOf(onesArray)){
			buyStocks(me, onesArray);
		}
	}
	
	private int costOf(Option... options) {
		int totalCost = 0;
		for(Option o : options){
			totalCost += o.getCostOfShare();
		}
		return totalCost;
	}

	private void buyStocks(Player me, Option... options){
		for(Option i : options){
			me.buyStock(i.getType(), i.getAmountToBuy(), i.getCostOfShare());
		}
	}
	
	private double costValue(Option... options){
		if(options == null){
			return 0;
		}
		double totalCostValue = 0;
		for(Option option : options){
			if(option != null){
				totalCostValue += option.costValue();
			}
		}
		return totalCostValue;
	}

	private int determineValue(ChainType type, int totalStockBought, int majorityAmount, int minorityAmount, int myStocks, int amountToBuy) {
		int secondBonus = type.getSecondBonus(totalStockBought + amountToBuy);
		int value = 0;
		if(myStocks < majorityAmount && myStocks > majorityAmount - amountToBuy || myStocks == minorityAmount){
			value = secondBonus / 2;
		}else if(myStocks < minorityAmount && myStocks > minorityAmount - amountToBuy){
			value = secondBonus;
		}
		
		value += type.getStockPrice(totalStockBought + amountToBuy);
		
		return value;
	}

    @Override
    public void resolveMergedStock(Chain winner, List<Chain> mergers, SmartPlayer me, List<Player> otherPlayers) {
    	mergers.remove(winner);
    	//Calculate everyone's stock in the winner
		int majorityAmount = 0;
		int minorityAmount = 0;
		
		for(Player player : otherPlayers){
			int playerAmount = player.getStockSharesCount(winner.getType());
			if(playerAmount > majorityAmount){
				minorityAmount = majorityAmount;
				majorityAmount = playerAmount;
			}else if(playerAmount > minorityAmount){
				minorityAmount = playerAmount;
			}
		}
		//gets number of stocks available to us
		int availableStocks = mergers.stream().mapToInt((i) -> me.getStockSharesCount(i.getType())).sum();
		int myAmount = me.getStockSharesCount(winner.getType());
		int stocksForMajority = majorityAmount - myAmount;
		int stocksForMinority = minorityAmount - myAmount;
		
		//trade if we can gain a stock status
		if(stocksForMajority == 0 && availableStocks > 2){
			tradeOne(winner, mergers, me);
		}else if(stocksForMajority < availableStocks / 2){
            tradeAll(winner, mergers, me);
		}else if(stocksForMinority == 0 && availableStocks > 2){
			tradeOne(winner, mergers, me);
		}else if(stocksForMinority < availableStocks / 2){
            tradeAll(winner, mergers, me);
		}
		
		//sell the rest in any case
        sellAll(mergers, me);
    }

	private void sellAll(List<Chain> mergers, Player me) {
		for (Chain defunct : mergers) {
            int numICanSell = me.getStockSharesCount(defunct.getType());
            try{
            	me.sellStock(defunct.getType(), numICanSell, defunct.getStockPrice());
            }catch(Exception e){
            	//System.out.println("Attempted to sell stock that we have none of");
            }
        }
	}

	private void tradeOne(Chain winner, List<Chain> mergers, Player me) {
		for (Chain merger : mergers) {
			ChainType type = merger.getType();
		    int numICanTrade = me.getStockSharesCount(type);
	    	if(numICanTrade > 1){
	    		try{
		    		me.tradeStock(type, 2, winner.getType());
		    		break;
		    	}catch(NullPointerException e){
		    		//System.out.println("Attempted to trade stock that we have none of");
		    	}
	    	}
		}
	}

	private void tradeAll(Chain winner, List<Chain> mergers, Player me) {
		for (Chain merger : mergers) {
			ChainType type = merger.getType();
		    int numICanTrade = me.getStockSharesCount(type);
		    try{
		    	me.tradeStock(type, numICanTrade, winner.getType());
		    }catch(NullPointerException e){
		    	//System.out.println("Attempted to trade stock that we have none of");
		    }
		}
	}

    @Override
    public Chain selectWinner(List<Chain> chains, SmartPlayer me, List<Player> otherPlayers) {
    	//Just select the winner as the one where we have the most stocks in
    	//Yes I stole this from tyler.
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
    	//if number of chains created is above four and more than 20 tiles have been place
    	//or any chain is safe
    	//set to midgame strategy
    	List<Chain> activeChains = game.getActiveChains();
    	if(activeChains.size() > 4 && game.getPlayedTiles().size() > 32){
        	me.setCurrentStrategy(new MidGameStrategy());
    	}
    	for(Chain chain : activeChains){
    		if(chain.isSafe()){
    	    	me.setCurrentStrategy(new MidGameStrategy());
    		}
    	}
    }
}
