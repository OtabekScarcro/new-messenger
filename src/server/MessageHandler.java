package server;

import java.io.*;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageHandler {

    // Users library path
    private final String USERS_LIBRARY_FILE = "./users_lib/users.csv";
    private final int EMAILS_ROW = 2;
    private final int NICKNAME_ROW = 1;
    private final int SOCKET_ROW = 3;

    /**
     * method will remove all white spaces
     * @param str
     * @return
     */
    public String rmSpaces(String str){
        return str.replaceAll("\\s", "");
    }

    /**
     * to remove all white spaces from command,
     * and to make them into the same case
     * @param str
     * @return
     */
    public String sanitize(String str){
        return str.replaceAll("\\s", "").toLowerCase();
    }

    /**
     * this method check whether the email is valid or not
     * @param email
     * @return
     */
    public boolean checkEmail(String email){
        String emailRegex = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$";
        Pattern emailPattern = Pattern.compile(emailRegex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = emailPattern.matcher(email);
        return matcher.find();
    }

    /**
     * check whether this email is available or not
     * @param email
     * @return
     */
    public boolean searchForEmail(String email){
        try (BufferedReader br = new BufferedReader(new FileReader(USERS_LIBRARY_FILE))){
            String line;
            while((line = br.readLine()) != null){
                String[] str = line.split(",");
                if(str[EMAILS_ROW].equals(email)){
                    return true;
                }
            }
        } catch (FileNotFoundException e){
            // TODO: handle
        } catch (IOException e1){
            // TODO: handle
        }
        return false;
    }

    /**
     * checking whether nickname is unique
     * @param nickname
     * @return
     */
    public boolean searchForNickname(String nickname){
        try (BufferedReader br = new BufferedReader(new FileReader(USERS_LIBRARY_FILE))){
            String line;
            while((line = br.readLine()) != null){
                String[] str = line.split(",");
                if(str.length != 3){
                    continue;
                }
                if(str[NICKNAME_ROW].equals(nickname)){
                    return true;
                }
            }
        }catch (FileNotFoundException e){
            // TODO: handle
        } catch (IOException e1){
            // TODO: handle
        }
        return false;
    }

}
