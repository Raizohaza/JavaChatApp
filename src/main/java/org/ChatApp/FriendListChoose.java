package org.ChatApp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class FriendListChoose extends JPanel {
    private JPanel panelMain;
    final Integer height = 600;
    final Integer width = 450;
    User selectedUser;
    ChatGui.iChangeChat changeChatFunc = null;

    public FriendListChoose() {
        initPanel();
    }

    public FriendListChoose(ChatGui.iChangeChat changeChatFunc) {
        this.changeChatFunc = changeChatFunc;
        initPanel();
    }

    private void initPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        User[] userList = new User[]{
                new User("Nguyen Van A", "res/user.png"),
                new User("Pham Thi B", "res/user.png"),
                new User("Nguyen Van C", "res/user.png"),
                new User("Nguyen Van D", "res/user.png"),
                new User("Nguyen Van E", "res/user.png"),
                new User("Nguyen Van F", "res/user.png"),
                new User("Nguyen Van G", "res/user.png"),
                new User("Nguyen Van E", "res/user.png"),
                new User("Nguyen Van F", "res/user.png"),
                new User("Nguyen Van G", "res/user.png"),
                new User("Nguyen Van E", "res/user.png"),
                new User("Nguyen Van F", "res/user.png"),
                new User("Nguyen Van G", "res/user.png"),
                new User("Nguyen Van E", "res/user.png"),
                new User("Nguyen Van F", "res/user.png"),
                new User("Nguyen Van G", "res/user.png"),

        };

        JList<User> list = new JList<>(userList);
        list.setCellRenderer(new UserCellRenderer());
        list.setAlignmentX(Component.CENTER_ALIGNMENT);
        list.setSelectedIndex(0);
        selectedUser = userList[0];

        if (changeChatFunc != null)
            changeChatFunc.changeChat(selectedUser);

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
                selectedUser = list.getSelectedValue();
                if (changeChatFunc != null) {
                    changeChatFunc.changeChat(selectedUser);
                }
            }
        });
        setPreferredSize(new Dimension(50, height));
    }

    public User getSelectedUser() {
        return selectedUser;
    }

    public static void main(String[] args) {
        JFrame f = new JFrame();
        f.setSize(600, 450);
        f.add(new FriendListChoose());
        f.setVisible(true);
    }
}
