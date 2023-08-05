package org.chatapp.forms;

import org.chatapp.socket.Server;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ServerUI {
    private final JTextField ipAddressField;
    private final JTextField portField;
    private final JButton startButton;
    private final JButton stopButton;
    private final JTextPane logTextPane;
    public Server server;
    Thread tServer = null;

    public ServerUI() {
        JFrame frame = new JFrame("Server");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new BorderLayout());

        // Initialize components
        ipAddressField = new JTextField(getCurrentIPAddress());
        portField = new JTextField("1234");
        startButton = new JButton("Start");
        stopButton = new JButton("Stop");
        logTextPane = new JTextPane();
        PrintStream printStream = new PrintStream(new TextAreaOutputStream(logTextPane));
        System.setOut(printStream);

        DefaultCaret caret = (DefaultCaret) logTextPane.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        // Create input panel using GridBagLayout
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);

        inputPanel.add(new JLabel("IP Address:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        inputPanel.add(ipAddressField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        inputPanel.add(new JLabel("Port:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        inputPanel.add(portField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.CENTER;
        gbc.anchor = GridBagConstraints.CENTER;
        inputPanel.add(startButton, gbc);

        gbc.gridy = 3;
        inputPanel.add(stopButton, gbc);

        frame.add(inputPanel, BorderLayout.NORTH);
        frame.add(new JScrollPane(logTextPane), BorderLayout.CENTER);

        // Add action listeners for the Start and Stop buttons
        startButton.addActionListener((ActionEvent e) -> startServer());

        stopButton.addActionListener((ActionEvent e) -> stopServer());

        // Disable the Stop button initially
        stopButton.setEnabled(false);

        // Display the frame
        frame.setVisible(true);
    }

    private String getCurrentIPAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "127.0.0.1"; // Default to localhost if IP address cannot be determined
        }
    }

    private void startServer() {
        String ipAddress = ipAddressField.getText();
        int port = Integer.parseInt(portField.getText());

        tServer = new Thread(() -> {
            try {
                new Server(port, port + 1, this);
                logMessage("Server started on " + ipAddress + ":" + port);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            } finally {
                server.stop();
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
            }
        });
        tServer.start();
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
    }

    private void stopServer() {
        if (server != null) {
            server.stop();
            logMessage("Server stopped");
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
        }
    }

    private void logMessage(String message) {
        logTextPane.setText(logTextPane.getText() + message + "\n");
    }

    private static class TextAreaOutputStream extends OutputStream {
        private final JTextPane textPane;

        public TextAreaOutputStream(JTextPane textPane) {
            this.textPane = textPane;
        }

        @Override
        public void write(int b) {
            try {
                textPane.getDocument().insertString(textPane.getDocument().getLength(), String.valueOf((char) b), null);
            } catch (BadLocationException e) {
                System.out.println(e.getMessage());
            }
            textPane.setCaretPosition(textPane.getDocument().getLength());
        }
    }

    public static void main(String[] args) {
        new ServerUI();
    }
}
