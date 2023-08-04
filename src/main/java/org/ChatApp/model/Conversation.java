package org.ChatApp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Conversation implements Serializable {
    private int conversation_id;
    private String conversation_name;

    // Other methods and constructors

    public static Conversation getById(Connection connection, int conversationId) throws SQLException {
        String sql = "SELECT * FROM conversation WHERE conversation_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, conversationId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return extractConversationFromResultSet(resultSet);
                }
            }
        }
        return null;
    }

    public static List<Conversation> getAll(Connection connection) throws SQLException {
        List<Conversation> conversations = new ArrayList<>();
        String sql = "SELECT * FROM conversation";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                conversations.add(extractConversationFromResultSet(resultSet));
            }
        }
        return conversations;
    }

    private static Conversation extractConversationFromResultSet(ResultSet resultSet) throws SQLException {
        int conversationId = resultSet.getInt("conversation_id");
        String conversationName = resultSet.getString("conversation_name");
        return new Conversation(conversationId, conversationName);
    }

    public void save(Connection connection) throws SQLException {
        String sql = "INSERT INTO conversation (conversation_name) VALUES (?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, conversation_name);
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    this.conversation_id = generatedKeys.getInt(1);
                }
            }
        }
    }

    public void update(Connection connection) throws SQLException {
        String sql = "UPDATE conversation SET conversation_name=? WHERE conversation_id=?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, conversation_name);
            statement.setInt(2, conversation_id);
            statement.executeUpdate();
        }
    }

    public void delete(Connection connection) throws SQLException {
        String sql = "DELETE FROM conversation WHERE conversation_id=?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, conversation_id);
            statement.executeUpdate();
        }
    }
}
