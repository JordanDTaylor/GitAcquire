package halladay.acquire;


public class Hotel implements Comparable<Hotel>{
	private final Location location;

	public Hotel(Location location) {
		this.location = location;
	}

	public Location getLocation() {
		return location;
	}

	public boolean isAdjacent(Hotel that) {
		return this.location.isAdjacent(that.location);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((location == null) ? 0 : location.hashCode());
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
		Hotel other = (Hotel) obj;
		if (location == null) {
			if (other.location != null)
				return false;
		} else if (!location.equals(other.location))
			return false;
		return true;
	}

	@Override
	public int compareTo(Hotel o) {
		Hotel that = o;
		return location.compareTo(that.location);
	}

	@Override
	public String toString() {
		return location.toString();
	}


}
