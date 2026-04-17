import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class GameWindow extends JFrame {
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
        menuPanel = new CharacterSelectPanel(choice -> startGame(choice));
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

        // This will now work because the class is defined below
        InputHandler input = new InputHandler(state);
        gamePanel.addKeyListener(input);

        revalidate();
        repaint();

        SwingUtilities.invokeLater(() -> gamePanel.requestFocusInWindow());

        if (gameLoop != null) gameLoop.stop();
        gameLoop = new Timer(16, e -> {
            state.update();
            gamePanel.repaint();
        });
        gameLoop.start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GameWindow());
    }
}

// --- PUT THIS AT THE BOTTOM OF THE SAME FILE ---
class InputHandler extends KeyAdapter {
    private Gamestate state;

    public InputHandler(Gamestate state) {
        this.state = state;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code >= 0 && code < state.keys.length) {
            state.keys[code] = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code >= 0 && code < state.keys.length) {
            state.keys[code] = false;
        }
    }
}
