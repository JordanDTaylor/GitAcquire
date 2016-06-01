package austinjordantyler.strategy;

import austinjordantyler.SmartPlayer;
import austinjordantyler.TileUtils;
import halladay.acquire.Chain;
import halladay.acquire.ChainType;
import halladay.acquire.Game;
import halladay.acquire.Hotel;
import halladay.acquire.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class EarlyGameStrategy implements IStrategy {
	
	
    @Override
    public void placeTile(Game game, Player me, List<Player> otherPlayers) {
        List<Hotel> tiles = me.getTiles();
        
        //sorts the tiles by their distance to center to guarantee closes center placement
        tiles.sort((x,y) -> (int)TileUtils.tileDistanceFromCenter(game, x) - (int)TileUtils.tileDistanceFromCenter(game, y));
        
        //If we can create a chain, we do it  No matter what
    	if (game.getStartableChains().size() > 0){
    		for(Hotel tile : tiles){
            	if(TileUtils.getConnectingLoneTiles(game, tile).size() > 0) {
            		game.placeTile(tile, me);
            		startChain(game, tile, me);
            		return;
            	}
        	}
    	}

    	//At this point we know we cant create a new chain so we must get the closest to the center
		game.placeTile(tiles.get(0), me);
		

    }

	private void startChain(Game game, Hotel closestTile, Player me) {
		List<ChainType> types = game.getStartableChains();
		ChainType bestType = types.get(0);
		for(ChainType type : types){
			if(type.getStockPrice(2) > bestType.getStockPrice(2)){
				bestType = type;
			}
		}
		game.startChain(bestType, closestTile);
		if (bestType.getOutstandingSharesCount() > 1) {
			//obtains initial stock
			((SmartPlayer)me).acquireStock(bestType, 1);
		}
	}

	@Override
    public void buyStock(Game game, Player me, List<Player> otherPlayers) {
		//this gets all available chain types in order of value
		List<ChainType> availableTypes = new ArrayList<>();
		game.getActiveChains().stream().map((chain) -> chain.getType()).sorted((x,y) -> x.getStockPrice(1)).forEach((type) -> availableTypes.add(type));
		
		//three concerns: cost, value, available
		
		//calculate the value of all potential options
		List<Option> options = calculateCurrentStockOptions(me, otherPlayers, availableTypes);

		//determine most valuable triple and double buys and three best single buys
		selectBestStockOption(me, options);
		
    }

	private List<Option> calculateCurrentStockOptions(Player me, List<Player> otherPlayers,
			List<ChainType> availableTypes) {
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
		//Sort the stocks and determine the best 3, 21, and 111 combos
		Collections.sort(options);
		Option three = null;
		Option two = null;
		List<Option> ones = new ArrayList<>();
		
		for(Option option : options){
			if(option.getAmountToBuy() == 3 && three == null){
				three = option;
			}else if(option.getAmountToBuy() == 2 && two == null){
				two = option;
			}else if(option.getAmountToBuy() == 1 && ones.size() < 3){
				ones.add(option);
			}
		}
		
		//select the stocks to buy with the best total cost:value that fits in with the available stock to buy
		
		Option[] onesArray = ones.toArray(new Option[3]);
		
		if(costValue(three) > costValue(two, ones.get(0)) && costValue(three) > costValue(onesArray)){
			buyStocks(me, three);
		}else if(costValue(two, ones.get(0)) > costValue(onesArray)){
			buyStocks(me, two, ones.get(0));
		}else{
			buyStocks(me, onesArray);
		}
	}
	
	private void buyStocks(Player me, Option... options){
		for(Option i : options){
			me.buyStock(i.getType(), i.getAmountToBuy(), i.getCostOfShare());
		}
	}
	
	private double costValue(Option... options){
		double totalCostValue = 0;
		for(Option option : options){
			totalCostValue += option.costValue();
		}
		return totalCostValue;
	}

	private int determineValue(ChainType type, int totalStockBought, int majorityAmount, int minorityAmount, int myStocks, int amountToBuy) {
		int secondBonus = type.getSecondBonus(totalStockBought + amountToBuy);
		int value = 0;
		if(myStocks < majorityAmount && myStocks > majorityAmount - amountToBuy || myStocks < minorityAmount && myStocks > minorityAmount - amountToBuy){
			value = secondBonus / 2;
		}
		
		value += type.getStockPrice(totalStockBought + amountToBuy) - type.getStockPrice(totalStockBought);
		
		return value;
	}

    @Override
    public void resolveMergedStock(Chain winner, List<Chain> mergers, Player me, List<Player> otherPlayers) {
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
		    me.tradeStock(mergers.get(0).getType(), 2, winner.getType());
		}else if(stocksForMajority < availableStocks / 2){
            tradeAll(winner, mergers, me);
		}else if(stocksForMinority == 0 && availableStocks > 2){
		    me.tradeStock(mergers.get(0).getType(), 2, winner.getType());
		}else if(stocksForMinority < availableStocks / 2){
            tradeAll(winner, mergers, me);
		}
		
		//sell the rest in any case
        sellAll(mergers, me);
    }

	private void sellAll(List<Chain> mergers, Player me) {
		for (Chain defunct : mergers) {
            int numICanSell = me.getStockSharesCount(defunct.getType());
            me.sellStock(defunct.getType(), numICanSell, defunct.getStockPrice());
        }
	}

	private void tradeAll(Chain winner, List<Chain> mergers, Player me) {
		for (Chain merger : mergers) {
			ChainType type = merger.getType();
		    int numICanTrade = me.getStockSharesCount(type);
		    me.tradeStock(type, numICanTrade, winner.getType());
		}
	}

    @Override
    public Chain selectWinner(List<Chain> chains, Player me, List<Player> otherPlayers) {
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
    	if(activeChains.size() > 3 && game.getPlayedTiles().size() > 20){
        	me.setCurrentStrategy(new MidGameStrategy());
    	}
    	for(Chain chain : activeChains){
    		if(chain.isSafe()){
    	    	me.setCurrentStrategy(new MidGameStrategy());
    		}
    	}
    }
}
