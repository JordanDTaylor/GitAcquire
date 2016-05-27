import Strategy.IStrategy;
import halladay.acquire.Player;
import halladay.acquire.PlayerFactory;

public class SmartPlayerFactory implements PlayerFactory {

    private static final String name = "SmartPlayer";

    @Override
    public Player createPlayer(int startingCash){
        IStrategy a = null;
        IStrategy b=null;
        IStrategy c = null;
        return new SmartPlayer(name, startingCash, a, b, c);
    }
}
