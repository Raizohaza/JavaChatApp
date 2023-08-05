package org.ChatApp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileMessage extends Message implements Serializable {
    private String fileName;
    private byte[] fileData;

    public FileMessage(int message_id, String from_number, String to_number, Date sent, int conversation_id,
                       String fileName, byte[] fileData) {
        super(message_id, from_number, to_number, sent, conversation_id);
        this.fileName = fileName;
        this.fileData = fileData;
    }
}
