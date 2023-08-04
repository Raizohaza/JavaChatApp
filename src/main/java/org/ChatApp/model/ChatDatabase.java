package org.ChatApp.model;

import java.sql.*;

public class ChatDatabase {

    private Connection connection = null;

    public ChatDatabase(String databasePath) {
        try {
            // Load the SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");

            String url = "jdbc:sqlite:users.db";
            connection = DriverManager.getConnection(url);
            // Create the table if it doesn't exist
            createTableIfNotExists();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTableIfNotExists() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            String msg = """                                              
                    CREATE TABLE message (
                        message_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        from_number VARCHAR(20) NOT NULL,
                        message_text VARCHAR(255) NOT NULL,
                        sent DATETIME NOT NULL,
                        conversation_id INTEGER NOT NULL,
                        FOREIGN KEY (conversation_id) REFERENCES conversation (conversation_id)
                    );
                    """;

            String contact = """
                    CREATE TABLE contact (
                        contact_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_name VARCHAR (20) NOT NULL,
                        password VARCHAR (20) NOT NULL,
                        profile_photo VARCHAR (255) NULL,
                        phone_number VARCHAR(20) NOT NULL UNIQUE
                    );
                    """;
            String conversation = """
                    CREATE TABLE conversation (
                        conversation_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        conversation_name VARCHAR (255) NOT NULL
                    );
                    """;
            String group_member = """
                    CREATE TABLE group_member (
                        contact_id INTEGER NOT NULL,
                        conversation_id INTEGER NOT NULL,
                        joined DATETIME NOT NULL,
                        left DATETIME NULL,
                        PRIMARY KEY (contact_id, conversation_id),
                        FOREIGN KEY (contact_id) REFERENCES contact(contact_id),
                        FOREIGN KEY(conversation_id) REFERENCES conversation (conversation_id)
                    );
                      """;
            statement.executeUpdate(msg);
            statement.executeUpdate(contact);
            statement.executeUpdate(conversation);
            statement.executeUpdate(group_member);
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

    public void initData() {
        String data = """
                -- Insert sample data for the contact table.
                INSERT INTO contact (user_name, password, phone_number) VALUES
                  ('John', 'Doe', '+1234567890'),
                  ('Alice', 'Smith', '+9876543210');
                                
                -- Insert sample data for the conversation table.
                INSERT INTO conversation (conversation_name) VALUES
                  ('Family Group'),
                  ('Work Group');
                                
                -- Insert sample data for the message table.
                INSERT INTO message (from_number, message_text, sent, conversation_id) VALUES
                  ('+1234567890', 'Hello, this is John!', '2023-07-27 12:30:00', 1),
                  ('+9876543210', 'Hi John, nice to meet you!', '2023-07-27 12:35:00', 1),
                  ('+1234567890', 'Hello from the work group!', '2023-07-27 13:00:00', 2),
                  ('+9876543210', 'Hi everyone!', '2023-07-27 13:05:00', 2);
                                
                -- Insert sample data for the group_member table.
                INSERT INTO group_member (contact_id, conversation_id, joined) VALUES
                  (1, 1, '2023-07-27 12:30:00'),
                  (2, 1, '2023-07-27 12:35:00'),
                  (1, 2, '2023-07-27 13:00:00'),
                  (2, 2, '2023-07-27 13:05:00');
                                
                """;
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(data);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

//    public void exampleUsage(){
//        String url = "jdbc:sqlite:users.db"; // Replace with your database path
//        try (Connection connection = DriverManager.getConnection(url)) {
//            // Create instances of the classes
//            Message message1 = new Message(0, "123456789", "Hello!", new Date(), 1);
//            Message message2 = new Message(0, "987654321", "Hi there!", new Date(), 1);
//            Contact contact1 = new Contact(0, "John", "Doe", null, "123456789");
//            Contact contact2 = new Contact(0, "Jane", "Smith", null, "987654321");
//            Conversation conversation = new Conversation(0, "Sample Conversation");
//
//            // Save the instances to the database
//            message1.save(connection);
//            message2.save(connection);
//            contact1.save(connection);
//            contact2.save(connection);
//            conversation.save(connection);
//
//            // Update the instances and save changes to the database
//            message1.setMessage_text("Updated message");
//            message1.update(connection);
//
//            contact2.setPassword("Johnson");
//            contact2.update(connection);
//
//            // Retrieve instances by ID
//            Message retrievedMessage = Message.getById(connection, message1.getMessage_id());
//            Contact retrievedContact = Contact.getById(connection, contact2.getContact_id());
//            Conversation retrievedConversation = Conversation.getById(connection, conversation.getConversation_id());
//
//            System.out.println("Retrieved Message: " + retrievedMessage);
//            System.out.println("Retrieved Contact: " + retrievedContact);
//            System.out.println("Retrieved Conversation: " + retrievedConversation);
//
//            // Retrieve all instances from the database
//            System.out.println("All Messages:");
//            for (Message message : Message.getAll(connection)) {
//                System.out.println(message);
//            }
//
//            System.out.println("All Contacts:");
//            for (Contact contact : Contact.getAll(connection)) {
//                System.out.println(contact);
//            }
//
//            System.out.println("All Conversations:");
//            for (Conversation conv : Conversation.getAll(connection)) {
//                System.out.println(conv);
//            }
//
//            // Delete instances from the database
//            message2.delete(connection);
//            contact1.delete(connection);
//
//            // Retrieve all instances from the database after deletions
//            System.out.println("All Messages after deletions:");
//            for (Message message : Message.getAll(connection)) {
//                System.out.println(message);
//            }
//
//            System.out.println("All Contacts after deletions:");
//            for (Contact contact : Contact.getAll(connection)) {
//                System.out.println(contact);
//            }
//
//            System.out.println("All Conversations after deletions:");
//            for (Conversation conv : Conversation.getAll(connection)) {
//                System.out.println(conv);
//            }
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } catch (NoSuchAlgorithmException e) {
//            throw new RuntimeException(e);
//        }
//    }

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
        new ChatDatabase(databasePath);
//        chatDatabase.initData();
//        chatDatabase.exampleUsage();

    }
}
