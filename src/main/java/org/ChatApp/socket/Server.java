package org.ChatApp.socket;

import org.ChatApp.forms.ServerUI;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    Socket socket = null;
    ServerSocket server;
    List<ChatHandler> clients = new ArrayList<>();
    private volatile boolean isRunning;

    public Server(int port, ServerUI serverUI) throws IOException {
        server = new ServerSocket(port);
        if (serverUI != null) serverUI.server = this;

        System.out.println("Server started and waiting for clients...");
        isRunning = true;
        System.out.println(server);
        while (isRunning) {
            System.out.println("isRunning: " + isRunning);
            socket = server.accept();
            System.out.println("New client connected: " + socket);
            ChatHandler client = new ChatHandler(socket, this);
            clients.add(client);
            Thread clientThread = new Thread(client);
            clientThread.start();
        }
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
            server.close();
        } catch (IOException e) {
            System.out.println("Error stopping server: " + e.getMessage());
        }
    }

    public void removeClient(ChatHandler client) {
        clients.remove(client);
    }

    public static void main(String[] args) throws IOException {
        new Server(5000, null);
    }
}

