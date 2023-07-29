package org.ChatApp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.swing.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Contact {
    private int contact_id;
    private String user_name;
    private String password;
    private String profile_photo;
    private String phone_number;

    public void save(Connection connection) throws SQLException, NoSuchAlgorithmException {
        // Hash the password before saving
        String hashedPassword = hashPassword(password);

        String sql = "INSERT INTO contact (user_name, password, profile_photo, phone_number) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, user_name);
            statement.setString(2, hashedPassword);
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

    public ImageIcon getImage() {
        return new ImageIcon(profile_photo);
    }

    public void update(Connection connection) throws SQLException, NoSuchAlgorithmException {
        // Hash the password before updating
        String hashedPassword = hashPassword(password);

        String sql = "UPDATE contact SET user_name=?, password=?, profile_photo=?, phone_number=? WHERE contact_id=?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user_name);
            statement.setString(2, hashedPassword);
            statement.setString(3, profile_photo);
            statement.setString(4, phone_number);
            statement.setInt(5, contact_id);
            statement.executeUpdate();
        }
    }

    // New login method
    public static Contact login(Connection connection, String username, String password) throws SQLException, NoSuchAlgorithmException {
        String hashedPassword = hashPassword(password);

        String sql = "SELECT * FROM contact WHERE user_name = ? AND password = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, hashedPassword);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return extractContactFromResultSet(resultSet);
                }
            }
        }
        return null;
    }

    public void delete(Connection connection) throws SQLException {
        String sql = "DELETE FROM contact WHERE contact_id=?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, contact_id);
            statement.executeUpdate();
        }
    }

    private static String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hashedBytes = md.digest(password.getBytes());

        // Convert the byte array to a hexadecimal string
        StringBuilder sb = new StringBuilder();
        for (byte b : hashedBytes) {
            sb.append(Integer.toHexString((b & 0xFF) | 0x100), 1, 3);
        }
        return sb.toString();
    }

    private static Contact extractContactFromResultSet(ResultSet resultSet) throws SQLException {
        int contactId = resultSet.getInt("contact_id");
        String userName = resultSet.getString("user_name");
        String password = resultSet.getString("password");
        String profilePhoto = resultSet.getString("profile_photo");
        String phoneNumber = resultSet.getString("phone_number");
        return new Contact(contactId, userName, password, profilePhoto, phoneNumber);
    }
}

