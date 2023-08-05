package org.ChatApp.socket;

import org.ChatApp.model.*;

import java.io.*;
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

    private void processRequest(Request request) throws IOException {
        RequestType type = request.getType();
        Object data = request.getData();

        try {
            switch (type) {
                case LOGIN -> handleLogin((Contact) data);
                case REGISTER -> handleRegistration((Contact) data);
                case GET_CONTACTS -> handleGetContacts();
                case CREATE_CONVERSATION -> {
                    CreateConversationData conversationData = (CreateConversationData) data;
                    Contact fromContact = Contact.getByPhoneNumber(conversationData.getFrom_number());
                    Contact toContact = Contact.getByPhoneNumber(conversationData.getTo_number());

                    if (fromContact != null && toContact != null) {
                        createConversation(fromContact, toContact);
                    } else {
                        outputStream.writeObject(new Response(ResponseType.FAILURE, "Invalid contacts", null));
                    }
                }

                case GET_MESSAGE_HISTORY -> {
                    System.out.println(data);
                    Integer consersationId = (Integer) data;

                    if (consersationId != 0) {
                        getMessageHistory(consersationId);
                    } else {
                        outputStream.writeObject(new Response(ResponseType.FAILURE, "Invalid contacts", null));
                    }
                }

                case SEND_MESSAGE -> {
                    Message message = (Message) data;
                    Contact contact = Contact.getByPhoneNumber(message.getFrom_number());
                    if (contact != null) {
                        handleMessage(message);
                        return;
                    }
                    outputStream.writeObject(new Response(ResponseType.FAILURE, "No contacts", null));
                }
                case SEND_FILE -> handleSendFile((FileMessage) data);
                case LOGOUT -> handleLogout();
                default -> System.out.println("Unknown request type: " + type);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            outputStream.flush();
        }
    }

    private void handleMessage(Message message) throws SQLException, IOException {
        int status = message.save();
        System.out.println(status);
        if (status == 1) {
//            server.broadcastMessage(message);
            outputStream.writeObject(new Response(ResponseType.SUCCESS, "Message history retrieved successfully.", message));
        } else {
            outputStream.writeObject(new Response(ResponseType.FAILURE, "No messages found.", null));
        }
    }

    private void getMessageHistory(Integer conversationId) throws IOException, SQLException {
        List<Message> messages = Message.getByConversationId(conversationId);
        System.out.println(messages.size());
        if (!messages.isEmpty()) {
            outputStream.writeObject(new Response(ResponseType.SUCCESS, "Message history retrieved successfully.", messages));
        } else {
            outputStream.writeObject(new Response(ResponseType.FAILURE, "No messages found.", null));
        }
    }

    private void handleGetContacts() throws SQLException, IOException {
        List<Contact> contacts = Contact.getAll();
        if (!contacts.isEmpty()) {
            outputStream.writeObject(new Response(ResponseType.SUCCESS, "Get contacts successful.", contacts));
            return;
        }
        outputStream.writeObject(new Response(ResponseType.FAILURE, "No contacts", null));
    }

    private void createConversation(Contact fromContact, Contact toContact) throws SQLException, IOException {
        Conversation existingConversation = Conversation.getChatConversation(fromContact, toContact);
        System.out.println(existingConversation);
        if (existingConversation != null) {
            // Only create a message
//            Message message = new Message();
//            message.setFrom_number(fromContact.getPhone_number());
//            message.setMessage_text("This is a test message.");
//            message.setSent(new Date());
//
//            message.setConversation_id(existingConversation.getConversation_id());
//            message.save();
            Response response = new Response(ResponseType.SUCCESS, "Message sent successfully.", existingConversation.getConversation_id());
            outputStream.writeObject(response);
        } else {
            // Create a new conversation, group members, and message
            Conversation newConversation = new Conversation();
            newConversation.setConversation_name(String.valueOf(System.currentTimeMillis()));
            newConversation.save();

            GroupMember groupMember1 = new GroupMember(fromContact.getContact_id(), newConversation.getConversation_id(), new Date(), null);
            groupMember1.save();

            GroupMember groupMember2 = new GroupMember(toContact.getContact_id(), newConversation.getConversation_id(), new Date(), null);
            groupMember2.save();

//            Message message = new Message();
//            message.setFrom_number(fromContact.getPhone_number());
//            message.setMessage_text("This is a test message.");
//            message.setSent(new Date());
//
//            message.setConversation_id(newConversation.getConversation_id());
//            message.save();

            Response response = new Response(ResponseType.SUCCESS, "Conversation created and message sent successfully.", newConversation.getConversation_id());
            outputStream.writeObject(response);
        }
    }
    // Handle receiving a file from the client

    private void handleSendFile(FileMessage fileMessage) {
        String directoryPath = "Store/";
        String filePath = directoryPath + fileMessage.getFileName();

        try {
            File directory = new File(directoryPath);
            if (!directory.exists()) {
                if (directory.mkdirs()) {
                    System.out.println("Directory created: " + directoryPath);
                } else {
                    System.err.println("Failed to create directory: " + directoryPath);
                    outputStream.writeObject(new Response(ResponseType.FAILURE, "Failed to receive file.", null));
                    return;
                }
            }

            try (FileOutputStream fileOutputStream = new FileOutputStream(filePath)) {
                fileOutputStream.write(fileMessage.getFileData());

                // You can implement further logic here, such as saving the file information in a database

                System.out.println("File received: " + filePath);

                outputStream.writeObject(new Response(ResponseType.SUCCESS, "File received successfully.", null));
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    outputStream.writeObject(new Response(ResponseType.FAILURE, "Failed to receive file.", null));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendGroupMessage(Message message, Contact contact) {
        // Implement sending a group message
        // This logic depends on how you handle group conversations
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

    public void sendMessage(Message message) {
        try {
            outputStream.writeObject(new Response(ResponseType.SUCCESS, "New message received.", message));
        } catch (IOException e) {
            e.printStackTrace();
        }
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
