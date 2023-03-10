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

    private void mainMenu(){
        String msgInMenu = "";
        do {
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
     * search for new friends
     */
    private void searchFriend(){
        System.out.println("Enter a nickname you want to search: ");
        out.println("/command");
        out.println("/searchFriend");

        while(!InputHandler.getStopCommand()){
            String newFriend = scanner.nextLine();
            out.println(newFriend);
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

    private void shutdown(){

    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }
}
