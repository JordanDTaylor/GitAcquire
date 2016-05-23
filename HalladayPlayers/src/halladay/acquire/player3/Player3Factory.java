package halladay.acquire.player3;

import halladay.acquire.Player;
import halladay.acquire.PlayerFactory;
import halladay.acquire.dumbPlayer.DumbPlayer;

public class Player3Factory implements PlayerFactory {

	private static final String name = "Chuck";
	
	@Override
	public Player createPlayer(int startingCash){
		return new DumbPlayer(name, startingCash);
	}
}
