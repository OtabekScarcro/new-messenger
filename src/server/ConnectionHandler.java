package server;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ConnectionHandler implements Runnable{

    private final String NICKNAMES_LIBRARY = "./users_lib/nicknames.txt";
    private final String CHAT_HISTORY_DIR = "./users_lib/";

    // to stop infinite loop in Client
    private final String STOP = "/stopLooping";

    private MessageHandler msgHandler;
    private AccountHandler accountHandler;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String connectionId;

    public ConnectionHandler(Socket socket, String connectionId){
        this.socket = socket;
        this.connectionId = connectionId;
    }

    @Override
    public void run() {
        try {
            accountHandler = new AccountHandler(socket, connectionId);
            accountHandler.run();

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
            if(command.equals("/searchFriend")){
                searchForNickname();
            }
        } catch (IOException e){
            shutdown();
        }
    }

    /**
     * searching compatible nicknames and collect them
     * @return
     */
    private void searchForNickname(){
        ArrayList<String> matchFriends = new ArrayList<>();
        try {
            // ask for nickname client want to search
            out.println("Enter a nickname you want to search (\"/quit\" to go back to the main Window): ");
            String nickname = msgHandler.rmSpaces(in.readLine());
            if(msgHandler.sanitize(nickname).equals("/quit")){
                // sending command to break infinite loop
                // in new friends section
                out.println("/command");
                out.println(STOP);
                out.println("Press enter to go to the main window!");
            }
            else {
                matchFriends = collectMatches(nickname, matchFriends);
                while(matchFriends.size() == 0){
                    out.println("There is no such user in Server!");
                    out.println("Please try with another one: ");
                    nickname = msgHandler.rmSpaces(in.readLine());
                    if(nickname.equals("/quit")){
                        out.println("/command");
                        out.println(STOP);
                        out.println("Press enter to go to the main window!");
                        return;
                    }
                    matchFriends = collectMatches(nickname, matchFriends);
                }
                selectNewFriend(matchFriends);
            }
        } catch (IOException e){
            shutdown();
        }
    }

    /**
     * collect matches with a given nickname
     * @param nickname
     * @param matchFriends
     * @return
     */
    private ArrayList<String> collectMatches(String nickname, ArrayList<String> matchFriends){
        try (BufferedReader br = new BufferedReader(new FileReader(NICKNAMES_LIBRARY))) {
            // search that nickname from Server
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains(nickname) && (!line.equals(accountHandler.getNickname()))) {
                    matchFriends.add(line);
                }
            }
        } catch (IOException e){
            // TODO: handle
        }
        return matchFriends;
    }

    /**
     * selecting one friend from the collection
     * @param list
     */
    private void selectNewFriend(ArrayList<String> list){
        // printing matched friends
        int length = list.size();
        for (int i = 1; i <= length; i++) {
            out.println(i + ". " + list.get(i - 1));
        }
        // selecting step
        try {
            out.println("Please select the right number (0 for none): ");
            String option = msgHandler.rmSpaces(in.readLine());
            int num = Integer.parseInt(option);
            while (num < 0 || num > length) {
                out.println("Please select the right number: ");
                option = msgHandler.rmSpaces(in.readLine());
                num = Integer.parseInt(option);
            }
            if(num == 0){
                // sending command to break infinite loop
                // in new friends section
                out.println("/command");
                out.println(STOP);
                out.println("Press enter to go to the main window!");
            }
            else {
                // send a command to add a particular friend
                String friend = list.get(num-1);
                out.println("/command");
                out.println("/addFriend");
                out.println(friend);

                // sending command to break infinite loop
                // in new friends section
                out.println("/command");
                out.println(STOP);

                // last message in infinite loop
                // it will be the answer to start chat or not
                out.println("Do you want to start chat with that user (y/n): ");
                String isChatStarted = msgHandler.sanitize(in.readLine());
                if(isChatStarted.equals("yes") || isChatStarted.equals("y")){
                    // call saveChat() method to save messages
                    saveChat(accountHandler.getNickname(), friend);
                }
                else {
                    // Back to the main menu
                }
            }
        } catch (IOException e) {
            //TODO: handle
        }
    }

    /**
     * saving messages from current user
     * @param me
     * @param friend
     */
    private void saveChat(String me, String friend){
        // generate unique file name
        int uuidNum = 0;
        for(byte b : me.getBytes()){
            uuidNum += b;
        }
        for(byte b : friend.getBytes()){
            uuidNum += b;
        }
        final String FILE_NAME = (uuidNum) + ".txt";

        try (FileOutputStream fos = new FileOutputStream(CHAT_HISTORY_DIR+FILE_NAME, true)){
            String message = in.readLine();
            while (!message.equals("/quit")){
                fos.write((me + ": " + message).getBytes());
                fos.write(System.lineSeparator().getBytes());
                message = in.readLine();
            }
        } catch (IOException e){
            // TODO: handle
        }
    }

    /**
     * class to save messages from friend
     */
    class SaveFromFriend implements Runnable{
        private Socket socket;
        private String friend;
        private final String PATH;
        public SaveFromFriend(String friend, String PATH){
            this.socket = Server.usersList.get(friend);
            this.friend = friend;
            this.PATH = PATH;
        }

        @Override
        public void run() {
            String msg;
            try (FileOutputStream fos = new FileOutputStream(PATH, true);
                 BufferedReader inFromFriend = new BufferedReader(new InputStreamReader(socket.getInputStream())))
            {
                while(!(msg = inFromFriend.readLine()).equals("/quit")){
                    fos.write((friend + ": " + msg).getBytes());
                    fos.write(System.lineSeparator().getBytes());
                }
            } catch (IOException e){
                // TODO: handle
            }
        }
    }

    /**
     * shutdown everything
     */
    private void shutdown(){
        try {
            if (!socket.isClosed()) {
                socket.close();
            }
            in.close();
            out.close();
        } catch (IOException e){
            // ignore
        }
    }
}
