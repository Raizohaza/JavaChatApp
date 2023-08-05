package org.chatapp.forms;

import org.chatapp.model.ContactOnline;

import javax.swing.*;
import java.awt.*;

class ContactCellRenderer extends JPanel implements ListCellRenderer<Object> {
    private final JLabel nameLabel;
    private final JLabel statusLabel;

    public ContactCellRenderer() {
        setLayout(new BorderLayout());

        nameLabel = new JLabel();
        statusLabel = new JLabel();

        add(nameLabel, BorderLayout.WEST);
        add(statusLabel, BorderLayout.EAST);
    }
    public Component getListCellRendererComponent(
            JList<?> list,           // the list
            Object value,            // value to display
            int index,               // cell index
            boolean isSelected,      // is the cell selected
            boolean cellHasFocus)    // does the cell have focus
    {
        ContactOnline user = (ContactOnline) value;
        nameLabel.setText(user.getContact().getUser_name());
        nameLabel.setIcon(user.getContact().getImage());
        setPreferredSize(new Dimension(100,50));

        if (user.isOnline()) {
            statusLabel.setText("Online");
            statusLabel.setForeground(Color.GREEN.darker());
        } else {
            statusLabel.setText("Offline");
            statusLabel.setForeground(Color.RED);
        }


        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
        setEnabled(list.isEnabled());
        setFont(list.getFont());
        setOpaque(true);
        return this;
    }
}