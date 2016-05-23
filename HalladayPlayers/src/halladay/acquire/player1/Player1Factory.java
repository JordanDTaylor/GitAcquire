package halladay.acquire.player1;

import halladay.acquire.Player;
import halladay.acquire.PlayerFactory;
import halladay.acquire.dumbPlayer.DumbPlayer;

public class Player1Factory implements PlayerFactory {

	private static final String name = "Alice";
	
	@Override
	public Player createPlayer(int startingCash){
		return new DumbPlayer(name, startingCash);
	}
}
