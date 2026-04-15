import javax.swing.*;
 
public class Smashgame {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Gamewindow(); // Gamewindow now handles map select → game start
        });
    }
}
