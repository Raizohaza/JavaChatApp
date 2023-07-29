package org.ChatApp.forms;

import org.ChatApp.model.Contact;

import javax.swing.*;
import java.awt.*;

class ContactCellRenderer extends JLabel implements ListCellRenderer<Object> {

    public Component getListCellRendererComponent(
            JList<?> list,           // the list
            Object value,            // value to display
            int index,               // cell index
            boolean isSelected,      // is the cell selected
            boolean cellHasFocus)    // does the cell have focus
    {
        Contact user = (Contact) value;
        setText(user.getUser_name());
        setIcon(user.getImage());
        setPreferredSize(new Dimension(100,50));
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