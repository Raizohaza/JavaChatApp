import javax.swing.*;
import java.io.*;
import java.net.*;

public class Client extends JFrame {
    Socket socket = null;
    DataInputStream input = null;
    DataOutputStream output = null;

    public Client(String address, int port){
        try {
            socket = new Socket(address,port);

            System.out.println("Connected");

            input = new DataInputStream(System.in);

            output = new DataOutputStream(socket.getOutputStream());
        }

        catch (IOException e){
            e.printStackTrace();
        }

        String line = "";
        try {
            System.out.println("Please enter your username: ");
            line = input.readLine();
            output.writeUTF(line);
        }catch (IOException e){
            e.printStackTrace();
        }

        System.out.println("You can chat freely now! \nEnter q to exit.");
        while (!line.equals("q")){
            try {
                line = input.readLine();
                output.writeUTF(line);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            input.close();
            output.close();
            socket.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Client client = new Client("127.0.0.1",5000);
    }
}
