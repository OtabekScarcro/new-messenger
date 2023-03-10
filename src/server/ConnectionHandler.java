package server;

import java.io.*;
import java.net.Proxy;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ConnectionHandler implements Runnable{

    // Users library path
    private final String USERS_LIBRARY_FILE = "./users_lib/users.csv";
    private final int NICKNAME_ROW = 1;
    private MessageHandler msgHandler;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public ConnectionHandler(Socket socket){
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            msgHandler = new MessageHandler();

            while(true){
                String msgFromClient = in.readLine();
                if(msgFromClient.equals("/command")){
                    commands();
                }
            }
        } catch (IOException e){
            shutdown();
        }
    }

    private void commands(){
        try {
            String command = in.readLine();
            if(command.equals("searchFriend")){
                selectNewFriend(searchForNickname());
            }
        } catch (IOException e){
            shutdown();
        }
    }

    /**
     * searching compatible nicknames and collect them
     * @return
     */
    private Map<String, Socket> searchForNickname(){
        Map<String, Socket> matchFriends = new HashMap<String, Socket>();
        try (BufferedReader br = new BufferedReader(new FileReader(USERS_LIBRARY_FILE))){

            // ask for nickname client want to search
            String nickname = msgHandler.rmSpaces(in.readLine());
            while(msgHandler.searchForNickname(nickname)){
                out.println("This nickname is not existed in Server");
                out.println("Please try with another name: ");
                nickname = msgHandler.rmSpaces(in.readLine());
            }

            // search that from Server
            String line;
            while((line = br.readLine()) != null){
                String[] str = line.split(",");
                if(str[NICKNAME_ROW].contains(nickname)){
                    matchFriends.put(str[NICKNAME_ROW], Server.usersList.get(str[NICKNAME_ROW]));
                }
            }
        } catch (IOException e){
            shutdown();
        }
        return matchFriends;
    }

    /**
     * selecting one friend from the collection
     * @param list
     */
    private void selectNewFriend(Map<String, Socket> list){
        int length = list.size();

        // printing matched friends
        int index = 0;
        for(String name : list.keySet()){
            out.println((++index) + ". " + name);
        }

        // selecting step
        try {
            out.println("Please select the right number: ");
            String option = msgHandler.rmSpaces(in.readLine());
            int num = Integer.parseInt(option);
            while(num < 1 || num > length){
                out.println("Please select the right number: ");
                option = msgHandler.rmSpaces(in.readLine());
                num = Integer.parseInt(option);
            }

            // sending command to break infinite loop
            // in new friends section
            out.println("/command");
            out.println("/stopLooping");

            out.println("/command");
            out.println("/addFriend");
            out.println(list.get(num-1));
        } catch (IOException e){
            //TODO: handle
        }

    }

    private void shutdown(){

    }
}
