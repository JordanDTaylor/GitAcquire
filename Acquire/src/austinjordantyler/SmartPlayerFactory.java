package austinjordantyler;

import austinjordantyler.strategy.EarlyGameStrategy;
import austinjordantyler.strategy.IStrategy;
import halladay.acquire.Player;
import halladay.acquire.PlayerFactory;

public class SmartPlayerFactory implements PlayerFactory {

    private static final String name = "Austin+Jordan+Tyler AI";

    @Override
    public Player createPlayer(int startingCash){
//        IStrategy startingStrategy = new EarlyGameStrategy();
        IStrategy startingStrategy = new EarlyGameStrategy();
        return new SmartPlayer(name, startingCash, startingStrategy);
    }
}
