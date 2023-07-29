package org.ChatApp.socket;

import org.ChatApp.socket.Server;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class ChatHandler implements Runnable {
    Socket clientSocket;
    Server server;
    DataInputStream in = null;
    String clientName = "";

    public ChatHandler(Socket clientSocket, Server server) {
        this.clientSocket = clientSocket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            in = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
            this.clientName = getName(in);
            String line = "";
            while (!line.equals("q")) {
                try {
                    line = in.readUTF();
                    sendMessage(line);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                server.broadcastMessage(clientName + " has left the chat.");
                System.out.println("Closing connection");
                server.removeClient(this);
                clientSocket.close();
                in.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private String getName(DataInputStream in) throws IOException {
        String line = "";
        try {
            line = in.readUTF();
            System.out.println(line);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return line;
    }
    public void sendMessage(String msg){
        System.out.println(clientName + ":" + msg);
    }
}
