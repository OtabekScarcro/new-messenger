package application;

import java.io.*;
import java.net.Socket;

public class InputHandler implements Runnable{

    private Socket socket;
    private BufferedReader in;
    private final String FRIENDS_LIST = "./lib/friends.csv";
    private final int NICKNAMES_ROW = 0;

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

            while(socket.isConnected()){
                String msgFromServer = in.readLine();

                // if it is "/command" call commands()
                if(msgFromServer.equals("/command")){
                    commands();
                }
                // otherwise print to console
                else {
                    System.out.println(msgFromServer);
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
                if(stopLooping){
                    stopLooping = false;
                }
                else {
                    stopLooping = true;
                }
            }

            else if(command.equals("/addFriend")){
                try (PrintWriter pw = new PrintWriter(FRIENDS_LIST)){
                    String nickname = in.readLine();
                    //pw.write();
                } catch (IOException e){
                    // TODO: handle
                }
            }
        } catch (IOException e){
            shutdown();
        }
    }


    public static boolean getStopCommand(){
        return stopLooping;
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
