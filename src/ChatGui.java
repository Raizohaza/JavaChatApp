import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
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
    User selectedUser;
    List<String> friendNames;
    private final List<String> conversationList;
    FriendListChoose panel1;

    public ChatGui() {
        iChangeChat iChangeChat = this::changeChat;
        panel1 = new FriendListChoose(iChangeChat);

        //user info
        selectedUser = panel1.getSelectedUser();
        friendNames = new ArrayList<>();
        friendNames.add("Pham Thi B");
        textFriendName.setText(selectedUser.getUsername());

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setDefaultLookAndFeelDecorated(true);
        setTitle("Chat app");
        conversationList = new ArrayList<>();

        btnSend.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                String msg = textMsg.getText();
                conversationList.add("\n" + selectedUser.getUsername() + ": " + msg);
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

    public interface iChangeChat {
        void changeChat(User selectedUser);
    }

    public void changeChat(User selectedUser) {
        System.out.println("selectedUser" + selectedUser);

        this.textFriendName.setText(selectedUser.getUsername());
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(ChatGui::new);
    }
}
