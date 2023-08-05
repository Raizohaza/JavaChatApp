package org.chatapp.forms;

import org.chatapp.model.*;
import org.chatapp.socket.ChatEventListener;
import org.chatapp.socket.Client;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatGui extends JFrame implements ChatEventListener {
    private JButton btnSend;
    private JTextField textMsg;
    private JPanel panelMain;
    private JTextPane chatArea;
    private JButton btnSendFile;
    private JLabel textFriendName;
    final Integer height = 360;
    final Integer width = 650;
    Contact selectedUser;
    final Contact loginedContact;
    List<Message> messageList = new ArrayList<>();
    final FriendListChoose panel1;
    final Client client;
    Integer conversationId = 0;

    public ChatGui(Client client, Contact loginedContact) {
        this.client = client;
        this.client.getEventManager().subscribe("message", this);
        this.client.getEventManager().subscribe("online", this);
        this.client.getEventManager().subscribe("offline", this);

        this.loginedContact = loginedContact;

        initComponents();
        //user info
        panel1 = new FriendListChoose(this);

        if (panel1.getSelectedContact() != null) {
            selectedUser = panel1.getSelectedContact();
            textFriendName.setText(selectedUser.getUser_name());
            getConversation();
        }

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setDefaultLookAndFeelDecorated(true);
        setTitle("Chat app: " + loginedContact.getUser_name());

        btnSend.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                String msg = textMsg.getText();
                Response response = client.sendRequest(RequestType.SEND_MESSAGE, new Message(0, loginedContact.getPhone_number(), msg, new Date(), conversationId));
                System.out.println(loginedContact);
                if (response.getType().equals(ResponseType.SUCCESS)) {
                    appendMessage((Message) response.getData());
                } else System.out.println(response);
            }
        });

        btnSendFile.addActionListener(e ->
        {
            JFileChooser fileChooser = new JFileChooser();
            int returnValue = fileChooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                try {
                    Response response = sendFile(selectedFile);
                    if (response.getType().equals(ResponseType.SUCCESS)) {
                        Message msg = (Message) response.getData();
                        appendFileMessage(msg.getMessage_text());
                    } else System.out.println(response);
                } catch (IOException ex) {
                    System.out.println(ex.getMessage());
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

//        startUpdateTimer();

        // Add a MouseListener to the chatArea
        chatArea.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                StyledDocument doc = chatArea.getStyledDocument();
                Element elem = doc.getCharacterElement(chatArea.viewToModel2D(e.getPoint()));

                // Check if the clicked element represents a File message
                // Extract the file name from the text
                String messageText;
                try {
                    messageText = doc.getText(elem.getStartOffset(), elem.getEndOffset() - elem.getStartOffset());
                } catch (BadLocationException ex) {
                    System.out.println(ex.getMessage());
                    return;
                }

                if (messageText.contains("File: ")) {
                    // Extract the file name from the message text
                    int startIndex = messageText.indexOf("File: ") + 6;
                    String fileName = messageText.substring(startIndex);

                    System.out.println("File Name: " + fileName);
                    client.downloadFile(fileName);
                }
            }
        });
        setVisible(true);
    }

    private Response sendFile(File selectedFile) throws IOException {
        try(FileInputStream fileInputStream = new FileInputStream(selectedFile)){
            byte[] fileData = fileInputStream.readAllBytes();

            FileMessage fileMessage = new FileMessage();
            fileMessage.setFrom_number(loginedContact.getPhone_number());
            fileMessage.setSent(new Date());
            fileMessage.setConversation_id(conversationId);
            fileMessage.setFileName(selectedFile.getName());
            fileMessage.setFileData(fileData);
            return client.sendRequest(RequestType.SEND_FILE, fileMessage);
        }
    }

    private void getConversation() {
        Response response = client.sendRequest(RequestType.CREATE_CONVERSATION, new CreateConversationData(loginedContact.getPhone_number(), selectedUser.getPhone_number()));
        if (ResponseType.SUCCESS.equals(response.getType())) {
            conversationId = (Integer) response.getData();
        } else {
            System.out.println(response.getMessage());
        }
        updateChatArea();
    }

    private void updateChatArea() {
        messageList = getMessageHistory(conversationId);
        if (messageList != null) {
            StyledDocument document = chatArea.getStyledDocument();
            try {
                document.remove(0, document.getLength());
                messageList.forEach(message -> {
                    if (message.getMessage_text().contains("File: "))
                        appendFileMessage(message.getMessage_text());
                    else appendMessage(message);
                });
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
        if (message == null) return;

        if (message.getConversation_id() != conversationId) return;

        if (message.getFrom_number().equals(loginedContact.getPhone_number())) {
            String chatText = "\n" + loginedContact.getUser_name() + ": " + message.getMessage_text();
            addStyledLine(chatText, StyleConstants.ALIGN_RIGHT, Color.BLACK);
            return;
        }

        String chatText = "\n" + selectedUser.getUser_name() + ": " + message.getMessage_text();
        addStyledLine(chatText, StyleConstants.ALIGN_LEFT, Color.BLUE);

    }

    private void appendFileMessage(String message) {
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
    public List<ContactOnline> getContacts() {
        Response response = client.sendRequest(RequestType.GET_CONTACTS, null);
        if (response.getData() != null) {
            List<ContactOnline> contactList = (List<ContactOnline>) response.getData();
            contactList.removeIf(item -> item.getContact().getPhone_number().equals(loginedContact.getPhone_number()));
            return contactList;
        }
        return null;
    }

    @Override
    public void onChatEvent(String eventType, Object data) {
        switch (eventType) {
            case "online", "offline" -> panel1.updateOnlineList(getContacts());
            case "message" -> {
                Message newMessage = (Message) data;
                if (!newMessage.getFrom_number().equals(loginedContact.getPhone_number())) {
                    if (newMessage.getMessage_text().contains("File: ")) {
                        appendFileMessage(newMessage.getMessage_text());
                        chatArea.setCaretPosition(chatArea.getDocument().getLength());
                        return;
                    }
                    appendMessage(newMessage);
                }
                chatArea.setCaretPosition(chatArea.getDocument().getLength());
            }
            default -> {
            }
        }
    }

    private void initComponents() {
        panelMain = new JPanel();
        textFriendName = new JLabel("Friend");
        JScrollPane scrollPane = new JScrollPane();
        chatArea = new JTextPane();
        chatArea.setEditable(false);
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
        scrollPane.setViewportView(chatArea);
        panelMain.add(scrollPane, gbc);

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

