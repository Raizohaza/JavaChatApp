package org.chatapp.socket;

import org.chatapp.forms.ServerUI;
import org.chatapp.model.Contact;
import org.chatapp.model.Conversation;
import org.chatapp.model.GroupMember;
import org.chatapp.model.Message;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Server {
    Socket socket;
    ServerSocket server;
    final List<ChatHandler> clients = new ArrayList<>();
    final List<Thread> tClients = new ArrayList<>();

    private final ServerSocket eventServerSocket;
    private final List<ChatEventSocketHandler> eventHandlers = new ArrayList<>();
    private final ChatEventManager eventManager = new ChatEventManager();
    private volatile boolean isRunning;

    public Server(int port, int eventPort, ServerUI serverUI) throws IOException, SQLException, ClassNotFoundException {
        server = new ServerSocket(port);
        eventServerSocket = new ServerSocket(eventPort);
        if (serverUI != null) serverUI.server = this;
        Class.forName("org.sqlite.JDBC");
        String url = "jdbc:sqlite:users.db";
        Contact.connection = DriverManager.getConnection(url);
        Conversation.connection = Contact.connection;
        GroupMember.connection = Contact.connection;
        Message.connection = Contact.connection;

        createTableIfNotExists();
        System.out.println("Server started and waiting for clients...");
        isRunning = true;
        System.out.println(server);
        startEventHandlers();
        startRequestHandlers();
    }

    private void startRequestHandlers() throws IOException {
        do {
            System.out.println("isRunning: " + isRunning);
            socket = server.accept();
            System.out.println("New client connected: " + socket);
            ChatHandler client = new ChatHandler(socket, this);
            clients.add(client);
            tClients.add(new Thread(client));
            tClients.get(tClients.size() - 1).start();
            tClients.removeIf(tClient -> !tClient.isAlive());
        } while (isRunning && !tClients.isEmpty());
    }

    private void startEventHandlers() {
        new Thread(() -> {
            while (isRunning) {
                try {
                    Socket eventSocket = eventServerSocket.accept();
                    System.out.println("eventSocket" + eventSocket);
                    ChatEventSocketHandler eventHandler = new ChatEventSocketHandler(eventSocket, eventManager);
                    eventHandlers.add(eventHandler);

                    ChatHandler associatedChatHandler = getAssociatedChatHandler(eventSocket);
                    if (associatedChatHandler != null) {
                        associatedChatHandler.setEventHandler(eventHandler);
                        eventHandler.setChatHandler(associatedChatHandler);
                    }

                    new Thread(eventHandler).start();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        }).start();
    }

    private ChatHandler getAssociatedChatHandler(Socket eventSocket) {
        int expectedChatHandlerPort = eventSocket.getPort() - 1;
        for (ChatHandler chatHandler : clients) {
            int socketPort = chatHandler.getClientSocket().getPort();
            if (socketPort == expectedChatHandlerPort) {
                return chatHandler;
            }
        }
        return null;
    }

    public void sendMessageEventToClients(Message message) {
        eventManager.notifyMessage("message", message);
    }

    public void sendOnlineEventToClients(Contact contact) {
        sendChatEventToClients("online", contact);
    }

    public void sendOfflineEventToClients() {
        sendChatEventToClients("offline", null);
    }

    public void sendChatEventToClients(String eventType, Object data) {
        eventManager.notify(eventType, data);
    }

    public void removeClient(ChatHandler client) {
        clients.remove(client);
    }

    public void removeThreadClient(Thread thread) {
        tClients.remove(thread);
    }

    public void removeEventHandler(ChatEventSocketHandler eventHandler) {
        eventHandlers.remove(eventHandler);
    }

    public void stop() {
        isRunning = false;
        try {
            for (ChatHandler client : clients) {
                client.close();
            }
            for (ChatEventSocketHandler eventHandler : eventHandlers) {
                eventHandler.close();
            }
            System.out.println("Stopping server");
            server.close();
        } catch (IOException e) {
            System.out.println("Error stopping server: " + e.getMessage());
        }
    }

    private void createTableIfNotExists() throws SQLException {
        Statement statement = Contact.connection.createStatement();
        String[] tableQueries = {
                "CREATE TABLE IF NOT EXISTS message (" +
                        "message_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "from_number VARCHAR(20) NOT NULL," +
                        "message_text VARCHAR(255) NOT NULL," +
                        "sent DATETIME NOT NULL," +
                        "conversation_id INTEGER NOT NULL," +
                        "FOREIGN KEY (conversation_id) REFERENCES conversation (conversation_id)" +
                        ");",
                "CREATE TABLE IF NOT EXISTS contact (" +
                        "contact_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "user_name VARCHAR (20) NOT NULL," +
                        "password VARCHAR (20) NOT NULL," +
                        "profile_photo VARCHAR (255) NULL," +
                        "phone_number VARCHAR(20) NOT NULL UNIQUE" +
                        ");",
                "CREATE TABLE IF NOT EXISTS conversation (" +
                        "conversation_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "conversation_name VARCHAR (255) NOT NULL" +
                        ");",
                "CREATE TABLE IF NOT EXISTS group_member (" +
                        "contact_id INTEGER NOT NULL," +
                        "conversation_id INTEGER NOT NULL," +
                        "joined DATETIME NOT NULL," +
                        "left DATETIME NULL," +
                        "PRIMARY KEY (contact_id, conversation_id)," +
                        "FOREIGN KEY (contact_id) REFERENCES contact(contact_id)," +
                        "FOREIGN KEY(conversation_id) REFERENCES conversation (conversation_id)" +
                        ");"
        };
        for (String query : tableQueries) {
            statement.executeUpdate(query);
        }
    }

    public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException {
        new Server(5000, 5001, null);
    }
}

