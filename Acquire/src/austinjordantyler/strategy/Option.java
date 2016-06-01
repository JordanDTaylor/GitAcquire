package austinjordantyler.strategy;

import halladay.acquire.ChainType;

public class Option implements Comparable<Option>{
	
	private final int totalCost;
	private final int amountToBuy;
	private final int totalValue;
	private final ChainType type;
	private final int costOfShare;

	public Option(int totalCost, int amountToBuy, int totalValue, ChainType chainType, int costOfShare) {
		this.totalCost = totalCost;
		this.amountToBuy = amountToBuy;
		this.totalValue = totalValue;
		this.type = chainType;
		this.costOfShare = costOfShare;
	}

	@Override
	public int compareTo(Option other) {
		return (totalCost/totalValue) - (other.totalCost/other.totalValue);
	}

	public int getAmountToBuy() {
		return amountToBuy;
	}

	public ChainType getType() {
		return type;
	}
	
	public double costValue() {
		return (double)totalCost/(double)totalValue;
	}

	public int getCostOfShare() {
		return costOfShare;
	}

}
