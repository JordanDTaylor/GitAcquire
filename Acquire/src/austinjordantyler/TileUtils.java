package austinjordantyler;

import halladay.acquire.Chain;
import halladay.acquire.Game;
import halladay.acquire.Hotel;
import halladay.acquire.Location;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TileUtils {
    public static final double MAX_DISTANCE_FROM_CENTER =
            Math.sqrt(Math.pow(Game.N_ROWS / 2, 2) + Math.pow(Game.N_COLS / 2, 2));

    /**
     * Returns the distance of a tile from the center of the board where 0.0 is the center.
     * The largest this number can be is equal to MAX_DISTANCE_FROM_CENTER.
     */
    public static double tileDistanceFromCenter(Game game, Hotel tile) {
        Location center = new Location(Game.N_ROWS / 2, Game.N_COLS / 2);
        Location tileLocation = tile.getLocation();
        int xDistance = Math.abs(center.getCol() - tileLocation.getCol());
        int yDistance = Math.abs(center.getRow() - tileLocation.getRow());

        // a^2 + b^2 = c^2
        return Math.sqrt(xDistance * xDistance + yDistance * yDistance);
    }

    public static List<Hotel> getTilesInChain(Game game, Chain chain) {
        List<Hotel> chainTiles = new ArrayList<>(chain.getHotelCount());
        chainTiles.addAll(game.getPlayedTiles().stream()
                .filter(chain::contains)
                .collect(Collectors.toList()));
        return chainTiles;
    }

    public static List<Hotel> getConnectingLoneTiles(Game game, Hotel tile) {
        return getLoneTiles(game).stream()
                .filter(loneTile -> loneTile.isAdjacent(tile))
                .collect(Collectors.toList());
    }

    public static List<Hotel> getLoneTiles(Game game) {
        return game.getPlayedTiles().stream()
                .filter(playedTile -> game.getAffiliation(playedTile) == null)
                .collect(Collectors.toList());
    }

    public static Set<Hotel> AllTiles; //12c x 9r board

    static {
        AllTiles = new HashSet<>();
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 12; col++) {
                AllTiles.add(new Hotel(new Location(row, col)));
            }
        }
    }

    public static Set<Hotel> getUnplayedTiles(Game game) {
        Set<Hotel> playedTiles = game.getPlayedTiles()
                .stream()
                .collect(Collectors.toCollection(HashSet<Hotel>::new));
        Set<Hotel> allTiles = new HashSet<>(AllTiles);
        allTiles.removeAll(playedTiles);
        return allTiles;
    }

    public static Set<Hotel> getUnplayedLiveTiles(Game game){
        Set<Hotel> unPlayedTiles = getUnplayedTiles(game);
        return unPlayedTiles.stream()
                .filter(t->game.isPlayable(t))
                .collect(Collectors.toCollection(HashSet<Hotel>::new));
    }

}
