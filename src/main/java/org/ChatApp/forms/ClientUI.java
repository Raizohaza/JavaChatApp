package org.ChatApp.forms;

import org.ChatApp.model.Contact;
import org.ChatApp.model.RequestType;
import org.ChatApp.model.Response;
import org.ChatApp.model.ResponseType;
import org.ChatApp.socket.Client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ClientUI {
    private final JFrame frame;
    private final JTextField ipAddressField;
    private final JTextField portField;
    private final JTextField userNameField;
    private final JPasswordField passwordField;
    private final JTextField phoneNumberField;
    private final JRadioButton loginRadio;
    private final JRadioButton registerRadio;
    Client client = null;

    public ClientUI() {
        frame = new JFrame("Chat App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 350);

        frame.setLayout(new BorderLayout());

        // Add a margin around the entire layout
        JPanel mainPanel = new JPanel();
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10)); // Adjust the margin values as needed
        mainPanel.setLayout(new GridLayout(8, 2, 10, 10));

        // Initialize components
        ipAddressField = new JTextField("127.0.0.1");
        portField = new JTextField("1234");
        userNameField = new JTextField("Nguyen Van A");
        passwordField = new JPasswordField("1");
        phoneNumberField = new JTextField("0907000000");
        loginRadio = new JRadioButton("Login");
        registerRadio = new JRadioButton("Register");
        JButton startButton = new JButton("Start");

        loginRadio.setSelected(true);

        // Create a button group for radio buttons
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(loginRadio);
        buttonGroup.add(registerRadio);

        // Add components to the frame
        mainPanel.add(new JLabel("IP Address:"));
        mainPanel.add(ipAddressField);
        mainPanel.add(new JLabel("Port:"));
        mainPanel.add(portField);
        mainPanel.add(new JLabel("Username:"));
        mainPanel.add(userNameField);
        mainPanel.add(new JLabel("Password:"));
        mainPanel.add(passwordField);
        mainPanel.add(new JLabel("Phone Number:"));
        mainPanel.add(phoneNumberField);
        mainPanel.add(new JLabel("Select Action:"));
        mainPanel.add(loginRadio);
        mainPanel.add(new JLabel());
        mainPanel.add(registerRadio);
        mainPanel.add(new JLabel());
        mainPanel.add(startButton);
        frame.add(mainPanel, BorderLayout.CENTER);

        startButton.addActionListener(e -> {
            if (!validateFields()) {
                showDialog("Please fill in all required fields.");
                return;
            }

            String ipAddress = ipAddressField.getText();
            int port = Integer.parseInt(portField.getText());
            String username = userNameField.getText();
            String password = new String(passwordField.getPassword());
            String phoneNumber = phoneNumberField.getText();
            boolean isLogin = loginRadio.isSelected();

            Contact contact = new Contact(0, username, password, null, phoneNumber);
            RequestType type = isLogin ? RequestType.LOGIN : RequestType.REGISTER;
            client = new Client(ipAddress, port);
            Response response = client.sendRequest(type, contact);
            if (ResponseType.SUCCESS.equals(response.getType())) {
                frame.setVisible(false);
                new ChatGui(client, (Contact) response.getData());
                frame.dispose();
            } else {
                showDialog(response.getMessage());
            }
        });

        // Display the frame
        frame.setVisible(true);
    }

    private void showDialog(String msg) {
        JOptionPane.showMessageDialog(frame, msg);
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
        SwingUtilities.invokeLater(ClientUI::new);
    }
}
