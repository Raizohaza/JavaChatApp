package org.chatapp.forms;

import lombok.Getter;
import org.chatapp.model.Contact;
import org.chatapp.model.ContactOnline;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class FriendListChoose extends JPanel {
    final Integer height = 600;
    @Getter
    Contact selectedContact;
    final ChatGui chatGui;
    JScrollPane sp;

    public FriendListChoose(ChatGui chatGui) {
        this.chatGui = chatGui;
        initPanel();
    }

    private void initPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JLabel friendLabel = new JLabel("Online list");
        add(new JLabel(" "));
        add(friendLabel);

        sp = new JScrollPane();
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(sp);

        updateOnlineList(chatGui.getContacts());
        setPreferredSize(new Dimension(50, height));
    }

    public void updateOnlineList(List<ContactOnline> contactList) {
        if (contactList == null)
            return;

        ContactOnline[] contacts = contactList.toArray(new ContactOnline[0]);
        JList<ContactOnline> list = new JList<>(contacts);
        list.setCellRenderer(new ContactCellRenderer());
        list.setAlignmentX(Component.CENTER_ALIGNMENT);
        list.setSelectedIndex(0);
        selectedContact = contacts[0].getContact();


        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                selectedContact = list.getSelectedValue().getContact();
                System.out.println(selectedContact);
                if (chatGui != null) {
                    chatGui.changeChat(selectedContact);
                }
            }
        });
        if (sp != null) {
            sp.setViewportView(list);
            return;
        }
        sp = new JScrollPane(list);
    }

    public static void main(String[] args) {
        JFrame f = new JFrame();
        f.setSize(600, 450);
        f.add(new FriendListChoose(null));
        f.setVisible(true);
    }
}
