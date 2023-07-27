import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class UI extends JFrame {

    final Integer height = 600;
    final Integer width = 600;

    public UI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        setResizable(false);
        setTitle("Chat app");
        setSize(width, height);
        setLayout(new FlowLayout());
        setDefaultLookAndFeelDecorated(true);

        JPanel panel = new JPanel();
        panel.setBackground(Color.CYAN);
        panel.setPreferredSize(new Dimension(width, 70));
        panel.setLayout(new FlowLayout());

        ImageIcon icon = new ImageIcon("res/back.png");
        Image icon2 = icon.getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH);
        icon = new ImageIcon(icon2);
        JLabel btnBack = new JLabel("Back");
        btnBack.setIcon(icon);

        btnBack.addMouseListener(backHandler());
        JPanel clientInfo = new JPanel(new GridLayout(2,1));
        JLabel labelName = new JLabel("Nguyen Van A");
        JLabel labelStatus = new JLabel("Active now");
        clientInfo.add(labelName);
        clientInfo.add(labelStatus);

        panel.add(btnBack);
        panel.add(clientInfo);

        add(panel);
        setVisible(true);
//        pack();

    }

    private static MouseAdapter backHandler() {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                System.exit(0);
            }
        };
    }


    public static void main(String[] args) {
        new UI();
    }
}
