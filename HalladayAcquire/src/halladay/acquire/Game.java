package halladay.acquire;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

public class Game {
	public static final int STARTING_CASH = 6000;
	public static final int N_ROWS = 9;
	public static final int N_COLS = 12;
	private static final int INIT_TILE_COUNT = 6;
	private static final int BIG_CHAIN_SIZE = 41;
	private static final int SAFE_CHAIN_SIZE = 11;

	TileContainer tiles = new TileContainer(N_ROWS, N_COLS);
	List<Hotel> board = new ArrayList<Hotel>();
	List<Hotel> dead = new ArrayList<Hotel>();

	private TreeMap<Hotel, Player> startingOrder = new TreeMap<Hotel, Player>();
	private ArrayList<Player> players = new ArrayList<Player>();
	private boolean isOver = false;
	private ArrayList<Chain> activeChains = new ArrayList<Chain>();

	private ArrayList<Listener> listeners = new ArrayList<Listener>();
	private int delay = 0;
	private boolean isPaused = false;

	public void setIsPaused(boolean isPaused) {
		this.isPaused = isPaused;
	}

	public boolean isPaused() {
		return isPaused;
	}

	public void register(Listener listener) {
		listeners.add(listener);
	}

	public void unregister(Listener listener) {
		listeners.remove(listener);
	}

	private void notifyListeners(Player current) {
		for (Listener listener : listeners) {
			listener.playComplete(current);
		}
	}

	public void addPlayer(Player player) {
		Hotel hotel = tiles.getNextRandom();
		startingOrder.put(hotel, player);
		board.add(hotel);
		System.out.println("Initial tile: "+hotel);
	}

	public void play() {
		createPlayerList();
		while (!isOver) {
			Player current = players.remove(0);
			players.add(current);
			System.out.println("Playing "+current);
			current.play(this);
			notifyListeners(current);
			try {
				Thread.sleep(delay);
				while (isPaused) {
					Thread.yield();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		calculateScores();
		players.sort(new Comparator<Player>() {
			public int compare(Player p1, Player p2) {
				return p1.getCash() - p2.getCash();
			}
		});

	}
	
	public void scorePlayers(HashMap<String, Integer> playerScores) {
		int score = 0;
		for (Player p : players) {
			String name = p.getName();
			Integer previousScore = playerScores.get(name);
			if (previousScore == null) {
				playerScores.put(name, score);
			} else {
				previousScore += score;
				playerScores.put(name, previousScore);
			}
			score++;
		}
	}

	private void createPlayerList() {
		Set<Hotel> keys = startingOrder.keySet();
		for (Hotel h : keys) {
			Player p = startingOrder.get(h);
			p.addTiles(tiles.getNextNRandom(INIT_TILE_COUNT));
			players.add(p);
		}
	}

	private void calculateScores() {
		for (Chain c : activeChains) {
			awardBonuses(c);
			for (Player p : players) {
				p.cashOut(c);
			}
		}
	}

	private void awardBonuses(Chain c) {
		ChainType t = c.getType();

		ArrayList<Player> first = new ArrayList<Player>();
		ArrayList<Player> second = new ArrayList<Player>();

		for (Player p : players) {
			if ((first.size() == 0) || (p.getStockSharesCount(t) > first.get(0).getStockSharesCount(t))) {
				second = first;
				first = new ArrayList<Player>();
				first.add(p);
			} else if (p.getStockSharesCount(t) == first.get(0).getStockSharesCount(t)) {
				first.add(p);
			} else if ((second.size() == 0) || (p.getStockSharesCount(t) > second.get(0).getStockSharesCount(t))) {
				second.clear();
				second.add(p);
			} else if (p.getStockSharesCount(t) == second.get(0).getStockSharesCount(t)) {
				second.add(p);
			}
		}
		int hotelCount = c.getHotelCount();
		if ((second.size() == 0) || (first.size() > 0)) {
			int amount = t.getFirstBonus(hotelCount) + t.getSecondBonus(hotelCount);
			amount /= first.size();
			for (Player p : first) {
				p.addCashBonus(amount);
			}
		} else {
			for (Player p : first) {
				p.addCashBonus(t.getFirstBonus(hotelCount));
			}
			for (Player p : second) {
				p.addCashBonus(t.getSecondBonus(hotelCount));
			}
		}
	}

	public void setDelay(int delay) {
		this.delay = delay;
	}

	///////////////////////////// Player Interface Section

	public Hotel getNextTile() {

		Hotel tile = null;
		while ((tiles.getSize() > 0) && (tile == null)) {
			Hotel temp = tiles.getNextRandom();
			if (isPlayable(temp)) tile = temp;
		}
		return tile;
	}

	public boolean isEndable() {
		boolean hasBigChain = false;
		boolean hasSmallChain = true;

		for (Chain c : activeChains) {
			if (c.getHotelCount() >= BIG_CHAIN_SIZE) {
				hasBigChain = true;
			}
			if (c.getHotelCount() < SAFE_CHAIN_SIZE) {
				hasSmallChain = true;
			}
		}
		return hasBigChain || !hasSmallChain;
	}

	public void causeEnd() {
		assert(isEndable());

		isOver = true;
	}

	public boolean isPlayable(Hotel h) {
		int safeCount = 0;
		for (Chain c : activeChains){
			if (c.isSafe()) safeCount++;
		}
		return (safeCount < 2);
	}

	public Hotel exchangeUnplayableTile(Hotel deadTile) {
		assert(!isPlayable(deadTile));

		dead.add(deadTile);
		return getNextTile();
	}

	public void startChain(ChainType typ, Hotel hotel) {
		Chain chain = new Chain(typ);
		chain.add(hotel);
		for (Hotel h : board) {
			if (chain.connectsTo(h)) {
				chain.add(h);
			}
		}
		activeChains.add(chain);
		System.out.println("Starting chain: "+chain.getType());
	}

	public List<Chain> getActiveChains() {
		return activeChains;
	}

	public List<Hotel> getPlayedTiles() {
		return board;
	}

	public List<ChainType> getStartableChains() {
		List<ChainType> startable = new ArrayList<ChainType>();
		for (ChainType typ : ChainType.values()) startable.add(typ);

		for (Chain c : activeChains) {
			startable.remove(c.getType());
		}
		return startable;
	}

	public void placeTile(Hotel tile, Player player) {

		Chain chainToGrow = null;
		List<Chain> connections = getConnections(tile);
		if (connections.size() > 1) {
			List<Chain> largest = getLargestChains(connections);
			Chain winner = (largest.size() > 1)? player.selectWinner(largest): largest.get(0);
			awardBonuses(winner);
			workOutMerger(winner, connections, player);
			chainToGrow = winner;
		} else if (connections.size() == 1) {
			chainToGrow = connections.get(0);
		}
		if (chainToGrow != null) {
			recursiveAdd(tile, chainToGrow);
		}
		board.add(tile);
	}

	private void recursiveAdd(Hotel tile, Chain chain) { // This has got to be slow - should have used a 2D array for the board - Chase was wrong
		if (!chain.contains(tile)) {
			chain.add(tile);
			for (Hotel h : board) {
				if (tile.isAdjacent(h)) {
					recursiveAdd(h, chain);
				}
			}
		}
	}

	public List<Chain> getConnections(Hotel tile) {
		ArrayList<Chain> connections = new ArrayList<Chain>();
		for (Chain c : activeChains) {
			if (c.connectsTo(tile)) {
				connections.add(c);
			}
		}
		return connections;
	}

	private List<Chain> getLargestChains(List<Chain> list) {
		ArrayList<Chain> largest = new ArrayList<Chain>();
		for (Chain c : list) {
			if ((largest.size() == 0) || (c.getHotelCount() > largest.get(0).getHotelCount())) {
				largest.clear();
				largest.add(c);
			} else if (c.getHotelCount() == largest.get(0).getHotelCount()) {
				largest.add(c);
			}
		}
		return largest;
	}

	private void workOutMerger(Chain winner, List<Chain> mergers, Player mergingPlayer) {
		System.out.println("Merging "+winner.getType()+" with:");
		for (Chain c : mergers) System.out.println("\t"+c.getType());
		List<Player> orderedPlayers = getOrderedPlayerList(mergingPlayer);
		for (Player p : orderedPlayers) {
			p.resolveMergedStock(winner, mergers);
		}

		for (Chain c : mergers) {
			if (c != winner) {
				winner.merge(c);
				activeChains.remove(c);
			}
		}
	}

	private List<Player> getOrderedPlayerList(Player startingPlayer) {
		@SuppressWarnings("unchecked")
		ArrayList<Player> list = (ArrayList<Player>) players.clone();
		while (list.get(0) != startingPlayer) {
			list.add(list.remove(0));
		}
		return list;
	}

	public Chain getAffiliation(Hotel hotel) {
		Chain aff = null;

		for (int i = 0; aff == null && i < activeChains.size(); i++) {
			Chain c = activeChains.get(i);
			if (c.contains(hotel)) aff = c;
		}
		return aff;
	}

	/////////////////////////////// End Player Interface Section

	public void displayScores() {
		for (Player p : players) {
			System.out.println(p.getName() + ": " + p.getCash());
		}
	}

	public interface Listener {
		public void playComplete(Player player);
	}
}
