package org.ChatApp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    Socket socket = null;
    ServerSocket server = null;
    List<ChatHandler> clients = new ArrayList<>();

    public Server(int port) {
        try {
            server = new ServerSocket(port);
            System.out.println("Server started and waiting for clients...");

            for (; ; ) {
                socket = server.accept();
                System.out.println("New client connected: " + socket);
                ChatHandler client = new ChatHandler(socket, this);
                clients.add(client);
                Thread clientThread = new Thread(client);
                clientThread.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Server(5000);
    }

    public void broadcastMessage(String message) {
        for (ChatHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public void removeClient(ChatHandler client) {
        clients.remove(client);
    }
}

