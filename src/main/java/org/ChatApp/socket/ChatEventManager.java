package org.chatapp.socket;

import org.chatapp.model.Message;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ChatEventManager {
    private final Map<String, List<ChatEventListener>> eventListeners;

    public ChatEventManager() {
        eventListeners = new HashMap<>();
    }

    public void subscribe(String eventType, ChatEventListener listener) {
        eventListeners.computeIfAbsent(eventType, k -> new LinkedList<>()).add(listener);
    }

    public void unsubscribe(String eventType, ChatEventListener listener) {
        List<ChatEventListener> listeners = eventListeners.get(eventType);
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    public void notify(String eventType, Object data) {
        List<ChatEventListener> listeners = eventListeners.get(eventType);
        if (listeners != null) {
            for (ChatEventListener listener : listeners) {
                listener.onChatEvent(eventType, data);
            }
        }
    }

    public void notifyMessage(String eventType, Object data) {
        List<ChatEventListener> listeners = eventListeners.get(eventType);
        if (listeners != null) {
            for (ChatEventListener listener : listeners) {
                if (listener instanceof ChatEventSocketHandler chatEventSocketHandler && data instanceof Message message) {
                    if (chatEventSocketHandler.getChatHandler().isMemberOfConversation(message.getConversation_id())) {
                        listener.onChatEvent(eventType, data);
                    }
                }
            }
        }
    }
}
