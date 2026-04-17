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
        
        // 3. Clean up the Window
        getContentPane().removeAll();
        add(gamePanel);

        // 4. Setup Input Handler
        // Attach the listener to the PANEL, not the frame.
        // This avoids the need to manually clear listeners from the JFrame.
        InputHandler input = new InputHandler(state);
        gamePanel.addKeyListener(input);
        gamePanel.setFocusable(true);

        // 5. Finalize Layout
        revalidate();
        repaint();

        // 6. Force Focus
        // We only need the panel to be focused to receive the keys.
        gamePanel.requestFocusInWindow();

        // 7. Start/Restart Game Loop
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
