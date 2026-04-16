import javax.swing.*;
import java.awt.*;

public class GameWindow extends JFrame {
    private Gamestate state;
    private GamePanel gamePanel;
    private CharacterSelectPanel menuPanel;

    public GameWindow() {
        setTitle("Java Smash");
        setSize(1280, 720);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        showMenu(); // Start at the character select screen
        setVisible(true);
    }

    private void showMenu() {
        // Create the menu and give it a "callback" (what to do when a button is clicked)
        menuPanel = new CharacterSelectPanel(choice -> {
            startGame(choice);
        });
        
        getContentPane().removeAll();
        add(menuPanel);
        revalidate();
        repaint();
    }

    private void startGame(String characterChoice) {
        state = new Gamestate();
        state.setupSession(characterChoice); // Pass the choice to the state

        gamePanel = new GamePanel(state);
        
        // Remove menu and add game
        getContentPane().removeAll();
        add(gamePanel);
        
        // Input handling
        addKeyListener(new InputHandler(state));
        setFocusable(true);
        requestFocusInWindow();

        revalidate();
        repaint();

        // The Game Loop
        new Timer(16, e -> {
            state.update();
            gamePanel.repaint();
        }).start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GameWindow());
    }
}
