package org.ChatApp.forms;

import org.ChatApp.model.Contact;

import javax.swing.*;
import java.awt.*;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class RegisterFrame extends JFrame {
    private final JTextField usernameField;
    private final JTextField phoneField;
    private final JPasswordField passwordField;


    public RegisterFrame() {
        setTitle("Register Contact");
        setSize(300, 150);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new GridLayout(4, 2));

        phoneField = new JTextField();
        usernameField = new JTextField();
        passwordField = new JPasswordField();
        JButton registerButton = new JButton("Register");


        add(new JLabel("Phone number: "));
        add(phoneField);
        add(new JLabel("Username: "));
        add(usernameField);
        add(new JLabel("Password: "));
        add(passwordField);
        add(new JLabel());
        add(registerButton);

        // Add action listener
        registerButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            String phone = phoneField.getText();
            Contact contact = new Contact(0, username, password, null, phone);

            if (register(contact)) {
                // Successful registration logic here
                JOptionPane.showMessageDialog(RegisterFrame.this, "Registration successful!");
                dispose(); // Close the registration frame after successful registration
            } else {
                // Failed registration logic here
                JOptionPane.showMessageDialog(RegisterFrame.this, "Registration failed!");
            }
        });

        setVisible(true);
    }

    private boolean register(Contact contact) {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:users.db")) {
            contact.save(connection);
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        new RegisterFrame();
    }
}

