package org.ChatApp.forms;

import org.ChatApp.model.Contact;
import org.ChatApp.model.RequestType;
import org.ChatApp.model.Response;
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
import java.util.List;

public class ChatGui extends JFrame {
    private JButton btnSend;
    private JTextField textMsg;
    private JPanel panelMain;
    private JPanel panelInput;
    private JTextPane chatArea;
    private JButton btnReceive;
    private JLabel textFriendName;
    final Integer height = 600;
    final Integer width = 600;
    Contact selectedUser;
    Contact contact;
    List<String> friendNames;
    private final List<String> conversationList;
    FriendListChoose panel1;
    Client client;

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
                conversationList.add("\n" + selectedUser.getUser_name() + ": " + msg);
                String chatText = conversationList.get(conversationList.size() - 1);
                addStyledLine(chatText, StyleConstants.ALIGN_RIGHT, Color.BLACK);
            }
        });
        btnReceive.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                String msg = textMsg.getText();
                conversationList.add("\n" + friendNames.get(0) + ": " + msg);
                String chatText = conversationList.get(conversationList.size() - 1);
                addStyledLine(chatText, StyleConstants.ALIGN_LEFT, Color.BLUE);
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
        setVisible(true);
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
        this.textFriendName.setText(selectedUser.getUser_name());
    }

    @SuppressWarnings("unchecked")
    public List<Contact> getContacts() {
        Response response = client.sendRequest(RequestType.GET_CONTACTS, null);
        if (response.getData() != null)
            return (List<Contact>) response.getData();
        return null;
    }
}

