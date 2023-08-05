package org.chatapp.socket;

import lombok.Getter;
import lombok.Setter;
import org.chatapp.model.ChatEvent;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ChatEventSocketHandler implements Runnable, ChatEventListener {
    private final ObjectOutputStream eventOutputStream;
    final ChatEventManager eventManager;
    @Setter
    @Getter
    ChatHandler chatHandler;

    public ChatEventSocketHandler(Socket eventSocket, ChatEventManager refEventManager) throws IOException {
        this.eventManager = refEventManager;
        System.out.println(eventSocket);
        eventOutputStream = new ObjectOutputStream(eventSocket.getOutputStream());
        eventManager.subscribe("online", this);
        eventManager.subscribe("message", this);
        eventManager.subscribe("offline", this);
    }

    public void sendEvent(ChatEvent event) {
        try {
            eventOutputStream.writeObject(event);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void run() {
    }

    @Override
    public void onChatEvent(String eventType, Object data) {
        sendEvent(new ChatEvent(eventType, data));
    }

    public void close() {
        eventManager.unsubscribe("online", this);
        eventManager.unsubscribe("offline", this);
        eventManager.unsubscribe("message", this);
    }
}