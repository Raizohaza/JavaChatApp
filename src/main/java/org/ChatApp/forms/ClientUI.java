package org.ChatApp.forms;

import org.ChatApp.model.Contact;
import org.ChatApp.model.RequestType;
import org.ChatApp.model.Response;
import org.ChatApp.model.ResponseType;
import org.ChatApp.socket.Client;

import javax.swing.*;
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
        frame.setLayout(new GridLayout(8, 2, 10, 10));

        // Initialize components
        ipAddressField = new JTextField("127.0.0.1");
        portField = new JTextField("1234");
        userNameField = new JTextField("aa");
        passwordField = new JPasswordField("bb");
        phoneNumberField = new JTextField("123456789");
        loginRadio = new JRadioButton("Login");
        registerRadio = new JRadioButton("Register");
        JButton startButton = new JButton("Start");

        loginRadio.setSelected(true);

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
                showDialog(response.getMessage());
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
