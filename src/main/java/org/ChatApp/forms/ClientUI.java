package org.ChatApp.forms;

import org.ChatApp.model.Contact;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientUI {
    private JFrame frame;
    private JTextField ipAddressField;
    private JTextField portField;
    private JTextField userNameField;
    private JPasswordField passwordField;
    private JTextField phoneNumberField;
    private JRadioButton loginRadio;
    private JRadioButton registerRadio;
    private JButton startButton;

    public ClientUI() {
        frame = new JFrame("Chat App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 350);
        frame.setLayout(new GridLayout(8, 2, 10, 10));

        // Initialize components
        ipAddressField = new JTextField("127.0.0.1");
        portField = new JTextField("1234");
        userNameField = new JTextField("a");
        passwordField = new JPasswordField("1");
        phoneNumberField = new JTextField("123456789");
        loginRadio = new JRadioButton("Login");
        registerRadio = new JRadioButton("Register");
        startButton = new JButton("Start");

        registerRadio.setSelected(true);

        // Create a button group for radio buttons
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(loginRadio);
        buttonGroup.add(registerRadio);

        // Add components to the frame
        frame.add(new JLabel("IP Address:"));
        frame.add(ipAddressField);
        frame.add(new JLabel("Port:"));
        frame.add(portField);
        frame.add(new JLabel("Username:"));
        frame.add(userNameField);
        frame.add(new JLabel("Password:"));
        frame.add(passwordField);
        frame.add(new JLabel("Phone Number:"));
        frame.add(phoneNumberField);
        frame.add(new JLabel("Select Action:"));
        frame.add(loginRadio);
        frame.add(new JLabel()); // Empty label for spacing
        frame.add(registerRadio);
        frame.add(new JLabel()); // Empty label for spacing
        frame.add(startButton);

        // Add action listener for the Start button
        startButton.addActionListener(e -> {
            // Validate input fields
            if (!validateFields()) {
                JOptionPane.showMessageDialog(frame, "Please fill in all required fields.");
                return;
            }

            String ipAddress = ipAddressField.getText();
            int port = Integer.parseInt(portField.getText());
            String username = userNameField.getText();
            String password = new String(passwordField.getPassword());
            String phoneNumber = phoneNumberField.getText();
            boolean isLogin = loginRadio.isSelected();

            Contact contact = new Contact(0, username, password, null, phoneNumber);
            try {
                sendDataOverSocket(contact);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage());
            }
        });

        // Display the frame
        frame.setVisible(true);
    }

    private void sendDataOverSocket(Contact contact) throws IOException {
        String serverIP = ipAddressField.getText();
        int serverPort = Integer.parseInt(portField.getText());

        Socket socket = new Socket(serverIP, serverPort);
        ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());

        // Write the Contact object to the output stream
        outputStream.writeObject(contact);

        // You can add more logic here based on your application requirements
        // For example, you can wait for a response from the server

    }

    private boolean validateFields() {
        return !ipAddressField.getText().isEmpty() &&
                !portField.getText().isEmpty() &&
                !userNameField.getText().isEmpty() &&
                passwordField.getPassword().length > 0 &&
                !phoneNumberField.getText().isEmpty() &&
                (loginRadio.isSelected() || registerRadio.isSelected());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ClientUI();
            }
        });
    }
}
