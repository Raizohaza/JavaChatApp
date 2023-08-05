package org.ChatApp.socket;

import org.ChatApp.forms.ServerUI;
import org.ChatApp.model.Contact;
import org.ChatApp.model.Conversation;
import org.ChatApp.model.GroupMember;
import org.ChatApp.model.Message;

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
    List<ChatHandler> clients = new ArrayList<>();
    List<Thread> tClients = new ArrayList<>();
    private volatile boolean isRunning;

    public Server(int port, ServerUI serverUI) throws IOException, SQLException, ClassNotFoundException {
        server = new ServerSocket(port);
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
        do {
            System.out.println("isRunning: " + isRunning);
            socket = server.accept();
            System.out.println("New client connected: " + socket);
            ChatHandler client = new ChatHandler(socket, this);
            clients.add(client);
            tClients.add(new Thread(client));
            tClients.get(tClients.size() - 1).start();
            tClients.removeIf(tClient -> !tClient.isAlive());
        } while ((isRunning) && !tClients.isEmpty());

        System.out.println("isRunning: " + isRunning);
    }


    public void broadcastMessage(Message message) {
        for (ChatHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public void stop() {
        isRunning = false;
        try {
            for (ChatHandler client : clients) {
                client.close();
            }
            System.out.println("Stopping server");
            server.close();
        } catch (IOException e) {
            System.out.println("Error stopping server: " + e.getMessage());
        }
    }

    private void createTableIfNotExists() {
        try (Statement statement = Contact.connection.createStatement()) {
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
        } catch (SQLException e) {
            e.printStackTrace(); // Handle this exception appropriately in your application
        }
    }


    public void removeClient(ChatHandler client) {
        clients.remove(client);
    }

    public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException {
        new Server(5000, null);
    }
}

