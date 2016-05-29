package austinjordantyler;

import halladay.acquire.Game;
import halladay.acquire.Hotel;
import halladay.acquire.Location;

public class TileUtils {
    /**
     * Returns the distance of a tile from the center of the board where 0.0 is the center
     */
    public static double tileDistanceFromCenter(Game game, Hotel tile) {
        Location center = new Location(Game.N_ROWS / 2, Game.N_COLS / 2);
        Location tileLocation = tile.getLocation();
        int xDistance = Math.abs(center.getCol() - tileLocation.getCol());
        int yDistance = Math.abs(center.getRow() - tileLocation.getRow());

        // a^2 + b^2 = c^2
        return Math.sqrt(xDistance * xDistance + yDistance * yDistance);
    }
}
