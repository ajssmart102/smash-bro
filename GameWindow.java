import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class GameWindow extends JFrame implements KeyListener {
    private Gamestate state;
    private GamePanel gamePanel;
    private CharacterSelectPanel menuPanel;
    private MapSelectPanel mapPanel; // NEW
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

    // Step 1: Pick Character
    private void showCharacterMenu() {
        menuPanel = new CharacterSelectPanel(characterChoice -> {
            showMapMenu(characterChoice); // Move to Map Select next
        });
        
        getContentPane().removeAll();
        add(menuPanel);
        revalidate();
        repaint();
    }

    // Step 2: Pick Map (NEW)
    private void showMapMenu(String characterChoice) {
        mapPanel = new MapSelectPanel(chosenMap -> {
            startGame(characterChoice, chosenMap); // Finally start the game
        });

        getContentPane().removeAll();
        add(mapPanel);
        revalidate();
        repaint();
    }

    // Step 3: Start the Session
    private void startGame(String characterChoice, MapData chosenMap) {
        state = new Gamestate();
        // Updated setupSession to take the MapData object
        state.setupSession(characterChoice, "P2_Placeholder", chosenMap); 

        gamePanel = new GamePanel(state);
        gamePanel.setFocusable(true); 

        getContentPane().removeAll();
        add(gamePanel);
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

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
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
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GameWindow());
    }
}