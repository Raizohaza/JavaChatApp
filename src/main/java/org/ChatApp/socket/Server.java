package org.ChatApp.socket;

import org.ChatApp.forms.ServerUI;
import org.ChatApp.model.Contact;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.DriverManager;
import java.sql.SQLException;
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


    public void broadcastMessage(String message) {
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

    public void removeClient(ChatHandler client) {
        clients.remove(client);
    }

    public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException {
        new Server(5000, null);
    }
}

