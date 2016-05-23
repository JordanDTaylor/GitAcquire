package halladay.acquire;

import java.util.ArrayList;

public class Chain {
	
	private static final int SAFE_SIZE = 40;
	
	private ChainType chainType;
	private ArrayList<Hotel> hotels = new ArrayList<Hotel>();
	
	public Chain(ChainType chainType) {
		this.chainType = chainType;
	}
	
	public ChainType getType() {
		return chainType;
	}
	
	public boolean connectsTo(Hotel hotel) {
		boolean connects = false;
		for (int i = 0; !connects && i < hotels.size(); i++) {
			connects = hotel.isAdjacent(hotels.get(i));
		}
		return connects;
	}
	
	public void add(Hotel hotel) {
		assert(connectsTo(hotel));
		hotels.add(hotel);
	}
	
	public boolean isSafe() {
		return hotels.size() > SAFE_SIZE;
	}
	
	public int getHotelCount() {
		return hotels.size();
	}
	
	public void merge(Chain from) {
		for (Hotel h : from.hotels) {
			hotels.add(h);
		}
		from.hotels.clear();
	}
	
	public int getStockPrice() {
		return chainType.getStockPrice(hotels.size());
	}
	
	public boolean contains(Hotel hotel) {
		return hotels.contains(hotel);
	}
}
