package halladay.acquire.framework;

import halladay.acquire.Game;
import halladay.acquire.Player;
import halladay.acquire.PlayerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ServiceLoader;

public class Main {
	
	private static final int INTERMOVE_DELAY = 100;
	private static final int INTERGAME_DELAY = 5000;
	
	private static HashMap<String, Integer> playerScores = new HashMap<>();

	public static void main(String[] args) throws IOException {
		ArrayList<PlayerFactory> factories = getFactoryList();
		ArrayList<ArrayList<PlayerFactory>> factoryGroups = chooseN(new ArrayList<PlayerFactory>(), factories, 3);
		for (ArrayList<PlayerFactory> group : factoryGroups) {
			ArrayList<Player> players = createPlayers(group);
			play(players);
		}
		displayOverallScores();
	}

	private static ArrayList<Player> createPlayers(ArrayList<PlayerFactory> group) {
		ArrayList<Player> players = new ArrayList<>();
		for (PlayerFactory factory : group) {
			Player player = factory.createPlayer(Game.STARTING_CASH);
			players.add(player);
		}
		return players;
	}

	private static void play(ArrayList<Player> players) {
		Game game = new Game();
		AcquireGUI gui = new AcquireGUI(game, players);
		game.setDelay(INTERMOVE_DELAY);
		Controller controller = new Controller(game, gui);
		game.register(controller);
		
		for (Player player : players) {
			game.addPlayer(player);
		}
		game.play();
		System.out.println("RESULTS:");
		game.displayScores();
		game.scorePlayers(playerScores);
		
		game.unregister(controller);
		try {
			Thread.sleep(INTERGAME_DELAY);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		gui.dispose();
	}

	private static ArrayList<PlayerFactory> getFactoryList() throws IOException {
		File loc = new File("playerPlugins");

		File[] flist = loc.listFiles(
				new FileFilter() {
					public boolean accept(File file) {
						return file.getPath().toLowerCase().endsWith(".jar");
					}
				}
				);

		URL[] urls = new URL[flist.length];
		for (int i = 0; i < flist.length; i++) {
			urls[i] = flist[i].toURI().toURL();
		}
		URLClassLoader ucl = new URLClassLoader(urls);

		ServiceLoader<PlayerFactory> sl = ServiceLoader.load(PlayerFactory.class, ucl);

		ArrayList<PlayerFactory> factories = new ArrayList<PlayerFactory>();
		Iterator<PlayerFactory> apit = sl.iterator();
		while (apit.hasNext()) {
			factories.add(apit.next());
		}
		return factories;
	}

	@SuppressWarnings("unchecked")
	private static ArrayList<ArrayList<PlayerFactory>> chooseN(ArrayList<PlayerFactory> mustHave, ArrayList<PlayerFactory> mightHave, int n) {
		ArrayList<ArrayList<PlayerFactory>> result = null;
		if (mustHave.size() + mightHave.size() < n) {
			result = new ArrayList<ArrayList<PlayerFactory>>();
		} else if (mustHave.size() < n) {
			PlayerFactory temp = mightHave.remove(0);
			result = chooseN(mustHave, mightHave, n);
			mustHave.add(temp);
			ArrayList<ArrayList<PlayerFactory>> withResult = chooseN(mustHave, mightHave, n);
			mustHave.remove(temp);
			mightHave.add(0,temp);
			result.addAll(withResult);
		} else {
			result = new ArrayList<ArrayList<PlayerFactory>>();
			result.add((ArrayList<PlayerFactory>) mustHave.clone());
		}
		return result;
	}
	
	private static void displayOverallScores() {
		System.out.println("Overall scores:");
		for (String p : playerScores.keySet()) {
			int score = playerScores.get(p);
			System.out.println(p + ": " + score);
		}
	}
	
	public static class Controller implements Game.Listener{
		
		private AcquireGUI gui;
		private Game game;
		
		public Controller(Game game, AcquireGUI gui) {
			this.game = game;
			this.gui = gui;
		}

		@Override
		public void playComplete(Player player) {
			gui.update(game, player);
		}
		
	}
}
