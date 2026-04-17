import javax.swing.*;
import java.awt.*;

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
        gamePanel.setFocusable(true); // Must be focusable BEFORE adding listeners

        // 3. Clean up the Window
        getContentPane().removeAll();
        add(gamePanel);

        // 4. Setup Input Handler
        // Ensure you pass 'state' to the handler so it can move the character!
        InputHandler input = new InputHandler(state);
        gamePanel.addKeyListener(input);

        // 5. Finalize Layout
        revalidate();
        repaint();

        // 6. Force Focus (Aggressive method)
        // Sometimes requestFocusInWindow fails if called too fast. 
        // invokeLater ensures the UI is finished rendering first.
        SwingUtilities.invokeLater(() -> {
            gamePanel.requestFocusInWindow();
        });

        // 7. Start/Restart Game Loop
        if (gameLoop != null) gameLoop.stop();
        gameLoop = new Timer(16, e -> {
            state.update();
            gamePanel.repaint(); // Redraw the graphics
        });
        gameLoop.start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GameWindow());
    }
}
