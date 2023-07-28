package org.ChatApp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupMember {
    private int contact_id;
    private int conversation_id;
    private Date joined;
    private Date left;

    // Other methods and constructors

    public void save(Connection connection) throws SQLException {
        String sql = "INSERT INTO group_member (contact_id, conversation_id, joined, left) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, contact_id);
            statement.setInt(2, conversation_id);
            statement.setDate(3, new java.sql.Date(joined.getTime()));
            if (left != null) {
                statement.setDate(4, new java.sql.Date(left.getTime()));
            } else {
                statement.setNull(4, Types.DATE);
            }
            statement.executeUpdate();
        }
    }

    public static List<GroupMember> getByConversationId(Connection connection, int conversationId) throws SQLException {
        List<GroupMember> groupMembers = new ArrayList<>();
        String sql = "SELECT * FROM group_member WHERE conversation_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, conversationId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    groupMembers.add(extractGroupMemberFromResultSet(resultSet));
                }
            }
        }
        return groupMembers;
    }

    public static List<GroupMember> getByContactId(Connection connection, int contactId) throws SQLException {
        List<GroupMember> groupMembers = new ArrayList<>();
        String sql = "SELECT * FROM group_member WHERE contact_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, contactId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    groupMembers.add(extractGroupMemberFromResultSet(resultSet));
                }
            }
        }
        return groupMembers;
    }

    public void update(Connection connection) throws SQLException {
        String sql = "UPDATE group_member SET joined=?, left=? WHERE contact_id=? AND conversation_id=?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, new java.sql.Date(joined.getTime()));
            if (left != null) {
                statement.setDate(2, new java.sql.Date(left.getTime()));
            } else {
                statement.setNull(2, Types.DATE);
            }
            statement.setInt(3, contact_id);
            statement.setInt(4, conversation_id);
            statement.executeUpdate();
        }
    }

    public void delete(Connection connection) throws SQLException {
        String sql = "DELETE FROM group_member WHERE contact_id=? AND conversation_id=?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, contact_id);
            statement.setInt(2, conversation_id);
            statement.executeUpdate();
        }
    }

    private static GroupMember extractGroupMemberFromResultSet(ResultSet resultSet) throws SQLException {
        int contactId = resultSet.getInt("contact_id");
        int conversationId = resultSet.getInt("conversation_id");
        Date joined = resultSet.getDate("joined");
        Date left = resultSet.getDate("left");
        return new GroupMember(contactId, conversationId, joined, left);
    }
}
