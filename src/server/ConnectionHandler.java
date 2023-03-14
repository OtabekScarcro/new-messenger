package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

public class ConnectionHandler implements Runnable{


    // to stop infinite loop in Client
    private final String STOP = "/stopLooping";

    private MessageHandler msgHandler;
    private AccountHandler accountHandler;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private final String connectionId;

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


                    // chat changing something
                    // server doesn't get message after chatting


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
     * @param nickname user's nickname
     * @param matchFriends collection from match friends
     * @return return collection matches
     */
    private ArrayList<String> collectMatches(String nickname, ArrayList<String> matchFriends){
        final String NICKNAMES_LIBRARY = "./users_lib/nicknames.txt";
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
     * @param list getting matched friends list
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

                    final int newPort = generatePort();
                    DirectChat directChat = new DirectChat(friend, this.accountHandler.getNickname(), newPort);
                    out.println("/command");
                    out.println("/setNewPort");
                    out.println(newPort);
                    directChat.run();
                }
            }
        } catch (IOException e) {
            shutdown();
        }
    }

    /**
     * generating new random port number
     * when 2 users start chat directly
     * @return return random port
     */
    private int generatePort(){
        final int portLength = 4;
        Random random = new Random();
        String str = "";
        for(int i=0;i<portLength;i++){
            str += ((Integer)random.nextInt(10)).toString();
        }
        return Integer.parseInt(str);
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
