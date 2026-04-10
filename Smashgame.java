import javax.swing.*;
 
public class Smashgame {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameWindow window = new GameWindow();
            window.setVisible(true);
            window.startGame();
        });
    }
}
 
