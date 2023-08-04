package org.ChatApp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message implements Serializable {
    private int message_id;
    private String from_number;
    private String message_text;
    private Date sent;
    private int conversation_id;
    public static Connection connection;
    // Other methods and constructors

    public void save() throws SQLException {
        String sql = "INSERT INTO message (from_number, message_text, sent, conversation_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, from_number);
            statement.setString(2, message_text);
            statement.setDate(3, new java.sql.Date(sent.getTime()));
            statement.setInt(4, conversation_id);
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    this.message_id = generatedKeys.getInt(1);
                }
            }
        }
    }

    public static Message getById(Connection connection, int messageId) throws SQLException {
        String sql = "SELECT * FROM message WHERE message_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, messageId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return extractMessageFromResultSet(resultSet);
                }
            }
        }
        return null;
    }

    public static List<Message> getAll(Connection connection) throws SQLException {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM message";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                messages.add(extractMessageFromResultSet(resultSet));
            }
        }
        return messages;
    }

    public void update(Connection connection) throws SQLException {
        String sql = "UPDATE message SET from_number=?, message_text=?, sent=?, conversation_id=? WHERE message_id=?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, from_number);
            statement.setString(2, message_text);
            statement.setDate(3, new java.sql.Date(sent.getTime()));
            statement.setInt(4, conversation_id);
            statement.setInt(5, message_id);
            statement.executeUpdate();
        }
    }

    public void delete(Connection connection) throws SQLException {
        String sql = "DELETE FROM message WHERE message_id=?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, message_id);
            statement.executeUpdate();
        }
    }

    private static Message extractMessageFromResultSet(ResultSet resultSet) throws SQLException {
        int messageId = resultSet.getInt("message_id");
        String fromNumber = resultSet.getString("from_number");
        String messageText = resultSet.getString("message_text");
        Date sent = resultSet.getDate("sent");
        int conversationId = resultSet.getInt("conversation_id");
        return new Message(messageId, fromNumber, messageText, sent, conversationId);
    }
}
