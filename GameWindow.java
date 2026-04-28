import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class GameWindow extends JFrame implements KeyListener {
    private Gamestate state;
    private GamePanel gamePanel;
    private Timer gameLoop;

    public GameWindow() {
        setTitle("Java Smash");
        setSize(1280, 720);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        showCharacterMenu(); 
        setVisible(true);
    }

    // Step 1: Pick your Character
    private void showCharacterMenu() {
        CharacterSelectPanel charPanel = new CharacterSelectPanel(characterChoice -> {
            showMapMenu(characterChoice); // Move to Map Selection next
        });
        
        updateView(charPanel);
    }

    // Step 2: Pick your Map
    private void showMapMenu(String characterChoice) {
        MapSelectPanel mapPanel = new MapSelectPanel(chosenMap -> {
            startGame(characterChoice, chosenMap); // Start the game with both choices
        });

        updateView(mapPanel);
    }

    // Step 3: Launch the Game
    private void startGame(String characterChoice, MapData chosenMap) {
        state = new Gamestate();
        state.setupSession(characterChoice, chosenMap); // Updated signature

        gamePanel = new GamePanel(state);
        gamePanel.setFocusable(true); 
        gamePanel.addKeyListener(this); // Listen for inputs here

        updateView(gamePanel);

        // Ensure the game panel grabs focus for key inputs
        SwingUtilities.invokeLater(() -> gamePanel.requestFocusInWindow());

        // Game Loop (60 FPS approx)
        if (gameLoop != null) gameLoop.stop();
        gameLoop = new Timer(16, e -> {
            state.update();
            gamePanel.repaint();
        });
        gameLoop.start();
    }

    // Helper to swap panels easily
    private void updateView(JPanel panel) {
        getContentPane().removeAll();
        add(panel);
        revalidate();
        repaint();
    }

    // --- KEY LISTENER METHODS ---

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (state != null && code >= 0 && code < state.keys.length) {
            state.keys[code] = true;
        }
        
        // Quick Reset shortcut (Optional: Press ESC to go back to menu)
        if (code == KeyEvent.VK_ESCAPE) {
            if (gameLoop != null) gameLoop.stop();
            showCharacterMenu();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (state != null && code >= 0 && code < state.keys.length) {
            state.keys[code] = false;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GameWindow());
    }
}