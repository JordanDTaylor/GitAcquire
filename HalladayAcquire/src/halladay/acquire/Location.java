package halladay.acquire;


public class Location implements Comparable<Location> {
	private final int row;
	private final int col;

	public Location(int row, int col) {
		this.row = row;
		this.col = col;
	}

	public Location(char row, int col) {
		this.row = Character.toLowerCase(row) - 'a';
		this.col = col - 1;
	}

	public int getRow() {
		return this.row;
	}

	public int getCol() {
		return this.col;
	}

	public String getDisplayRow() {
		return Character.toString((char) (row + 'A'));
	}

	public String getDisplayCol() {
		return Integer.toString(col+1);
	}

	public boolean isAdjacent(Location that) {
		boolean isAbove = (that.row == this.row-1) && (this.col == that.col);
		boolean isBelow = (that.row == this.row+1) && (this.col == that.col);
		boolean isLeft = (that.row == this.row) && (this.col == that.col+1);
		boolean isRight = (that.row == this.row) && (this.col == that.col-1);
		return isAbove || isBelow || isLeft || isRight;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + col;
		result = prime * result + row;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Location other = (Location) obj;
		if (col != other.col)
			return false;
		return row == other.row;
	}

	@Override
	public int compareTo(Location o) {
		Location that = o;
		int result = that.col - this.col;
		if (result == 0) result = that.row - this.row;
		return result;
	}

	@Override
	public String toString() {
		return "(" + (col + 1) +"," + Character.toString((char) (row + 'A'))+")";
	}


}
