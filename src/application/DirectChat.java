package application;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class DirectChat implements Runnable{

    private final int PORT_NUM;
    private Socket socket;
    private Scanner scanner;

    public DirectChat(int portNum){
        this.PORT_NUM = portNum;
    }

    @Override
    public void run() {
        try {
            socket = new Socket("localhost", PORT_NUM);
            scanner = new Scanner(System.in);
            // start sending message
            messageSending();
        } catch (IOException e){
            shutdown();
        }
    }

    private void messageSending(){
        try (PrintWriter pw = new PrintWriter(this.socket.getOutputStream(), true)){
            //Client.clear();
            System.out.println("You can send message now!");
            pw.println(Client.userNickname);
            String msg = scanner.nextLine();
            while(!msg.equals("/quit")){
                pw.println(msg);
                msg = scanner.nextLine();
            }
            pw.println(msg);
            System.out.println("You left from this chat");
            Thread.sleep(2000);
        } catch (InterruptedException | IOException e){
            shutdown();
        }
    }

    /**
     * shutdown everything in this class
     */
    private void shutdown(){
        try {
            if (!socket.isClosed()) {
                socket.close();
            }
            scanner.close();
        } catch (IOException e){
            // ignore
        }
    }
}
