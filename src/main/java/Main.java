
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Timer;
import java.util.TimerTask;

public class Main {
    public static void main(String[] args) {
        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try {
            Tamrin myBot = new Tamrin() ;
            telegramBotsApi.registerBot(myBot);
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    myBot.deleteSent();
                }
            } ;
            Timer timer = new Timer() ;
            timer.scheduleAtFixedRate(timerTask, 1000, 20 * 60 * 60 * 1000) ;
            //timer.scheduleAtFixedRate(timerTask, 1000,1000) ;
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        //timerTask
    }
}
