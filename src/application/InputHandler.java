package application;

import server.Server;

import java.io.*;
import java.net.Socket;

public class InputHandler implements Runnable{

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private final String FRIENDS_LIST = "./src/application/friends.txt";
    private final int NICKNAMES_ROW = 0;
    static int NEW_PORT;

    // we use that variable when
    // we came to accounts step
    // to stop infinity loop
    private static volatile boolean stopLooping = true;

    public InputHandler(Socket socket){
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            while(socket.isConnected()){
                String msgFromServer = in.readLine();

                // if it is "/command" call commands()
                if(msgFromServer != null) {
                    if (msgFromServer.equals("/command")) {
                        commands();
                    }
                    // otherwise print to console
                    else {
                        System.out.println(msgFromServer);
                    }
                }
            }
        } catch (IOException e){
            shutdown();
        }
    }

    private void commands(){
        try {
            String command = in.readLine();
            if(command.equals("/stopLooping")){
                stopLooping = false;
            }
            else if(command.equals("/addFriend")){
                try (FileOutputStream fos = new FileOutputStream(FRIENDS_LIST, true)){
                    String nickname = in.readLine();
                    fos.write(nickname.getBytes());
                    fos.write(System.lineSeparator().getBytes());
                    System.out.println(nickname + " has been added successfully to your friends list!");
                } catch (IOException e){
                    // TODO: handle
                }
            }
            else if(command.equals("/startedChat")){
                try {
                    String whoStarted = in.readLine();
                    String portNumber = in.readLine();
                    Client.notificationMessages.put(whoStarted, Integer.parseInt(portNumber));
                }catch (IOException e){
                    // TODO: handle
                }
            }
            else if(command.equals("/setNickname")){
                Client.userNickname = in.readLine();
            }
            else if(command.equals("/setNewPort")){
                String str = in.readLine();
                NEW_PORT = Integer.parseInt(str);
            }
        } catch (IOException e){
            shutdown();
        }
    }

    /**
     * getting stop command to break infinite loop in client app
     * @return
     */
    public static boolean getStopCommand(){
        return stopLooping;
    }

    /**
     * setting value to stopLooping variable
     * @param bool
     */
    public static void setStopLooping(boolean bool){
        stopLooping = bool;
    }

    /**
     * stop everything
     */
    private void shutdown(){
        try {
            in.close();
            if(!socket.isClosed()){
                socket.close();
            }
        } catch (IOException e){
            // ignore
        }
    }
}
