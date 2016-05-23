package halladay.acquire.framework;

import halladay.acquire.Chain;
import halladay.acquire.ChainType;
import halladay.acquire.Game;
import halladay.acquire.Hotel;
import halladay.acquire.Location;
import halladay.acquire.Player;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;

@SuppressWarnings("serial")
public class AcquireGUI extends JFrame {

	private GridPanel gridPanel = new GridPanel();
	private PlayersPanel playersPanel;
	private ControlPanel controlPanel;

	public AcquireGUI(Game game, ArrayList<Player> players) {
		super("Acquire");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		BoxLayout boxLayout = new BoxLayout(getContentPane(), BoxLayout.Y_AXIS); // top to bottom
		setLayout(boxLayout);

		add(gridPanel);

		playersPanel = new PlayersPanel(players);
		add(playersPanel);
		
		controlPanel = new ControlPanel(game);
		add(controlPanel);

		pack();
		setVisible(true);
	}

	public void update(Game game, Player player) {
		gridPanel.update(game);
		playersPanel.update(player);
	}

	private static class GridPanel extends JPanel {

		private CellPane[][] cells = new CellPane[Game.N_ROWS][Game.N_COLS];

		public GridPanel() {
			GridLayout gridLayout = new GridLayout(Game.N_ROWS, Game.N_COLS);
			setLayout(gridLayout);
			for (int i = 0; i < Game.N_ROWS; i++) {
				for (int j = 0; j < Game.N_COLS; j++) {
					CellPane pane = new CellPane(i,j);
					cells[i][j] = pane;
					add(pane);
				}
			}
		}

		public void update(Game game) {
			for (Hotel hotel : game.getPlayedTiles()) {
				Location loc = hotel.getLocation();
				ChainType typ = getChainType(game, hotel);
				if (typ == null) {
					cells[loc.getRow()][loc.getCol()].fill();
				} else {
					cells[loc.getRow()][loc.getCol()].setChainType(typ);
				}
			}
		}

		private ChainType getChainType(Game game, Hotel hotel) {
			ChainType result = null;

			for (Chain chain : game.getActiveChains()) {
				if (chain.contains(hotel)) {
					result = chain.getType();
					break; // I hate this break
				}
			}

			return result;
		}
	}

	private static class PlayersPanel extends JPanel {

		private ArrayList<PlayerPane> playerPanes = new ArrayList<PlayerPane>();

		public PlayersPanel(ArrayList<Player> players) {
			FlowLayout playersLayout = new FlowLayout();
			setLayout(playersLayout);
			for (Player player: players) {
				PlayerPane pane = new PlayerPane(player);
				playerPanes.add(pane);
				add(pane);
			}
		}

		public void update(Player current) {
			for (PlayerPane pane : playerPanes) {
				pane.update(current);
			}
		}
	}

	private static class CellPane extends JPanel {

		private static final Color FILLED_COLOR = Color.DARK_GRAY;
		private static HashMap<ChainType, Color> colorMap = new HashMap<ChainType, Color>();
		static {
			colorMap.put(ChainType.AMERICAN, Color.BLUE);
			colorMap.put(ChainType.CONTINENTAL, Color.CYAN);
			colorMap.put(ChainType.FESTIVAL, Color.YELLOW);
			colorMap.put(ChainType.IMPERIAL, new Color(200, 0, 150));
			colorMap.put(ChainType.LUXOR, new Color(0, 150, 150));
			colorMap.put(ChainType.TOWER, new Color(255, 0, 0));
			colorMap.put(ChainType.WORLDWIDE, Color.GREEN);
		}

		private JLabel label;

		public CellPane(int row, int col) {
			label = new JLabel(""+(col+1)+Character.toString((char)('A'+row)));
			add(label);
			Border border = new MatteBorder(1, 1, 1, 1, Color.GRAY);
			setBorder(border);
		}

		public void fill() {
			setBackground(FILLED_COLOR);
		}
		public void setChainType(ChainType typ) {
			setBackground(colorMap.get(typ));
		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(50, 50);
		}
	}

	private static class PlayerPane extends JPanel {
		
		private static final int WIDTH = 200;
		private static final int HEIGHT = 250;

		private JLabel name;
		private JLabel cash;
		private JLabel tiles;
		private JLabel[] stocks;
		private Player player;

		public PlayerPane(Player player) {

			this.player = player;

			name = new JLabel(player.getName());
			add(name);
			
			tiles = new JLabel(getTilesString(player.getTiles()));
			add(tiles);

			cash = new JLabel(getCashString(player.getCash()));
			add(cash);
			
			ChainType[] chains = ChainType.values();
			stocks = new JLabel[chains.length];
			for (int i = 0; i < chains.length; i++) {
				stocks[i] = new JLabel(getStockString(chains[i], player.getStockSharesCount(chains[i])));
				add(stocks[i]);
			}

			Border border = new MatteBorder(1, 1, 1, 1, Color.GRAY);
			setBorder(border);
		}
		
		private String getCashString(int cash) {
			return "++++++++++++ Cash: "+cash + " ++++++++++++";
		}
		
		private String getTilesString(List<Hotel> tiles) {
			String tilesString = "";
			for (Hotel h : player.getTiles()) {
				tilesString += h;
			}
			return tilesString;
		}
		
		private String getStockString(ChainType typ, int count) {
			return typ.toString() + ": " + count;
		}

		public void update(Player current) {
			Color background = (current == player)? Color.YELLOW: Color.LIGHT_GRAY;
			setBackground(background);
			name.setText(player.getName());
			cash.setText(getCashString(player.getCash()));
			tiles.setText(getTilesString(player.getTiles()));

			ChainType[] chains = ChainType.values();
			for (int i = 0; i < chains.length; i++) {
				stocks[i].setText(getStockString(chains[i], player.getStockSharesCount(chains[i])));
			}

		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(WIDTH, HEIGHT);
		}
	}
	
	public static class ControlPanel extends JPanel implements ActionListener {
		
		private Game game;
		private JButton pauseResumeButton;
		private static final String PAUSE_STRING = "Pause";
		private static final String RESUME_STRING = "Resume";
		
		public ControlPanel(Game game) {
			this.game = game;
			pauseResumeButton = new JButton("Pause");
			pauseResumeButton.addActionListener(this);
			add(pauseResumeButton);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (game.isPaused()) {
				pauseResumeButton.setText(PAUSE_STRING);
				game.setIsPaused(false);
			} else {
				pauseResumeButton.setText(RESUME_STRING);
				game.setIsPaused(true);
			}
		}
	}
}
