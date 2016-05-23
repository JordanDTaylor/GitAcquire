package halladay.acquire;

public enum ChainType {
	TOWER(200),
	LUXOR(200),
	AMERICAN(300),
	WORLDWIDE(300),
	FESTIVAL(300),
	IMPERIAL(400),
	CONTINENTAL(400);
	
	private static final int INIT_SHARE_COUNT = 25;
	private final int startValue;
	private int oustandingSharesCount = INIT_SHARE_COUNT;
	
	ChainType(int startValue) {
		this.startValue = startValue;
	}
	
	public int getStockPrice(int count) {
		int n = 0;
		
		if (count < 6) {
			n = count - 2;
		} else if (count < 11) {
			n = 4;
		} else if (count < 21) {
			n = 5;
		} else if (count < 31) {
			n = 6;
		} else if (count < 41) {
			n = 7;
		} else {
			n = 8;
		}
		
		return startValue + (n * 100);
	}
	
	public int getFirstBonus(int count) {
		return getStockPrice(count) * 10;
	}
	
	public int getSecondBonus(int count) {
		return getFirstBonus(count) / 2;
	}
	
	public int getOutstandingSharesCount() {
		return oustandingSharesCount;
	}
	
	public void buyShares(int count) {
		assert(count >= oustandingSharesCount);
		oustandingSharesCount -= count;
	}
	
	public void sellShares(int count) {
		oustandingSharesCount += count;
		
		assert(oustandingSharesCount <= INIT_SHARE_COUNT);
	}
}
