package org.chatapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileMessage extends Message implements Serializable {
    private String fileName;
    private byte[] fileData;

    public FileMessage(String from_number, String message_text, Date sent, int conversation_id,
                       String fileName, byte[] fileData) {
        super(0, from_number, message_text, sent, conversation_id); // message_id will be generated
        this.fileName = fileName;
        this.fileData = fileData;
    }
}
