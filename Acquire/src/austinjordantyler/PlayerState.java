package austinjordantyler;

import halladay.acquire.Chain;
import halladay.acquire.ChainType;
import halladay.acquire.Game;
import halladay.acquire.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class PlayerState extends Player {

    public static PlayerState fromPlayer(Player player, Game game){
        PlayerState state = new PlayerState(player.getName(), player.getCash());
        HashMap<ChainType, Integer> stocks = new HashMap<>();
        Arrays.stream(ChainType.values()).forEach(chainType -> stocks.put(chainType, player.getStockSharesCount(chainType)));
        state.addTiles(player.getTiles());
        return state;
    }

    public PlayerState(String name, int startingCash) {
        super(name, startingCash);
    }

    @Override
    public void play(Game game) {
        throw new RuntimeException("play has not been implemented");
    }

    @Override
    protected Chain selectWinner(List<Chain> chains) {
        throw new RuntimeException("selectWinner has not been implemented");
    }

    @Override
    protected void resolveMergedStock(Chain winner, List<Chain> mergers) {
        throw new RuntimeException("resolveMergedStock has not been implemented");
    }
}
