import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener; // Import the listener interface

// We add "implements KeyListener" to the class itself
public class GameWindow extends JFrame implements KeyListener {
    private Gamestate state;
    private GamePanel gamePanel;
    private CharacterSelectPanel menuPanel;
    private Timer gameLoop;

    public GameWindow() {
        setTitle("Java Smash");
        setSize(1280, 720);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        showMenu(); 
        setVisible(true);
    }

    private void showMenu() {
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
        state.setupSession(characterChoice); 

        gamePanel = new GamePanel(state);
        gamePanel.setFocusable(true); 

        getContentPane().removeAll();
        add(gamePanel);

        // Instead of a new class, we tell the panel that THIS window
        // will handle the key presses.
        gamePanel.addKeyListener(this);

        revalidate();
        repaint();

        SwingUtilities.invokeLater(() -> {
            gamePanel.requestFocusInWindow();
        });

        if (gameLoop != null) gameLoop.stop();
        gameLoop = new Timer(16, e -> {
            state.update();
            gamePanel.repaint();
        });
        gameLoop.start();
    }

    // --- KEY LISTENER METHODS (Built directly into GameWindow) ---

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        // Directly update the state's keys array
        if (state != null && code >= 0 && code < state.keys.length) {
            state.keys[code] = true;
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
    public void keyTyped(KeyEvent e) {
        // Not used for games, but required to be here
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GameWindow());
    }
}
