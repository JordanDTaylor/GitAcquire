package halladay.acquire;

import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

public class Logger {
    private static String logFile = "";
    private static boolean initialized = false;

    public static void GameMessageLog(String message){
        //do nothing
    }
    public static void Log(String message){
        if(!initialized){
            logFile = "log_" + UUID.randomUUID() + ".txt";
            initialized = true;
        }
        try (FileWriter fw = new FileWriter(logFile)){
            fw.append(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
