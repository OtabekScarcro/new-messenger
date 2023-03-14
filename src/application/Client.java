package application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client implements Runnable{
    public static String userNickname;
    static Map<String, Integer> notificationMessages = new HashMap<>();
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
            e.printStackTrace();
            shutdown();
        }
    }
    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            scanner = new Scanner(System.in);

            InputHandler inputHandler = new InputHandler(socket);
            Thread thread = new Thread(inputHandler);
            thread.start();

            // create an account (or)
            // log in to your account
            accountStep();

            // start input messages from user
            // main window inside the application
            mainWindow();
        } catch (IOException e){
            e.printStackTrace();
            shutdown();
        }

    }

    /**
     * main window in this application
     */
    private void mainWindow(){
        while(socket.isConnected()){
            clear();
            notifications();
            System.out.println("1. Show main menu");
            System.out.println("2. Write to friend");
            System.out.println("3. Write to group");
            System.out.println("4. Show notifications");

            String msgMainWindow = scanner.nextLine();
            if(msgMainWindow == null){
                continue;
            }
            else if(msgMainWindow.equals("1")){
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
                showNotifications();
            }
        }
    }

    /**
     * printing number of new notifications
     */
    private void notifications(){
        int length = notificationMessages.keySet().size();
        if(length != 0){
            System.out.println("You have " + length + " new notifications");
        }
    }

    /**
     * showing all new notifications to user
     */
    private void showNotifications(){
        ArrayList<String> list = new ArrayList<>(notificationMessages.keySet());
        int length = list.size();
        if(length == 0){
            System.out.println("You don't have any notifications yet!");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e){
                e.printStackTrace();
            }
        }
        else {
            for(int i=1;i<=length;i++){
                System.out.println(i + ": " + list.get(i-1));
            }
            System.out.println("Please choose the number (0 to go back): ");
            System.out.println("\"/clear\" to remove all the notifications");
            String msgInNotification = scanner.nextLine();
            if(!msgInNotification.equals("/clear")) {
                int option = Integer.parseInt(msgInNotification);
                while (option < 0 || option > length) {
                    System.out.println("Please select the right number!");
                    msgInNotification = scanner.nextLine();
                    if(!msgInNotification.equals("/clear")){
                        option = scanner.nextInt();
                    }
                    else {
                        option = 0;
                        break;
                    }

                }
                if (option != 0) {
                    String[] str = list.get(option - 1).split(" ");
                    String name = str[0];

                    // getting port number from notifications
                    int portNumber = notificationMessages.get(list.get(option - 1));

                    DirectChat directChat = new DirectChat(portNumber);
                    directChat.run();

                    // removing this notification
                    notificationMessages.remove(list.get(option - 1));
                }
            }

            // removing all notifications
            if(msgInNotification.equals("/clear")){
                Set<String> set = notificationMessages.keySet();
                set.forEach(e -> notificationMessages.remove(e));
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
                if(msgInMenu != null) {
                    if (msgInMenu.equals("1")) {
                        clear();
                        searchFriend();
                    } else if (msgInMenu.equals("2")) {

                    } else if (msgInMenu.equals("3")) {

                    } else if (msgInMenu.equals("4")) {

                    } else if (msgInMenu.equals("5")) {

                    } else if (msgInMenu.equals("6")) {

                    } else if (!msgInMenu.equals("0")) {
                        System.out.println("Please select 0 to 6 from the above menu");
                    }
                }
            } catch (Exception e){
                e.printStackTrace();
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

            // start chat
            // getting port number from server
            try {
                Thread.sleep(1000);
            }catch (InterruptedException e){
                // ignore
            }
            DirectChat directChat = new DirectChat(InputHandler.NEW_PORT);
            directChat.run();
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
    public static void clear(){
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
//        try {
//            if(!socket.isClosed()){
//                socket.close();
//            }
//            if(!poll.isShutdown()){
//                poll.shutdown();
//            }
//            in.close();
//            out.close();
//            scanner.close();
//        } catch (IOException e){
//            // ignore
//        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }
}
