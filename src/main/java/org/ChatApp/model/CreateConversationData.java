package org.ChatApp.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class CreateConversationData implements Serializable {
    String from_number;
    String to_number;
}
