package halladay.acquire.dumbPlayer;

import halladay.acquire.Chain;
import halladay.acquire.ChainType;
import halladay.acquire.Game;
import halladay.acquire.Hotel;
import halladay.acquire.Player;

import java.util.List;

public class DumbPlayer extends Player {

	public DumbPlayer(String name, int startingCash) {
		super(name, startingCash);
	}

	@Override
	public void play(Game game) {
		// Decide to call game
		if (game.isEndable()) {
			game.causeEnd();
		} else {
			// Place tile
			placeTile(game);
			// Buy stock
			buyStock(game);
			// Get next tile
			Hotel tile = game.getNextTile();
			tiles.add(tile);
			System.out.println("Picked up "+tile);
			// Exchange unplayable tiles
			exchangeUnplayableTiles(game);
		}
	}

	@Override
	public Chain selectWinner(List<Chain> chains) {
		return chains.get(0);
	}

	@Override
	public void resolveMergedStock(Chain winner, List<Chain> mergers) {
		for (Chain c : mergers) {
			if (c != winner && stocks.containsKey(c.getType())) {
				sellStock(c.getType(), stocks.get(c.getType()), c.getStockPrice());
			}
		}
	}

	private void placeTile(Game game) {
		Hotel tile = getChainStarter(game);
		if (tile == null) { // can't start a chain
			tile = addToChain(game);
			if (tile == null) {
				tile = tiles.get(random.nextInt(tiles.size()));
			}
			game.placeTile(tile, this);
		} else {
			game.placeTile(tile, this);
			ChainType typ = game.getStartableChains().get(0);
			game.startChain(typ, tile);
			if (typ.getOutstandingSharesCount() > 1) {
				acquireStock(typ, 1);
			}
		}
		System.out.println("placed tile: "+tile);
		tiles.remove(tile);
	}

	private Hotel getChainStarter(Game game) {
		Hotel starter = null;
		if (game.getStartableChains().size() > 0) {
			List<Hotel> playedTiles = game.getPlayedTiles();

			for (int i = 0; (starter == null) && (i < playedTiles.size()); i++) {
				Hotel played = playedTiles.get(i);
				for (int j = 0; (starter == null) && (j < tiles.size()); j++) {
					Hotel tile = tiles.get(j);
					if (played.isAdjacent(tile) && (game.getAffiliation(played) == null) && (game.getConnections(tile).size() == 0)) {
						starter = tile;
					}
				}
			}
		}
		return starter;
	}

	private Hotel addToChain(Game game) {
		List<Hotel> playedTiles = game.getPlayedTiles();

		Hotel adder = null;
		for (int i = 0; (adder == null) && (i < playedTiles.size()); i++) {
			Hotel played = playedTiles.get(i);
			Chain chain = game.getAffiliation(played);
			for (int j = 0; (chain != null) && (adder == null) && (j < tiles.size()); j++) {
				Hotel tile = tiles.get(j);
				if (played.isAdjacent(tile) && stocks.containsKey(chain)) {
					adder = tile;
				}
			}
		}
		return adder;
	}

	private void buyStock(Game game) {
		int purchaseCount = 0;
		for (;;) {
			Chain chain = getBuyableStock(game.getActiveChains());
			if ((chain == null) || purchaseCount >= 3) break;
			buyStock(chain.getType(), 1, chain.getStockPrice());
			purchaseCount++;
		}
	}
	
	private Chain getBuyableStock(List<Chain> activeChains) {
		Chain foundChain = null;
		for (int i = 0; (foundChain == null) && (i < activeChains.size()); i++) {
			Chain chain = activeChains.get(i);
			if ((chain.getType().getOutstandingSharesCount() > 1) && (chain.getStockPrice() < cash)) {
				foundChain = chain;
			}
		}
		return foundChain;
	}
}
