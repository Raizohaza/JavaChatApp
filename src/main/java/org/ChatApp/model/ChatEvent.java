package org.chatapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class ChatEvent implements Serializable {
    private String eventType;
    private Object data;
}

