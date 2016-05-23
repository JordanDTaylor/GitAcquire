package halladay.acquire;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class TileContainer implements Iterable<Hotel> {
	
	private static Random random = RandomFactory.getRandom();

	private ArrayList<Hotel> tiles = new ArrayList<Hotel>();

	public TileContainer() {
	}

	public TileContainer(int nRows, int nCols) {
		for (int row = 0; row < nRows; row++) {
			for (int col = 0; col < nCols; col++) {
				tiles.add(new Hotel(new Location(row, col)));
			}
		}
	}
	
	public int getSize() {
		return tiles.size();
	}
	
	public Hotel getNextRandom() {
		return tiles.remove(random.nextInt(tiles.size()));
	}
	
	public List<Hotel> getNextNRandom(int amount) {
		ArrayList<Hotel> list = new ArrayList<Hotel>();
		for (int i = 0; i < amount; i++) {
			list.add(getNextRandom());
		}
		return list;
	}
	
	@Override
	public Iterator<Hotel> iterator() {
		return tiles.iterator();
	}
}
