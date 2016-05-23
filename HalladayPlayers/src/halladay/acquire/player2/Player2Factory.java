package halladay.acquire.player2;

import halladay.acquire.Player;
import halladay.acquire.PlayerFactory;
import halladay.acquire.dumbPlayer.DumbPlayer;

public class Player2Factory implements PlayerFactory {

	private static final String name = "Bob";
	
	@Override
	public Player createPlayer(int startingCash){
		return new DumbPlayer(name, startingCash);
	}
}
