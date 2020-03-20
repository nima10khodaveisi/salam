import com.google.inject.internal.cglib.reflect.$FastMethod;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendAudio;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.bots.AbsSender ;



import java.util.ArrayList;

public class User {
    private String name ;
    private String command ;
    private long chatId ;
    private ArrayList<PlayList> playLists = new ArrayList<>() ;
    private ArrayList<Message> history = new ArrayList<>() ;

    public User(Message message) {
        if(message.getFrom().getUserName() != null) {
            name = message.getFrom().getUserName() ;
        } else {
            name = message.getFrom().getFirstName() + " " + message.getFrom().getLastName() ;
        }
        chatId = message.getChatId() ;
        command = null ;
    }

    public void createNewPlayList(String name) {
        PlayList playList = new PlayList(name) ;
        playLists.add(playList) ;
    }

    public void add(String name , Message message) {
        if(message.getAudio() == null) {
            return ;
        }
        for(PlayList playList : playLists) {
            if(playList.getName().equals(name)) {
                playList.add(message) ;
                break ;
            }
        }
    }

    public PlayList getPlayList(String name) {
        for(PlayList playList : playLists) {
            if(playList.getName().equals(name))
                return playList ;
        }
        return null ;
    }

    public void addToHistory(Message message) {
        System.out.println("add to history");
        history.add(message) ;
    }

    public void removeFromHistory(Message message) {
        history.remove(message) ;
    }

    public long getChatId() {
        return chatId;
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }

    public ArrayList<PlayList> getPlayLists() {
        return playLists;
    }

    public void setPlayLists(ArrayList<PlayList> playLists) {
        this.playLists = playLists;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public ArrayList<Message> getHistory() {
        return history;
    }

    public void setHistory(ArrayList<Message> history) {
        this.history = history;
    }
}
