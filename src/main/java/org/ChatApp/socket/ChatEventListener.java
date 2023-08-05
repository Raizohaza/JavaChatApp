package org.chatapp.socket;


public interface ChatEventListener {
    void onChatEvent(String eventType, Object data);
}