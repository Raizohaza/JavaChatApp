package org.chatapp.socket;

import lombok.Getter;
import org.chatapp.model.*;

import java.io.*;
import java.net.Socket;

public class Client {
    Socket socket = null;
    Socket eventSocket;
    ObjectOutputStream outputStream = null;
    ObjectInputStream inputStream = null;
    ObjectInputStream eventInputStream;
    @Getter
    final ChatEventManager eventManager = new ChatEventManager();

    public Client(String address, int port, int eventPort) {
        try {
            socket = new Socket(address, port);
            System.out.println("Connected");

            outputStream = new ObjectOutputStream(socket.getOutputStream());

            inputStream = new ObjectInputStream(socket.getInputStream());

            startListeningForEvents(address, eventPort);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void startListeningForEvents(String address, int eventPort) {
        new Thread(() -> {
            try {
                eventSocket = new Socket(address, eventPort);
                System.out.println(eventSocket);
                eventInputStream = new ObjectInputStream(eventSocket.getInputStream());
                while (true) {
                    ChatEvent receivedEvent = (ChatEvent) eventInputStream.readObject();
                    System.out.println(receivedEvent);
                    eventManager.notify(receivedEvent.getEventType(), receivedEvent.getData());
                }
            } catch (IOException | ClassNotFoundException e) {
                System.out.println(e.getMessage());
            }
        }).start();
    }

    public Response sendRequest(RequestType type, Object data) {
        try {
            outputStream.writeObject(new Request(type, data));
            return receiveResponse();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return new Response(ResponseType.FAILURE, "Request failed.", null);
    }

    public Response receiveResponse() {
        try {
            return (Response) inputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }
        return new Response(ResponseType.FAILURE, "Response receive failed.", null);
    }


    public void close() {
        try {
            outputStream.writeObject(new Request(RequestType.LOGOUT, null));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (outputStream != null) outputStream.close();
                if (inputStream != null) inputStream.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public void downloadFile(String fileName) {
        Response response = sendRequest(RequestType.RECEIVE_FILE, fileName);

        if (response.getType() == ResponseType.SUCCESS) {
            FileMessage fileMessage = (FileMessage) response.getData();
            String filePath = "ClientStore/" + fileMessage.getFileName();

            File directory = new File("ClientStore");
            if (!directory.exists()) {
                if (directory.mkdirs()) {
                    System.out.println("Directory created: " + directory.getAbsolutePath());
                } else {
                    System.err.println("Failed to create directory: " + directory.getAbsolutePath());
                    return;
                }
            }

            try (FileOutputStream fileOutputStream = new FileOutputStream(filePath)) {
                fileOutputStream.write(fileMessage.getFileData());
                System.out.println("File received: " + filePath);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        } else {
            System.out.println("Failed to download file: " + fileName);
        }

    }


}
