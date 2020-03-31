import com.google.inject.internal.cglib.reflect.$FastMethod;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendAudio;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.bots.AbsSender ;

import com.google.gson.Gson ;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class User {
    private String name ;
    private String command ;
    private long chatId ;
    private ArrayList<PlayList> playLists = new ArrayList<>() ;
    private ArrayList<Pair<Long , Integer>> history = new ArrayList<>() ;
    private File file ;

    public User(Message message) {
        if(message.getFrom().getUserName() != null) {
            name = message.getFrom().getUserName() ;
        } else {
            name = message.getFrom().getFirstName() + "-" + message.getFrom().getLastName() ;
        }
        chatId = message.getChatId() ;
        command = null ;

        createFile() ;
        updateFile();
    }

    public User() { }

    private void createFile( ) {
        File users = new File("users") ;
        users.mkdir() ;

        file = new File("users" + File.separator + String.valueOf(chatId) + ".json") ;
        try {
            file.createNewFile() ;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateFile() {
        Gson gson = new Gson() ;
        String json = gson.toJson(this) ;
        try {
            FileWriter fileWriter = new FileWriter(file) ;
            fileWriter.write(json) ;
            fileWriter.flush();
            fileWriter.close() ;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createNewPlayList(String name) {
        PlayList playList = new PlayList(name) ;
        playLists.add(playList) ;
        updateFile();
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
        updateFile();
    }

    public PlayList getPlayList(String name) {
        for(PlayList playList : playLists) {
            if(playList.getName().equals(name))
                return playList ;
        }
        return null ;
    }

    public void addToHistory(Pair<Long,Integer> message) {
        System.out.println("add to history");
        history.add(message) ;
        updateFile();
    }

    public void removeFromHistory(Pair<Long , Integer> message) {
        history.remove(message) ;
        updateFile();
    }

    public long getChatId() {
        return chatId;
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
        updateFile();
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
        updateFile();
    }

    public ArrayList<Pair<Long,Integer>> getHistory() {
        return history;
    }

    public void setHistory(ArrayList<Pair<Long,Integer>> history) {
        this.history = history;
        updateFile();
    }
}
