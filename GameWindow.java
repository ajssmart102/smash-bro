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
        menuPanel = new CharacterSelectPanel(choice -> {
            startGame(choice);
        });
        
        getContentPane().removeAll();
        add(menuPanel);
        revalidate();
        repaint();
    }

    private void startGame(String characterChoice) {
        // 1. Initialize State
        state = new Gamestate();
        state.setupSession(characterChoice); 

        // 2. Create and Configure Panel
        gamePanel = new GamePanel(state);
        gamePanel.setFocusable(true); 

        // 3. Clean up the Window
        getContentPane().removeAll();
        add(gamePanel);

        // 4. Setup Input Logic (No external file needed)
        gamePanel.addKeyListener(new KeyAdapter() {
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
        });

        // 5. Finalize Layout
        revalidate();
        repaint();

        // 6. Force Focus
        SwingUtilities.invokeLater(() -> {
            gamePanel.requestFocusInWindow();
        });

        // 7. Start Game Loop
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
