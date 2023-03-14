package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class DirectChat implements Runnable{

    private final String friend;
    private final String me;
    private ServerSocket serverSocket;
    private Socket socket;
    private Socket friendSocket;
    private Socket mySocket;
    private final int NEW_PORT;

    /**
     * constructor for this class
     * @param friend  friend's nickname
     * @param me      current user's nickname
     */
    public DirectChat(String friend, String me, int newPort){
        this.friend = friend;
        this.me = me;
        this.NEW_PORT = newPort;
        try {
            serverSocket = new ServerSocket(NEW_PORT);
            mySocket = Server.usersList.get(me);
            friendSocket = Server.usersList.get(friend);
        } catch (IOException e){
            shutdown();
        }
    }

    /**
     * Overriding run() method
     */
    @Override
    public void run() {
        // sending notification to friend about new user
        sendNotification();

        try {
            socket = serverSocket.accept();
            Thread thread = new Thread(new ChatInputHandler(socket));
            thread.start();

            ChatInputHandler chatInputHandler = new ChatInputHandler(serverSocket.accept());
            chatInputHandler.run();

        } catch (IOException e){
            shutdown();
        }
    }

    class ChatInputHandler implements Runnable{
        private Socket socket;
        private BufferedReader in;

        public ChatInputHandler(Socket socket){
            this.socket = socket;
        }

        @Override
        public void run() {
            try (FileOutputStream fos = new FileOutputStream(generateFile(friend, me), true)){
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String nickname = in.readLine();
                String msg = in.readLine();
                fos.write(("SERVER: " + nickname + " joined to this Chat!").getBytes());
                fos.write(System.lineSeparator().getBytes());
                while(!msg.equals("/quit")){
                    fos.write((nickname + ": " + msg).getBytes());
                    fos.write(System.lineSeparator().getBytes());
                    msg = in.readLine();
                }
                fos.write(("SERVER: " + nickname + " left from this Chat!").getBytes());
                fos.write(System.lineSeparator().getBytes());
                shutdown();
            } catch (IOException e){
                shutdown();
            }
        }
    }

    /**
     * sending notification if friend is online
     * otherwise, save it into Server
     */
    private void sendNotification(){

        if (friendSocket.isConnected()) {
            try (PrintWriter pw2 = new PrintWriter(friendSocket.getOutputStream(), true)) {
                pw2.println("/command");
                pw2.println("/startedChat");
                pw2.println(me);
                pw2.println(NEW_PORT);
            } catch (IOException e) {
                shutdown();
            }
        } else {
            saveNotification(friend, me + " started direct chat with you");
        }
    }

    /**
     * saving notifications in Server
     * when user is not online
     * and send them the notifications when
     * they come back to online
     * @param notificationTo  notification should be sent to
     * @param message         notification message
     */
    private void saveNotification(String notificationTo, String message){
        final String NOTIFICATION_FILE = ".users_lib/notifications.txt";
        try (FileOutputStream fos = new FileOutputStream(NOTIFICATION_FILE, true)){
            fos.write((notificationTo + ",").getBytes());
            fos.write(message.getBytes());
            fos.write(System.lineSeparator().getBytes());
        } catch (IOException e){
            shutdown();
        }
    }

    /**
     * generating unique file name to save chat messages
     * @param name      first user's nickname
     * @param name1     second user's nickname
     * @return          return unique filename
     */
    private String generateFile(String name, String name1){
        // generate unique file name
        int uuidNum = 0;
        for(byte b : name.getBytes()){
            uuidNum += b;
        }
        for(byte b : name1.getBytes()){
            uuidNum += b;
        }
        final String FILE_NAME = (uuidNum) + ".txt";
        final String CHAT_HISTORY_DIR = "./users_lib/";
        return CHAT_HISTORY_DIR+FILE_NAME;
    }

    /**
     * shutdown everything in this class
     */
    private void shutdown(){
        try {
            if(!serverSocket.isClosed()){
                serverSocket.close();
            }
            if(!socket.isClosed()){
                socket.close();
            }
        } catch (IOException e){
            // ignore
        }
    }
}
