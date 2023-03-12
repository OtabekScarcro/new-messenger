package server;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable{

    public static Map<String, Socket> usersList = new HashMap<>();
    private ServerSocket serverSocket;
    private Socket socket;
    private ExecutorService poll;
    private String connectionId;
    private final int UNIQUE_ID_LENGTH = 8;
    private final String USERS_LIBRARY_FILE = "./users_lib/users.csv";

    /**
     * constructor for Server class
     */
    private Server(){
        try {
            serverSocket = new ServerSocket(Integer.parseInt(System.getenv("SERVER_PORT")));
            poll = Executors.newCachedThreadPool();
        } catch (IOException e){
            shutdown();
        }

    }

    /**
     * overwriting run() method
     */
    @Override
    public void run() {
        while(true) {
            try {
                socket = serverSocket.accept();
                connectionId = getUniqueId();

                ConnectionHandler connectionHandler = new ConnectionHandler(socket, connectionId);
                poll.execute(connectionHandler);

            } catch (IOException e) {
                shutdown();
            }
        }
    }

    /**
     * stop everything
     */
    private void shutdown(){
        try {
            if (!serverSocket.isClosed()) {
                serverSocket.close();
            }
            if (!socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e){
            // ignore
        }
    }

    /**
     * generating unique ID for every connection
     * @return
     */
    private String getUniqueId(){
        Random rand = new Random();
        String uuid;
        boolean noneUnique;

        do {
            // generate unique ID for a connection
            uuid = "";
            for (int i = 0; i < UNIQUE_ID_LENGTH; i++) {
                uuid += ((Integer) rand.nextInt(10)).toString();
            }

            // check this id is unique or not
            noneUnique = false;
            try (BufferedReader br = new BufferedReader(new FileReader(USERS_LIBRARY_FILE))) {
                String id = "";
                while ((id = br.readLine()) != null) {
                    String str[] = id.split(",");
                    if (str[0].equals(uuid)) {
                        noneUnique = true;
                        break;
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } while(noneUnique);
        return uuid;
    }

    /**
     * main method
     * @param args
     */
    public static void main(String[] args) {
        Server server = new Server();
        System.out.println("Server is running....");
        server.run();
    }
}
