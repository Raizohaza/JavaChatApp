package org.ChatApp.forms;

import org.ChatApp.model.*;
import org.ChatApp.socket.Client;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatGui extends JFrame {
    private JButton btnSend;
    private JTextField textMsg;
    private JPanel panelMain;
    private JPanel panelInput;
    private JTextPane chatArea;
    private JButton btnSendFile;
    private JLabel textFriendName;
    private JScrollPane scrollpane;
    final Integer height = 600;
    final Integer width = 600;
    Contact selectedUser;
    Contact contact;
    List<Message> messageList = new ArrayList<>();
    FriendListChoose panel1;
    Client client;
    Integer conversationId = 0;
    private Timer updateTimer;

    public ChatGui(Client client, Contact contact) {
        this.client = client;
        this.contact = contact;

        initComponents();
        //user info
        panel1 = new FriendListChoose(this);

        if (panel1.getSelectedContact().getUser_name() != null) {
            selectedUser = panel1.getSelectedContact();
            textFriendName.setText(selectedUser.getUser_name());
            getConversation();
        }

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setDefaultLookAndFeelDecorated(true);
        setTitle("Chat app: " + contact.getUser_name());

        btnSend.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                String msg = textMsg.getText();
                Response response = client.sendRequest(RequestType.SEND_MESSAGE, new Message(0, contact.getPhone_number(), msg, new Date(), conversationId));
                System.out.println(contact);
                if (response.getType().equals(ResponseType.SUCCESS)) {
                    appendMessage((Message) response.getData());
                } else System.out.println(response);
            }
        });

        btnSendFile.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                JFileChooser fileChooser = new JFileChooser();
                int result = fileChooser.showOpenDialog(null);

                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    String filePath = selectedFile.getAbsolutePath();

                    Response response = client.sendFile(filePath, contact, selectedUser, conversationId);
                    if (ResponseType.SUCCESS.equals(response.getType())) {
                        String fileInfo = "File sent: " + selectedFile.getName();
                        appendInfoMessage(fileInfo);
                    } else {
//                        showDialog("Failed to send file: " + response.getMessage());
                    }
                }
            }
        });

        // Create a JSplitPane to divide the frame vertically
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panel1, panelMain);
        splitPane.setResizeWeight(0.33); // Set the ratio for panel1 and panelMain

        // Set up the content pane with a margin
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Add a margin
        contentPane.add(splitPane, BorderLayout.CENTER);

        setContentPane(contentPane);

        pack();
        setSize(width, height);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                client.close();
            }
        });

        startUpdateTimer();
        setVisible(true);
    }

    private void startUpdateTimer() {
        int intervalMilliseconds = 1000; // 1 second interval
        updateTimer = new Timer(intervalMilliseconds, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Updating chat area...");
                updateChatArea();
            }
        });

        updateTimer.start();
    }

    private void appendInfoMessage(String message) {
        StyledDocument document = chatArea.getStyledDocument();
        SimpleAttributeSet alignStyle = new SimpleAttributeSet();
        StyleConstants.setAlignment(alignStyle, StyleConstants.ALIGN_CENTER);
        StyleConstants.setFontSize(alignStyle, 12);
        StyleConstants.setItalic(alignStyle, true);

        try {
            document.insertString(document.getLength(), "\n" + message, alignStyle);
            document.setParagraphAttributes(document.getLength(), 1, alignStyle, false);
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
    }
    private void getConversation() {
        Response response = client.sendRequest(RequestType.CREATE_CONVERSATION, new CreateConversationData(contact.getPhone_number(), selectedUser.getPhone_number()));
        if (ResponseType.SUCCESS.equals(response.getType())) {
            conversationId = (Integer) response.getData();
        } else {
            System.out.println(response.getMessage());
        }
        updateChatArea();
    }

    private void updateChatArea() {
        List<Message> newMessageList = getMessageHistory(conversationId);
        System.out.println(newMessageList);
        if (newMessageList != null
                && !messageList.isEmpty()
                && newMessageList.size() == messageList.size())
            return;
        messageList = newMessageList;
        if (messageList != null) {
            StyledDocument document = chatArea.getStyledDocument();
            try {
                document.remove(0, document.getLength());
                messageList.forEach(this::appendMessage);
                chatArea.setCaretPosition(chatArea.getDocument().getLength());
            } catch (BadLocationException e) {

                throw new RuntimeException(e);
            }

        }

    }

    @SuppressWarnings("unchecked")
    private List<Message> getMessageHistory(Integer id) {
        Response response = client.sendRequest(RequestType.GET_MESSAGE_HISTORY, id);
        System.out.println("req: " + id + "\nres: " + response);
        if (ResponseType.SUCCESS.equals(response.getType())) {
            Object data = response.getData();
            List<Message> messages = new ArrayList<>();
            if (data instanceof Message)
                messages.add((Message) data);
            if (data instanceof List<?>)
                messages = (List<Message>) data;
            return messages;
        }
        return null;
    }

    private void appendMessage(Message message) {
        if (message == null)
            return;
        if (message.getFrom_number().equals(contact.getPhone_number())) {
            String chatText = "\n" + contact.getUser_name() + ": " + message.getMessage_text();
            addStyledLine(chatText, StyleConstants.ALIGN_RIGHT, Color.BLACK);
        } else {
            String chatText = "\n" + selectedUser.getUser_name() + ": " + message.getMessage_text();
            addStyledLine(chatText, StyleConstants.ALIGN_LEFT, Color.BLUE);
        }
    }

    private void addStyledLine(String msg, int align, Color color) {
        StyledDocument document = chatArea.getStyledDocument();
        SimpleAttributeSet alignStyle = new SimpleAttributeSet();

        StyleConstants.setForeground(alignStyle, color);
        StyleConstants.setAlignment(alignStyle, align);
        try {
            document.insertString(document.getLength(), msg, alignStyle);
            document.setParagraphAttributes(document.getLength(), 1, alignStyle, false);
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
    }


    public void changeChat(Contact selectedUser) {
        System.out.println("selectedUser" + selectedUser);
        this.selectedUser = selectedUser;
        StyledDocument document = chatArea.getStyledDocument();
        try {
            document.remove(0, document.getLength());
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
        getConversation();
        this.textFriendName.setText(selectedUser.getUser_name());
    }

    @SuppressWarnings("unchecked")
    public List<Contact> getContacts() {
        Response response = client.sendRequest(RequestType.GET_CONTACTS, null);
        if (response.getData() != null) {
            List<Contact> contactList = (List<Contact>) response.getData();
            contactList.removeIf(item -> item.getPhone_number().equals(contact.getPhone_number()));
            return contactList;
        }
        return null;
    }

    private void initComponents() {
        panelMain = new JPanel();
        textFriendName = new JLabel("Friend");
        JScrollPane scrollpane = new JScrollPane();
        chatArea = new JTextPane();
//        chatArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Add a margin

        JPanel panelInput = new JPanel();
        textMsg = new JTextField();

        btnSend = new JButton("Send");
        btnSendFile = new JButton("SendFile");

        panelMain.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(20, 20, 0, 0);
        panelMain.add(textFriendName, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 20, 0, 0);
        chatArea.setEditable(false);
        scrollpane.setViewportView(chatArea);
        panelMain.add(scrollpane, gbc);

        panelInput.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 20, 0, 0);
        panelMain.add(panelInput, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 0, 0);
        panelInput.add(textMsg, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 10, 0, 0);
        panelInput.add(btnSend, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 10, 0, 0);
        panelInput.add(btnSendFile, gbc);
    }
}

