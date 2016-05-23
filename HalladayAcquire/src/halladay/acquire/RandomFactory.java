package halladay.acquire;

import java.util.Random;

public class RandomFactory {

	private static Random random = new Random(37);
	
	public static Random getRandom() {
		return random;
	}
}
