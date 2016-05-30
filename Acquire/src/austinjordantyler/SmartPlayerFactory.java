package austinjordantyler;

import austinjordantyler.Strategy.IStrategy;
import halladay.acquire.Player;
import halladay.acquire.PlayerFactory;

public class SmartPlayerFactory implements PlayerFactory {

    private static final String name = "SmartPlayer";

    @Override
    public Player createPlayer(int startingCash){
        IStrategy startingStrategy = new EarlyGameStrategy();
        return new SmartPlayer(name, startingCash, startingStrategy);
    }
}
