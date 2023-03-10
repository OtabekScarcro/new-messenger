package server;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.net.Socket;
import java.util.Properties;
import java.util.Random;

public class AccountHandler implements Runnable{

    private MessageHandler msgHandler;
    private BufferedReader in;
    private PrintWriter out;
    private Socket socket;
    private String connectionId;
    private String nickname;
    private String userEmail;

    // to stop infinite loop in Client
    private final String STOP = "/stop";

    // Users library path
    private final String USERS_LIBRARY_FILE = "./users_lib/users.csv";

    /**
     * constructor for Account handler
     * @param socket
     * @param connectionId
     */
    public AccountHandler(Socket socket, String connectionId){
        this.socket = socket;
        this.connectionId = connectionId;
        msgHandler = new MessageHandler();
    }
    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // ask from user whether user has an account or not
            out.println("1. Sign in");
            out.println("2. Sign up");
            String choose = msgHandler.rmSpaces(in.readLine());
            while(!choose.equals("1") && !choose.equals("2")){
                out.println("Please select 1 or 2");
                choose = msgHandler.rmSpaces(in.readLine());
            }

            // ask for user's email address
            out.println("Please enter your email: ");
            userEmail = in.readLine();

            // check if email is valid
            isValidEmail(userEmail);

            // sign in
            if(choose.equals("1")){
                signIn(userEmail);
                out.println("A new account has been created successfully!");
                out.println(STOP);
            }
            // sign up
            else {
                signUp(userEmail);
                out.println("Logged in successful!");
                out.println(STOP);
            }

            // add this user to the library
            addToLibrary();

            // add this socket to the usersList
            Server.usersList.put(this.nickname, this.socket);

        } catch (IOException e){
            shutdown();
        }
    }

    /**
     * sign in to account
     * @param email
     */
    private void signIn(String email){
        try {
            // check if this email is available
            while(!msgHandler.searchForEmail(email)){
                out.println("This email is not available in Server");
                out.println("Please try with another email");
                email = in.readLine();
                isValidEmail(email);
            }
        } catch (IOException e){
            shutdown();
        }
    }

    /**
     * sign up to account
     * @param email
     */
    private void signUp(String email){
        try {
            // check if this email is already existed
            while(msgHandler.searchForEmail(email)){
                out.println("This email is already existed in Server");
                out.println("Please try with another email");
                email = in.readLine();
                isValidEmail(email);
            }

            // confirmation email by sending 6-digit code
            confirmationEmail(email);

            // ask for nickname from user
            out.println("Please enter a nickname for you: ");
            nickname = msgHandler.rmSpaces(in.readLine());
            while(msgHandler.searchForNickname(nickname)){
                out.println("This nickname is already existed!");
                out.println("Please choose another one: ");
                nickname = msgHandler.rmSpaces(in.readLine());
            }
        } catch (IOException e){
            shutdown();
        }
    }

    /**
     * check if this email is valid
     * @param email
     */
    private void isValidEmail(String email){
        while(!msgHandler.checkEmail(email)){
            out.println("Please enter a valid email address: ");
            try {
                email = in.readLine();
            } catch (IOException e){
                shutdown();
            }
        }
    }

    /**
     * to confirm that email
     * @param email
     */
    private void confirmationEmail(String email){
        // generating random confirmation code
        String randomCode = createConfirmationCode();

        Properties properties = new Properties();
        properties.put("mail.smtp.auth", true);
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", 587);
        properties.put("mail.smtp.starttls.enable", true);
        properties.put("mail.transport.protocol", "smtp");

        // code will be sent from this email
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("otaboyev149@gmail.com", "zoebjqhtjtstfjcb");
            }
        });

        try {

            // preparing message to send
            MimeMessage code = new MimeMessage(session);
            code.setSubject("Confirmation from Messenger application");
            code.setText("Confirmation code: " + randomCode);

            // adding email address
            Address addressTo = new InternetAddress(email);
            code.setRecipient(Message.RecipientType.TO, addressTo);

            // sending....
            Transport.send(code);

            // ask for confirmation code from user
            out.println("Please enter a confirmation code: ");
            String enteredCode = msgHandler.rmSpaces(in.readLine());

            // check if code is valid
            while(!enteredCode.equals(randomCode)){
                out.println("Confirmation code is not valid!");
                out.println("Please try again: ");
                enteredCode = in.readLine();
            }
            out.println("Email confirmation is successful");
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * generating new confirmation code
     * @return
     */
    private String createConfirmationCode(){
        Random rand = new Random();
        final int CODE_LENGTH = 6;
        String code = "";
        for(int i=0;i<CODE_LENGTH;i++){
            code += ((Integer)rand.nextInt(10)).toString();
        }
        return code;
    }

    /**
     * adding information to the library
     * like: [connectionID, nickname, email, socket]
     */
    private void addToLibrary(){
        try(PrintWriter pw = new PrintWriter(USERS_LIBRARY_FILE)){
            pw.write(connectionId + ",");
            pw.write(nickname + ",");
            pw.write(userEmail);
        } catch (FileNotFoundException e){
            e.printStackTrace();
        }
    }

    /**
     * to stop everything
     */
    private void shutdown(){
        try {
            in.close();
            out.close();
            if(!socket.isClosed()){
                socket.close();
            }
        } catch (IOException e){
            // ignore
        }
    }

}
