import javax.swing.*;

class User {
    private final String username;
    private final String imagePath;
    private ImageIcon image;
    public User(String username, String imagePath) {
        this.username = username;
        this.imagePath = imagePath;
    }
    public String getUsername() {
        return username;
    }
    public ImageIcon getImage() {
        if (image == null) {
            image = new ImageIcon(imagePath);
        }
        return image;
    }
    // Override standard toString method to give a useful result
    public String toString() {
        return username;
    }
}