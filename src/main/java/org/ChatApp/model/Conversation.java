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
    public static Connection connection;

    public static Conversation getById(int conversationId) throws SQLException {
        String sql = "SELECT * FROM conversation WHERE conversation_id = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1, conversationId);
        ResultSet resultSet = statement.executeQuery();
        return resultSet.next() ? extractConversationFromResultSet(resultSet) : null;
    }

    public static List<Conversation> getAll() throws SQLException {
        List<Conversation> conversations = new ArrayList<>();
        String sql = "SELECT * FROM conversation";
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
        while (resultSet.next()) {
            conversations.add(extractConversationFromResultSet(resultSet));
        }
        return conversations;
    }

    private static Conversation extractConversationFromResultSet(ResultSet resultSet) throws SQLException {
        int conversationId = resultSet.getInt("conversation_id");
        String conversationName = resultSet.getString("conversation_name");
        return new Conversation(conversationId, conversationName);
    }

    public void save() throws SQLException {
        String sql = "INSERT INTO conversation (conversation_name) VALUES (?)";
        PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        statement.setString(1, conversation_name);
        statement.executeUpdate();
        try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                conversation_id = generatedKeys.getInt(1);
            }
        }
    }

    public void update() throws SQLException {
        String sql = "UPDATE conversation SET conversation_name=? WHERE conversation_id=?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, conversation_name);
        statement.setInt(2, conversation_id);
        statement.executeUpdate();
    }

    public void delete() throws SQLException {
        String sql = "DELETE FROM conversation WHERE conversation_id=?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1, conversation_id);
        statement.executeUpdate();
    }

    public static Conversation getChatConversation(Contact contact1, Contact contact2) throws SQLException {
        // Check if a chat conversation exists between the two contacts
        String sql = "SELECT conversation_id FROM group_member WHERE contact_id = ? AND conversation_id IN " +
                "(SELECT conversation_id FROM group_member WHERE contact_id = ?) " +
                "AND left IS NULL";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, contact1.getContact_id());
            statement.setInt(2, contact2.getContact_id());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int conversationId = resultSet.getInt("conversation_id");
                    return Conversation.getById(conversationId);
                }
            }
        }

        return null;
    }
}
