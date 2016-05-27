package halladay.acquire;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;


public abstract class Player {

	protected final String name;
	protected int cash;
	protected HashMap<ChainType,Integer> stocks = new HashMap<ChainType, Integer>();
	protected ArrayList<Hotel> tiles = new ArrayList<Hotel>();

	protected static Random random = RandomFactory.getRandom();

	public Player(String name, int startingCash) {
		this.name = name;
		this.cash = startingCash;
	}

	public String getName() {
		return name;
	}

	public int getCash() {
		return cash;
	}

	public void addTiles(List<Hotel> tiles) {
		for (Hotel h : tiles) {
			this.tiles.add(h);
		}
	}

	@SuppressWarnings("unchecked")
	public ArrayList<Hotel> getTiles() {
		return (ArrayList<Hotel>) tiles.clone();
	}

	public void buyStock(ChainType chain, int amount, int pricePerShare) {
		assert(cash >= amount * pricePerShare);

		acquireStock(chain, amount);
		cash -= amount * pricePerShare;
	}

	public void sellStock(ChainType chain, int amount, int pricePerShare) {
		releaseStock(chain, amount);
		cash += amount * pricePerShare;
	}

	public int getStockSharesCount(ChainType chain) {
		int count = 0;
		if (stocks.containsKey(chain)) {
			count = stocks.get(chain);
		}
		return count;
	}

	public void addCashBonus(int amount) {
		cash += amount;
	}

	public void tradeStock(ChainType tradeIn, int tradeInAmount, ChainType result) {
		releaseStock(tradeIn, tradeInAmount);
		acquireStock(result, tradeInAmount/2);
	}


	////////////////////////////// Game Play Section

	public abstract void play(Game game);

	protected abstract Chain selectWinner(List<Chain> chains);

	protected abstract void resolveMergedStock(Chain winner, List<Chain> mergers);

	//////////////////////////////// End Play Section

	protected void exchangeUnplayableTiles(Game game) {
		ArrayList<Hotel> deadTiles = new ArrayList<Hotel>();
		for (Hotel h : tiles) {
			if (!game.isPlayable(h)) deadTiles.add(h);
		}

		for (Hotel h : deadTiles) {
			tiles.remove(h);
			Hotel temp = game.exchangeUnplayableTile(h);
			if (temp != null) tiles.add(temp);
		}
	}

	public void cashOut(Chain c) {
		ChainType stock = c.getType();
		if (stocks.containsKey(stock)) {
			sellStock(stock, stocks.get(stock), stock.getStockPrice(c.getHotelCount()));
		}
	}

	protected void releaseStock(ChainType chain, int amount) {
		chain.sellShares(amount);
		int count = stocks.get(chain);
		count -= amount;
		stocks.put(chain, count);
		chain.sellShares(amount);
	}

	protected void acquireStock(ChainType chain, int amount) {
		int count = 0;
		if (stocks.containsKey(chain)) {
			count = stocks.get(chain);
		}
		count += amount;
		stocks.put(chain, count);
		chain.buyShares(amount);
		Logger.GameMessageLog("bought "+amount+" share(s) of "+chain);
	}

	@Override
	public String toString() {
		String s = "Player name=" + name + ", cash=" + cash + "\n\tstocks: ";
		for (ChainType c : stocks.keySet()) {
			int count = stocks.get(c);
			s += ""+c+":"+count+" ";
		}
		s += "\n\ttiles:";
		for (Hotel h : tiles) {
			s += h + " ";
		}
		return s;
	}


}
