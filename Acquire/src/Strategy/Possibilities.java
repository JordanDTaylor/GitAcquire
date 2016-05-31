package Strategy;

import halladay.acquire.Game;
import halladay.acquire.Hotel;
import halladay.acquire.Location;

import java.util.*;
import java.util.stream.Collectors;

class Tile extends Hotel{
    public Tile(Location location) {
        super(location);
    }
    public Tile(Hotel hotel){
        super(hotel.getLocation());
    }
}

class Rack{
    Set<Tile> tiles;

    public Rack(Collection<Tile> tiles) {
        this.tiles = new HashSet<>(tiles);
    }
}

class Possibilities {
    public static Set<Tile> AllTiles; //12c x 9r board

    static {
        AllTiles = new HashSet<Tile>();
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 12; col++) {
                AllTiles.add(new Tile(new Location(row, col)));
            }
        }
    }

    public static void main(String[] args) {
        List<Tile> liveTiles = getLiveTiles();
        Combos.GenerateCombinations(liveTiles.toArray(), 3)
                .forEach(System.out::println);
    }

    public static Set<Tile> getLiveTiles(Game game) {
        Set<Tile> playedTiles = game.getPlayedTiles()
                .stream()
                .map(Tile::new)
                .collect(Collectors.toCollection(HashSet<Tile>::new));

        Set<Tile> allTiles = new HashSet<>(AllTiles);
        allTiles.removeAll(playedTiles);
        return allTiles;
    }

    public static List<Tile> getLiveTiles() {
        List<Tile> liveTiles = new ArrayList<>();
        liveTiles.add(new Tile(new Location(0, 0)));
        liveTiles.add(new Tile(new Location(0, 1)));
        liveTiles.add(new Tile(new Location(1, 0)));
        liveTiles.add(new Tile(new Location(1, 1)));
        return liveTiles;
    }
}
class Combos{
    public static <T> ArrayList<ArrayList<T>> GenerateCombinations(T[] arr, int size) {
        return GenerateCombinations(arr)
                .stream()
                .filter(l -> l.size() == size)
                .collect(Collectors.toCollection(ArrayList<ArrayList<T>>::new));
    }

    public static <T> ArrayList<ArrayList<T>> GenerateCombinations(T[] arr) {
        ArrayList<ArrayList<T>> combinations = new ArrayList<>();
        int length = arr.length;

        for (int i = 0; i < (1 << length); ++i) {
            ArrayList<T> combination = new ArrayList<>();
            int count;

            for (count = 0; count < length; ++count) {
                if ((i & 1 << count) > 0)
                    combination.add(arr[count]);
            }

            if (count > 0 && combination.size() > 0)
                combinations.add(combination);
        }
        return combinations;
    }
}
