package org.ChatApp.forms;

import org.ChatApp.model.*;
import org.ChatApp.socket.Client;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatGui extends JFrame {
    private JButton btnSend;
    private JTextField textMsg;
    private JPanel panelMain;
    private JPanel panelInput;
    private JTextPane chatArea;
    private JButton btnReceive;
    private JLabel textFriendName;
    private JScrollPane scrollpane;
    final Integer height = 600;
    final Integer width = 600;
    Contact selectedUser;
    Contact contact;
    List<String> friendNames;
    private final List<String> conversationList;
    List<Message> messageList = new ArrayList<>();
    FriendListChoose panel1;
    Client client;
    Integer conversationId = 0;

    public ChatGui(Client client, Contact contact) {
        this.client = client;
        this.contact = contact;
        //user info
        friendNames = new ArrayList<>();
        panel1 = new FriendListChoose(this);

        friendNames.add("Pham Thi B");
        if (panel1.getSelectedContact().getUser_name() != null) {
            selectedUser = panel1.getSelectedContact();
            textFriendName.setText(selectedUser.getUser_name());
            getConversation();
        }

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setDefaultLookAndFeelDecorated(true);
        setTitle("Chat app: " + contact.getUser_name());
        conversationList = new ArrayList<>();

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
        btnReceive.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                updateChatArea();
            }
        });

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;


        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        add(panel1, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.gridheight = 1;
        add(panelMain, gbc);

        setSize(width, height);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                client.close();
            }
        });

        Thread intervalThread = new Thread(() -> {
            while (true) {
                try {
                    // Your code to be executed at each interval
                    System.out.println("Interval thread is running...");
                    updateChatArea();

                    // Sleep for 1 second
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        intervalThread.start();


        setVisible(true);
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
//                this.revalidate(); //Update the scrollbar size
//                JScrollBar vertical = scrollpane.getVerticalScrollBar();
//                vertical.setValue(vertical.getMaximum());
            } catch (BadLocationException e) {

                throw new RuntimeException(e);
            }

        }

    }

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
}

