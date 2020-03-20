import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendAudio;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.bots.AbsSender ;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;


public class Tamrin extends TelegramLongPollingBot {
    // name , file
    String nameOfPlayList = null ;
    ArrayList<User> users = new ArrayList<>() ;
    User curUser = null ;

    public void clear_history() {
        ArrayList<Message> history = curUser.getHistory() ;
        if(history.isEmpty())
            return ;
        for(Message message : history) {
            DeleteMessage deleteMessage = new DeleteMessage(curUser.getChatId() , message.getMessageId()) ;
            try {
                execute(deleteMessage) ;
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
        history.clear();
        curUser.setHistory(history) ;
    }

    public void check_history_time() {
        if(curUser == null) {
            return ;
        }
        ArrayList<Message> history = curUser.getHistory() ;
        if(history.isEmpty()) {
            return;
        }
        ArrayList<Message> remove = new ArrayList<>() ;
        for(Message message : history) {
            Date now = new Date() ;
            if(now.getTime() / 1000 - message.getDate() >= 30 * 60 * 60) {
                remove.add(message) ;
                String chatId = message.getChatId().toString();
                Integer messageId = message.getMessageId() ;
                DeleteMessage deleteMessage = new DeleteMessage(chatId , messageId) ;
                try {
                    execute(deleteMessage) ;
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }
        for(Message message : remove)
            history.remove(message) ;
        curUser.setHistory(history) ;
    }

    public void sendMessage(String text) {
        SendMessage message = new SendMessage() ;
        message.setText(text) ;
        message.setChatId(curUser.getChatId()) ;
        System.out.println(curUser.getName() + " " + text);
        try {
            execute(message) ;
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        check_history_time() ;
        boolean alreadyUser = false ;
        for(User user : users) {
            if(user.getChatId() == update.getMessage().getChatId()) {
                curUser = user ;
                alreadyUser = true ;
                break ;
            }
        }
        if(!alreadyUser) {
            System.out.println("shiiiit");
            curUser = new User(update.getMessage()) ;
            users.add(curUser) ;
        }
        System.out.println(curUser.getName());
        String command = curUser.getCommand() ;
        if(command == null) {
            String str = update.getMessage().getText() ;
            if ("/create".equals(str) || "/add".equals(str)) {
                curUser.setCommand("/create") ;
            } else if ("/get".equals(str)) {
                curUser.setCommand("/get") ;
            } else if ("/list".equals(str)) {
                curUser.setCommand(null) ;
                String stringBuilder = "List of your playlists : \n" ;
                for(PlayList playList : curUser.getPlayLists()) {
                    stringBuilder += playList.getName() + "," ;
                }
                sendMessage(stringBuilder) ;
            } else if(str.equals("/start")) {
                String stringBuilder = "Hey welcome to playlist bot , this is a demo version of bot\n" +
                        "contact me : @nima10khodaveisi";
                sendMessage(stringBuilder) ;
            } else if(str.equals("/users")) {
                if(update.getMessage().getFrom().getUserName().equals("Nima10Khodaveisi")) {
                   String string = "list of users : "  ;
                   for(User user : users)
                       string += user.getName() + ", " ;
                   sendMessage(string) ;
                }
            }
        } else if(command.equals("/create")) {
            String name = update.getMessage().getText() ;
            nameOfPlayList = name ;
            curUser.setCommand("name") ;
            curUser.createNewPlayList(name) ;
        } else if(command.equals("name")) {
            if(update.getMessage().getAudio() == null) {
                // /done
                sendMessage(nameOfPlayList + " has been created!") ;
                clear_history() ;
                curUser.setCommand(null) ;
                return ;
            }
            curUser.add(nameOfPlayList , update.getMessage()) ;
            curUser.addToHistory(update.getMessage()) ;
        } else if(command.equals("/get")) {
            curUser.setCommand(null) ;
            String name = update.getMessage().getText() ;
            clear_history() ;
            PlayList playList = curUser.getPlayList(name) ;
            if(playList == null) {
                return ;
            }
            ArrayList<String> songs = playList.getSongs() ;
            for(String song : songs) {
                System.out.println(song);
                SendAudio sendAudio = new SendAudio() ;
                sendAudio.setAudio(song) ;
                sendAudio.setChatId(update.getMessage().getChatId()) ;
                try {
                    Message message = execute(sendAudio) ;
                    curUser.addToHistory(message) ;
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public String getBotUsername() {
        return "MyOwnPlayLists_bot" ;
    }

    @Override
    public String getBotToken() {
        return "928487559:AAEvAZnXgaV5aw8Wzq9kPV1QtW85Lgwl0l8" ;
    }
}