package org.ChatApp.forms;

import lombok.Getter;
import org.ChatApp.model.Contact;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class FriendListChoose extends JPanel {
    private JPanel panelMain;
    final Integer height = 600;
    final Integer width = 450;
    @Getter
    Contact selectedContact;
    ChatGui chatGui;

    public FriendListChoose(ChatGui chatGui) {
        this.chatGui = chatGui;
        initPanel();
    }

    private void initPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
//        Contact[] ContactList = new Contact[]{
//                new Contact(0, "John", "1", "res/Contact.png", "0123456780"),
//                new Contact(0, "John1", "1", "res/Contact.png", "0123456781"),
//                new Contact(0, "John2", "1", "res/Contact.png", "0123456782"),
//                new Contact(0, "John3", "1", "res/Contact.png", "0123456783"),
//        };
        Contact[] contacts = chatGui.getContacts().toArray(new Contact[0]);
        JList<Contact> list = new JList<>(contacts);
        list.setCellRenderer(new ContactCellRenderer());
        list.setAlignmentX(Component.CENTER_ALIGNMENT);
        list.setSelectedIndex(0);
        selectedContact = contacts[0];

        if (chatGui != null)
            chatGui.changeChat(selectedContact);

        JScrollPane sp = new JScrollPane(list);
//        sp.setViewportView(list);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        JLabel friendLabel = new JLabel("Friend list");
        add(new JLabel(" "));

        add(friendLabel);
        add(sp);
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                selectedContact = list.getSelectedValue();
                if (chatGui != null) {
                    chatGui.changeChat(selectedContact);
                }
            }
        });
        setPreferredSize(new Dimension(50, height));
    }

    public static void main(String[] args) {
        JFrame f = new JFrame();
        f.setSize(600, 450);
        f.add(new FriendListChoose(null));
        f.setVisible(true);
    }
}
