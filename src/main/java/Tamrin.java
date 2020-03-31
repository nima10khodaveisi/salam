
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendAudio;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.bots.AbsSender;


import com.google.gson.Gson ;

import java.io.*;
import java.lang.reflect.Method;
import java.util.*;


public class Tamrin extends TelegramLongPollingBot {
    // name , file
    private String nameOfPlayList = null;
    private ArrayList<User> users = new ArrayList<>();
    private User curUser = null;
    // { { date , audio } , { chatId , messageId } }
    private ArrayList<String> sent = new ArrayList<>() ;

    public void clear_history() {
        ArrayList<Pair<Long,Integer>> history = curUser.getHistory();
        if (history.isEmpty())
            return;
        for (Pair<Long,Integer> message : history) {
            DeleteMessage deleteMessage = new DeleteMessage(curUser.getChatId(), message.getValue());
            try {
                execute(deleteMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
        history.clear();
        curUser.setHistory(history);
    }


    public Message sendMessage(String text) {
        SendMessage message = new SendMessage();
        message.setText(text);
        message.setChatId(curUser.getChatId());
        System.out.println(curUser.getName() + " " + text);
        try {
            return execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        return null ;
    }

    public void deleteSent() {
        setSent();
        ArrayList<String> remove = new ArrayList<>() ;
        ArrayList<String> add = new ArrayList<>() ;
        if(sent.isEmpty()) return ;
        for(int i = 0 ; i < sent.size() ; ++i) {
            String message = sent.get(i) ;
            String[] split = message.split(" ") ;
            long now = System.currentTimeMillis() ;
            long messageTime = Integer.parseInt(split[0]) ;
            if(now / 1000 - messageTime >= 20 * 60 * 60){
                remove.add(message) ;

                DeleteMessage deleteMessage = new DeleteMessage(Long.parseLong(split[2]), Integer.valueOf(split[3])) ;
                SendAudio sendAudio = new SendAudio() ;
                sendAudio.setAudio(split[1]);
                sendAudio.setChatId(Long.parseLong(split[2])) ;
                try {
                    execute(deleteMessage) ;
                    Message message1 = execute(sendAudio) ;
                    add.add(String.valueOf(message1.getDate()) + " " + message1.getAudio().getFileId() + " " + String.valueOf(message1.getChatId()) + " " + String.valueOf(message1.getMessageId())) ;
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }
        for(String message : remove)
            sent.remove(message) ;
        for(String message : add)
            sent.add(message) ;
        updateSentFile() ;
    }

    public void updateSentFile() {
        File file = new File("sent.json") ;
        try {
            file.createNewFile() ;
            Gson gson = new Gson() ;
            String json = gson.toJson(sent) ;
            FileWriter fw = new FileWriter(file) ;
            fw.write(json) ;
            fw.flush();
            fw.close() ;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setSent() {
        Gson gson = new Gson() ;
        try {
            if(new File("sent.json").isFile())
                sent = gson.fromJson(new FileReader(new File("sent.json")) , ArrayList.class) ;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        boolean alreadyUser = false ;
        File usersFile = new File("users") ;
        usersFile.mkdir() ;
        File []files = usersFile.listFiles() ;
        for(File file : files) {
            if(file.isFile()) {
                long chatId = update.getMessage().getChatId() ;
                String name = file.getName() ;
                if(name.equals(String.valueOf(chatId) + ".json")) {
                    alreadyUser = true ;
                    Gson gson = new Gson() ;
                    try {
                        curUser = gson.fromJson(new FileReader(file) , User.class) ;
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    break ;
                }
            }
        }


        if (!alreadyUser) {
            curUser = new User(update.getMessage());
            users.add(curUser);
        }
        System.out.println(curUser.getName() + " " + update.getMessage().getText());
        String command = curUser.getCommand();
        if (command == null) {
            String str = update.getMessage().getText();
            if ("/create".equals(str) || "/add".equals(str)) {


                String stringBuilder = "please enter the name of playlist";
                SendMessage message = new SendMessage().setChatId(curUser.getChatId()).setText(stringBuilder) ;


                ReplyKeyboardRemove replyKeyboardRemove = new ReplyKeyboardRemove() ;
                message.setReplyMarkup(replyKeyboardRemove) ;

                try {
                    execute(message) ;
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }


                curUser.setCommand("/create");
            } else if ("/get".equals(str)) {


                String stringBuilder = "choose the playlist you want to listen";
                SendMessage message = new SendMessage().setChatId(curUser.getChatId()).setText(stringBuilder) ;


                ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup() ;

                List<KeyboardRow> keyboardRows = new ArrayList<>() ;

                KeyboardRow row = new KeyboardRow() ;

                for(PlayList playList : curUser.getPlayLists()) {
                    row.add(playList.getName()) ;
                    if(row.size() == 3) {
                        keyboardRows.add(row);
                        row = new KeyboardRow();
                    }
                }
                if(row.size() > 0)
                    keyboardRows.add(row) ;

                keyboardMarkup.setKeyboard(keyboardRows) ;
                message.setReplyMarkup(keyboardMarkup) ;

                try {
                    execute(message) ;
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }




                curUser.setCommand("/get");
            } else if ("/list".equals(str)) {
                curUser.setCommand(null);
                String stringBuilder = "List of your playlists : \n";
                for (PlayList playList : curUser.getPlayLists()) {
                    stringBuilder += playList.getName() + ",";
                }
                sendMessage(stringBuilder);
            } else if (str.equals("/start")) {
                String stringBuilder = "Hey welcome to playlist bot";
                SendMessage message = new SendMessage().setChatId(curUser.getChatId()).setText(stringBuilder) ;


                ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup() ;

                List<KeyboardRow> keyboardRows = new ArrayList<>() ;

                KeyboardRow row = new KeyboardRow() ;

                row.add("/create") ;
                row.add("/get") ;
                row.add("/list") ;

                keyboardRows.add(row) ;
                keyboardMarkup.setKeyboard(keyboardRows) ;
                message.setReplyMarkup(keyboardMarkup) ;

                try {
                    execute(message) ;
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }



            } else if (str.equals("/users")) {
                if (update.getMessage().getFrom().getUserName().equals("Nima10Khodaveisi")) {
                    String string = "list of users : ";
                    for (User user : users)
                        string += user.getName() + ", ";
                    sendMessage(string);
                }
            } else if(str.equals("/get_data")) {
                    if(update.getMessage().getFrom().getUserName().equals("Nima10Khodaveisi")) {
                        SendDocument sendDocument = new SendDocument() ;
                        ZipCompress.compress("users") ;
                        try {
                            sendDocument.setDocument("users.zip",new FileInputStream("users.zip")) ;
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        sendDocument.setChatId(curUser.getChatId()) ;
                        try {
                            execute(sendDocument) ;
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    }
            }
        } else if (command.equals("/create")) {

            String stringBuilder = "now send your musics and press /done button when you are done";
            SendMessage message = new SendMessage().setChatId(curUser.getChatId()).setText(stringBuilder) ;


            ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup() ;

            List<KeyboardRow> keyboardRows = new ArrayList<>() ;

            KeyboardRow row = new KeyboardRow() ;

            row.add("/done") ;

            keyboardRows.add(row) ;
            keyboardMarkup.setKeyboard(keyboardRows) ;
            message.setReplyMarkup(keyboardMarkup) ;


            try {
                execute(message) ;
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }


            String name = update.getMessage().getText();
            nameOfPlayList = name;
            curUser.setCommand("name");
            curUser.createNewPlayList(name);
        } else if (command.equals("name")) {
            if (update.getMessage().getAudio() == null) {
                // /done


                String stringBuilder = nameOfPlayList + " has been created!" ;
                SendMessage message = new SendMessage().setChatId(curUser.getChatId()).setText(stringBuilder) ;


                ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup() ;

                List<KeyboardRow> keyboardRows = new ArrayList<>() ;

                KeyboardRow row = new KeyboardRow() ;

                row.add("/create") ;
                row.add("/get") ;
                row.add("/list") ;

                keyboardRows.add(row) ;
                keyboardMarkup.setKeyboard(keyboardRows) ;
                message.setReplyMarkup(keyboardMarkup) ;

                try {
                    execute(message) ;
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }


                clear_history();
                curUser.setCommand(null);
                return;
            }
            curUser.add(nameOfPlayList, update.getMessage());
            curUser.addToHistory(new Pair(update.getMessage().getChatId(),update.getMessage().getMessageId()));
            System.out.println("add " + update.getMessage().getAudio().getTitle());
        } else if (command.equals("/get")) {

            String stringBuilder = "here you are!";
            SendMessage sendMessage = new SendMessage().setChatId(curUser.getChatId()).setText(stringBuilder) ;


            ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup() ;

            List<KeyboardRow> keyboardRows = new ArrayList<>() ;

            KeyboardRow row = new KeyboardRow() ;

            row.add("/create") ;
            row.add("/get") ;
            row.add("/list") ;

            keyboardRows.add(row) ;
            keyboardMarkup.setKeyboard(keyboardRows) ;
            sendMessage.setReplyMarkup(keyboardMarkup) ;

            try {
                execute(sendMessage) ;
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }

            curUser.setCommand(null);
            String name = update.getMessage().getText();
            PlayList playList = curUser.getPlayList(name);
            if (playList == null) {
                return;
            }
            clear_history();
            ArrayList<String> songs = playList.getSongs();
            for (String song : songs) {
                System.out.println(song);
                SendAudio sendAudio = new SendAudio();
                sendAudio.setAudio(song);
                sendAudio.setChatId(update.getMessage().getChatId());
                System.out.println("get " + name + " " + song);
                try {
                    Message message = execute(sendAudio);
                    sent.add(String.valueOf(message.getDate()) + " " + message.getAudio().getFileId() + " " + String.valueOf(message.getChatId()) + " " + String.valueOf(message.getMessageId())) ;
                    updateSentFile() ;
                    curUser.addToHistory(new Pair(message.getChatId(),message.getMessageId())) ;
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public String getBotUsername() {
        return "MyOwnPlayLists_bot";
    }

    @Override
    public String getBotToken() {
        return "928487559:AAEvAZnXgaV5aw8Wzq9kPV1QtW85Lgwl0l8";
    }
}