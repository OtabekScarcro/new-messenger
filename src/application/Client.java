package application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client implements Runnable{
    private Socket socket;
    private ExecutorService poll;
    private BufferedReader in;
    private PrintWriter out;
    private Scanner scanner;
    private final int SERVER_PORT = 2421;

    private Client(){
        try {
            socket = new Socket("127.0.0.1", SERVER_PORT);
            poll = Executors.newCachedThreadPool();
        } catch (IOException e){
            shutdown();
        }
    }
    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            scanner = new Scanner(System.in);

            Thread thread = new Thread(new InputHandler(socket));
            thread.start();

            // create an account (or)
            // log in to your account
            accountStep();

            // start input messages from user
            // main window inside the application
            mainWindow();
        } catch (IOException e){
            shutdown();
        }

    }

    /**
     * main window in this application
     */
    private void mainWindow(){
        while(socket.isConnected()){
            clear();
            //notifications();
            System.out.println("1. Show main menu");
            System.out.println("2. Write to friend");
            System.out.println("3. Write to group");
            System.out.println("4. Show notifications");

            String msgMainWindow = scanner.nextLine();
            if(msgMainWindow.equals("1")){
                clear();
                mainMenu();
            }
            else if(msgMainWindow.equals("2")){
                clear();
                //writeToFriend();
            }
            else if(msgMainWindow.equals("3")){
                clear();
                //writeToGroup();
            }
            else if(msgMainWindow.equals("4")){
                clear();
                //showNotifications();
            }
        }
    }

    /**
     * main menu in this application
     */
    private void mainMenu(){
        String msgInMenu;
        do {
            msgInMenu = null;
            clear();
            System.out.println("1. Search for new friends");
            System.out.println("2. Search for new groups");
            System.out.println("3. Show my friends list");
            System.out.println("4. Show my groups list");
            System.out.println("5. Create a new group");
            System.out.println("6. Settings");
            System.out.println("0. Back to the main window");
            try {
                msgInMenu = scanner.nextLine();
                if(msgInMenu.equals("1")){
                    clear();
                    searchFriend();
                }
                else if(msgInMenu.equals("2")){

                }
                else if(msgInMenu.equals("3")){

                }
                else if(msgInMenu.equals("4")){

                }
                else if(msgInMenu.equals("5")){

                }
                else if(msgInMenu.equals("6")){

                }
                else if(!msgInMenu.equals("0")){
                    System.out.println("Please select 0 to 6 from the above menu");
                }
            } catch (Exception e){
                shutdown();
            }
        } while (!msgInMenu.equals("0"));
    }

    /**
     * search for new friends
     */
    private void searchFriend(){
        out.println("/command");
        out.println("/searchFriend");
        String message = "";
        InputHandler.setStopLooping(true);
        while(InputHandler.getStopCommand()){
            message = scanner.nextLine();
            out.println(message);
        }
        message = message.replaceAll("\\s","").toLowerCase();
        if(message.equals("yes") || message.equals("y")){
            System.out.println(message);
            startChat();
        }
    }

    /**
     * start direct chat with one friend
     */
    private void startChat(){
        try {
            String msg;
            System.out.println("Chat has been started!");
            while (!(msg = scanner.nextLine()).equals("/quit")) {
                out.println(msg);
            }
            out.println(msg);
            System.out.println("You left from this chat!");
            Thread.sleep(2000);
        } catch (InterruptedException e1){
            // TODO: handle
        }
    }

    /**
     * class to print messages in the chat
     */
    class OutputInChat implements Runnable{
        private Socket socket;
        private BufferedReader in;
        private String friend;
        public OutputInChat(Socket socket, String friend){
            this.friend = friend;
            this.socket = socket;
        }

        @Override
        public void run() {
            clear();
            String msg;
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String nickname = this.in.readLine();
                System.out.println("You can start to write to " + nickname);
                System.out.println("\"/history\" - to see full history in this chat");
                System.out.println("\"/quit\" - to exit from this chat!");
                while ((msg = in.readLine()) != "/quit") {
                    System.out.println(friend + ": " + msg);
                }
                System.out.println(nickname + " has left from this chat!");
            } catch (IOException e){
                shutdown();
            }
        }
    }

    /**
     * method sends information to server
     * in Accounts step
     */
    private void accountStep(){
        while(InputHandler.getStopCommand()){
            String msg = scanner.nextLine();
            out.println(msg);
        }
    }

    /**
     * clearing the client screen
     */
    private void clear(){
        try {
            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            System.out.print("\033[H\033[2J");
            System.out.flush();
        } catch (IOException e){
            // ignore
        } catch (InterruptedException e1){
            // ignore
        }
    }

    /**
     * shutdown everything
     */
    private void shutdown(){
        try {
            if(!socket.isClosed()){
                socket.close();
            }
            if(!poll.isShutdown()){
                poll.shutdown();
            }
            in.close();
            out.close();
            scanner.close();
        } catch (IOException e){
            // ignore
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }
}
