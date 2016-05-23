package halladay.acquire.player4;

import halladay.acquire.Player;
import halladay.acquire.PlayerFactory;
import halladay.acquire.dumbPlayer.DumbPlayer;

public class Player4Factory implements PlayerFactory {

	private static final String name = "Diane";
	
	@Override
	public Player createPlayer(int startingCash){
		return new DumbPlayer(name, startingCash);
	}
}
