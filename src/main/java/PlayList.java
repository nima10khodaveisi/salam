import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.ArrayList;

public class PlayList {
    private String name ;
    private ArrayList<String> songs = new ArrayList<>() ;

    public void add(Message message) {
        songs.add(message.getAudio().getFileId()) ;
    }

    public PlayList(String name) {
        this.name = name ;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<String> getSongs() {
        return songs;
    }

    public void setSongs(ArrayList<String> songs) {
        this.songs = songs;
    }
}
