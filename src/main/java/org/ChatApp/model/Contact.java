package org.ChatApp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Contact {
    private int contact_id;
    private String first_name;
    private String last_name;
    private String profile_photo;
    private String phone_number;

    public static Contact getById(Connection connection, int contactId) throws SQLException {
        String sql = "SELECT * FROM contact WHERE contact_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, contactId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return extractContactFromResultSet(resultSet);
                }
            }
        }
        return null;
    }

    public static List<Contact> getAll(Connection connection) throws SQLException {
        List<Contact> contacts = new ArrayList<>();
        String sql = "SELECT * FROM contact";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                contacts.add(extractContactFromResultSet(resultSet));
            }
        }
        return contacts;
    }

    private static Contact extractContactFromResultSet(ResultSet resultSet) throws SQLException {
        int contactId = resultSet.getInt("contact_id");
        String firstName = resultSet.getString("first_name");
        String lastName = resultSet.getString("last_name");
        String profilePhoto = resultSet.getString("profile_photo");
        String phoneNumber = resultSet.getString("phone_number");
        return new Contact(contactId, firstName, lastName, profilePhoto, phoneNumber);
    }

    public void save(Connection connection) throws SQLException {
        String sql = "INSERT INTO contact (first_name, last_name, profile_photo, phone_number) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, first_name);
            statement.setString(2, last_name);
            statement.setString(3, profile_photo);
            statement.setString(4, phone_number);
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    this.contact_id = generatedKeys.getInt(1);
                }
            }
        }
    }

    public void update(Connection connection) throws SQLException {
        String sql = "UPDATE contact SET first_name=?, last_name=?, profile_photo=?, phone_number=? WHERE contact_id=?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, first_name);
            statement.setString(2, last_name);
            statement.setString(3, profile_photo);
            statement.setString(4, phone_number);
            statement.setInt(5, contact_id);
            statement.executeUpdate();
        }
    }

    public void delete(Connection connection) throws SQLException {
        String sql = "DELETE FROM contact WHERE contact_id=?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, contact_id);
            statement.executeUpdate();
        }
    }
}
