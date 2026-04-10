import javax.swing.*;
 
public class Smashgame {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Gamewindow window = new Gamewindow();
            window.setVisible(true);
            window.startGame();
        });
    }
}
 
