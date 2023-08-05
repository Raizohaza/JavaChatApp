package org.ChatApp.socket;

import org.ChatApp.model.*;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client extends JFrame {
    Socket socket = null;
    ObjectOutputStream outputStream = null;
    ObjectInputStream inputStream = null;

    public Client(String address, int port) {
        try {
            socket = new Socket(address, port);

            System.out.println("Connected");

            outputStream = new ObjectOutputStream(socket.getOutputStream());

            inputStream = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public Response sendRequest(RequestType type, Object data) {
        try {
            outputStream.writeObject(new Request(type, data));
            return receiveResponse();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Response(ResponseType.FAILURE, "Request failed.", null);
    }
    public Response receiveResponse() {
        try {
            return (Response) inputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return new Response(ResponseType.FAILURE, "Response receive failed.", null);
    }

    public void close() {
        try {
            outputStream.writeObject(new Request(RequestType.LOGOUT, null));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (outputStream != null) outputStream.close();
                if (inputStream != null) inputStream.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static void main(String[] args) {
        Client client = new Client("127.0.0.1", 1234);

        try {
            // Register a new contact
            Contact newContact = new Contact();
            newContact.setUser_name("username");
            newContact.setPassword("hashed_password");
            newContact.setPhone_number("1234567890");
            System.out.println(client.sendRequest(RequestType.REGISTER, newContact).getMessage());

            // Login
            Contact loggedInContact = (Contact) client.sendRequest(RequestType.LOGIN, newContact).getData();
            if (loggedInContact != null) {
                System.out.println("Logged in: " + loggedInContact.getUser_name());
            } else {
                System.out.println("Login failed");
            }

//            // Get contacts
//            List<Contact> contacts = (List<Contact>) client.sendRequest(RequestType.GET_CONTACTS, null).getData();
//            if (contacts != null) {
//                contacts.forEach(contact -> System.out.println("Contact: " + contact.getUser_name()));
//            } else {
//                System.out.println("Failed to get contacts");
//            }

            // ... (similarly handle other request types)

        } finally {
            client.close();
        }
    }
}
