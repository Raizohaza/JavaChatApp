package org.chatapp.socket;

import lombok.Getter;
import lombok.Setter;
import org.chatapp.model.*;

import java.io.*;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
public class ChatHandler implements Runnable {
    public static final String STORE_PATH = "Store/";
    private final Socket clientSocket;
    private final Server server;
    private final ObjectInputStream inputStream;
    private final ObjectOutputStream outputStream;
    @Setter
    private ChatEventSocketHandler eventHandler = null;
    @Getter
    private Contact loggedInContact = null;
    private boolean isRunning = true;


    public ChatHandler(Socket clientSocket, Server server) throws IOException {
        this.clientSocket = clientSocket;
        this.server = server;

        outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
        inputStream = new ObjectInputStream(clientSocket.getInputStream());
    }

    @Override
    public void run() {
        try {
            while (isRunning) {
                Request request = (Request) inputStream.readObject();
                processRequest(request);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                close();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void processRequest(Request request) throws Exception {
        RequestType type = request.getType();
        Object data = request.getData();

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
                Integer conservationId = (Integer) data;

                if (conservationId != 0) {
                    getMessageHistory(conservationId);
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
            case RECEIVE_FILE -> {
                String fileName = (String) data;
                handleDownloadFile(fileName);
            }

            case LOGOUT -> handleLogout();
            default -> System.out.println("Unknown request type: " + type);
        }

        outputStream.flush();
    }

    private void handleDownloadFile(String fileName) throws IOException {
        try {
            File file = new File(STORE_PATH + fileName); // Replace with the actual path
            if (file.exists()) {
                try (FileInputStream fileInputStream = new FileInputStream(file)) {
                    byte[] fileData = fileInputStream.readAllBytes();
                    FileMessage fileMessage = new FileMessage("", "", new Date(), 0, fileName, fileData);

                    Response response = new Response(ResponseType.SUCCESS, "File download successful.", fileMessage);
                    outputStream.writeObject(response);
                }
            } else {
                Response response = new Response(ResponseType.FAILURE, "File not found.", null);
                outputStream.writeObject(response);
            }
        } finally {
            Response response = new Response(ResponseType.FAILURE, "File download failed.", null);
            outputStream.writeObject(response);
        }
    }

    private void handleMessage(Message message) throws SQLException, IOException {
        int status = message.save();
        System.out.println(status);
        if (status == 1) {
            server.sendMessageEventToClients(message);
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

    public boolean isMemberOfConversation(int conversationId) {
        System.out.println("isMemberOfConversation: " + loggedInContact.getContact_id() + "\t" + conversationId + "\t" + Conversation.isMemberOfConversation(loggedInContact.getContact_id(), conversationId));
        return Conversation.isMemberOfConversation(loggedInContact.getContact_id(), conversationId);
    }

    private void handleGetContacts() throws SQLException, IOException {
        List<Contact> contacts = Contact.getAll();
        List<ContactOnline> onlineContacts = new ArrayList<>();
        List<String> onlineNumber = new ArrayList<>(server.clients.stream().map(client -> client.getLoggedInContact().getPhone_number()).toList());

        if (!onlineNumber.isEmpty()) {
            for (Contact contact : contacts) {
                onlineContacts.add(new ContactOnline(contact, onlineNumber.contains(contact.getPhone_number())));
            }
        }
        if (!onlineContacts.isEmpty()) {
            outputStream.writeObject(new Response(ResponseType.SUCCESS, "Get contacts successful.", onlineContacts));
            return;
        }
        outputStream.writeObject(new Response(ResponseType.FAILURE, "No contacts", null));
    }


    private void createConversation(Contact fromContact, Contact toContact) throws SQLException, IOException {
        Conversation existingConversation = Conversation.getChatConversation(fromContact, toContact);
        System.out.println(existingConversation);
        if (existingConversation != null) {
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

            Response response = new Response(ResponseType.SUCCESS, "Conversation created and message sent successfully.", newConversation.getConversation_id());
            outputStream.writeObject(response);
        }
    }
    // Handle receiving a file from the client

    private void handleSendFile(FileMessage fileMessage) throws Exception {
        String directoryPath = STORE_PATH;
        String filePath = directoryPath + fileMessage.getFileName();

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
            System.out.println("File received: " + filePath);

            // Save the file message as a regular message
            Message message = new Message(0, fileMessage.getFrom_number(), "File: " + fileMessage.getFileName(), new Date(), fileMessage.getConversation_id());
            int status = message.save();

            if (status == 1) {
                outputStream.writeObject(new Response(ResponseType.SUCCESS, "File received successfully.", null));
            } else {
                outputStream.writeObject(new Response(ResponseType.FAILURE, "Failed to receive file.1", null));
            }

        } finally {
            outputStream.writeObject(new Response(ResponseType.FAILURE, "Failed to receive file.2", null));
        }
    }

    private void handleLogin(Contact contact) throws IOException, SQLException, NoSuchAlgorithmException {
        loggedInContact = contact.login();

        if (loggedInContact != null) {
            server.sendOnlineEventToClients(contact);
            outputStream.writeObject(new Response(ResponseType.SUCCESS, "Login successful.", loggedInContact));
        } else {
            outputStream.writeObject(new Response(ResponseType.FAILURE, "Invalid credentials.", null));
        }
    }

    private void handleRegistration(Contact contact) throws IOException,SQLException ,NoSuchAlgorithmException{
        try {
            Contact existingContact = Contact.getByPhoneNumber(contact.getPhone_number());
            if (existingContact == null) {
                contact.save(); // Save the new contact
                loggedInContact = contact.login();
                server.sendOnlineEventToClients(contact);
                outputStream.writeObject(new Response(ResponseType.SUCCESS, "Registration successful.", contact));
            } else {
                outputStream.writeObject(new Response(ResponseType.FAILURE, "Username or phone number already registered.", null));
            }
        } finally {
            outputStream.writeObject(new Response(ResponseType.FAILURE, "Registration failed.", null));
        }
    }

    private void handleLogout() throws IOException {
        if (eventHandler != null) {
            server.removeEventHandler(eventHandler);
            eventHandler.close();
            eventHandler = null;
        }
        loggedInContact = null;
        server.sendOfflineEventToClients();
        outputStream.writeObject(new Response(ResponseType.SUCCESS, "Logout successful.", null));
        server.removeClient(this);
        server.removeThreadClient(Thread.currentThread());
        isRunning = false;
    }

    public void close() throws IOException {
        if (inputStream != null) inputStream.close();
        if (outputStream != null) outputStream.close();
        if (clientSocket != null) clientSocket.close();
    }
}
