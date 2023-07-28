package org.ChatApp;

import java.sql.*;

public class ChatDatabase {

    private Connection connection = null;

    public ChatDatabase(String databasePath) {
        try {
            // Load the SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");

//            String url = "jdbc:sqlite:C:\\Users\\raizo\\IdeaProjects\\JavaChatApp\\users.db";
            // Connect to the database
            String url = "jdbc:sqlite:C:\\Users\\raizo\\IdeaProjects\\JavaChatApp\\users.db";
//            connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
            connection = DriverManager.getConnection(url);
            System.out.println(connection);
            // Create the table if it doesn't exist
            createTableIfNotExists();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTableIfNotExists() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS chat_messages (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "sender TEXT NOT NULL," +
                    "message TEXT NOT NULL," +
                    "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ")";
            statement.executeUpdate(sql);
        }
    }

    public void insertMessage(String sender, String message) throws SQLException {
        String sql = "INSERT INTO chat_messages (sender, message) VALUES (?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, sender);
            preparedStatement.setString(2, message);
            preparedStatement.executeUpdate();
        }
    }

    public void printAllMessages() throws SQLException {
        String sql = "SELECT * FROM chat_messages";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String sender = resultSet.getString("sender");
                String message = resultSet.getString("message");
                String timestamp = resultSet.getString("timestamp");
                System.out.println(id + " | " + sender + " | " + message + " | " + timestamp);
            }
        }
    }

    public static void main(String[] args) {
        String databasePath = "user.db";
        ChatDatabase chatDatabase = new ChatDatabase(databasePath);

        try {
            // Insert example messages
            chatDatabase.insertMessage("John", "Hello!");
            chatDatabase.insertMessage("Alice", "Hi, John!");

            // Print all messages
            chatDatabase.printAllMessages();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
