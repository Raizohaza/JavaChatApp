package org.ChatApp.socket;

import org.ChatApp.model.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class ChatHandler implements Runnable {
    private final Socket clientSocket;
    private final Server server;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private Contact clientInfo;

    public ChatHandler(Socket clientSocket, Server server) {
        this.clientSocket = clientSocket;
        this.server = server;

        try {
            outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            inputStream = new ObjectInputStream(clientSocket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                Request request = (Request) inputStream.readObject();
                processRequest(request);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println(e.getMessage());
        } finally {
            close();
        }
    }

    private void processRequest(Request request) {
        RequestType type = request.getType();
        Object data = request.getData();

        try {
            switch (type) {
                case LOGIN -> handleLogin((Contact) data);
                case REGISTER -> handleRegistration((Contact) data);
                case GET_CONTACTS -> handleGetContacts();
                case SEND_MESSAGE -> handleSendMessage((Message) data);

                case LOGOUT -> handleLogout();
                default -> System.out.println("Unknown request type: " + type);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleGetContacts() throws SQLException, IOException {
        List<Contact> contacts = Contact.getAll();
        if (!contacts.isEmpty()) {
            outputStream.writeObject(new Response(ResponseType.SUCCESS, "Get contacts successful.", contacts));
        } else {
            outputStream.writeObject(new Response(ResponseType.FAILURE, "No contacts", null));
        }
    }


    private void handleSendMessage(Message data) throws IOException {
        try {
            // Extract the necessary information from the messageRequest
            String fromNumber = data.getFrom_number();
            String messageText = data.getMessage_text();
            int conversationId = data.getConversation_id();

            Message newMessage = new Message(0, fromNumber, messageText, new Date(), conversationId);
            newMessage.save();

            // Send apprpriate response indicating success
            Response response = new Response(ResponseType.SUCCESS, "Message sent successfully.", null);
            outputStream.writeObject(response);
        } catch (SQLException e) {
            e.printStackTrace();
            Response response = new Response(ResponseType.FAILURE, "Error sending message.", null);
            outputStream.writeObject(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleLogin(Contact contact) throws IOException, SQLException, NoSuchAlgorithmException {
        Contact loggedInContact = contact.login();
        if (loggedInContact != null) {
            outputStream.writeObject(new Response(ResponseType.SUCCESS, "Login successful.", loggedInContact));
        } else {
            outputStream.writeObject(new Response(ResponseType.FAILURE, "Invalid credentials.", null));
        }
    }

    private void handleRegistration(Contact contact) throws IOException {
        try {
            Contact existingContact = Contact.getByPhoneNumber(contact.getPhone_number());
            if (existingContact == null) {
                contact.save(); // Save the new contact
                outputStream.writeObject(new Response(ResponseType.SUCCESS, "Registration successful.", contact));
            } else {
                outputStream.writeObject(new Response(ResponseType.FAILURE, "Username or phone number already registered.", null));
            }
        } catch (Exception e) {
            e.printStackTrace();
            outputStream.writeObject(new Response(ResponseType.FAILURE, "Registration failed.", null));
        }
    }

    private void handleLogout() {
        try {
            outputStream.writeObject(new Response(ResponseType.SUCCESS, "Logout successful.", null));
        } catch (IOException e) {
            e.printStackTrace();
        }
        close();
    }

    public void sendMessage(String msg) {
        System.out.println(clientInfo.getUser_name() + ": " + msg);
    }

    public void close() {
        try {
            if (inputStream != null) inputStream.close();
            if (outputStream != null) outputStream.close();
            if (clientSocket != null) clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
