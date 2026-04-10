import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class Gamewindow extends JFrame {
    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;
    private static final int TARGET_FPS = 60;

    private GamePanel gamePanel;
    private GameState gameState;
    private Timer gameLoop;

    public GameWindow() {
        setTitle("Smash Bros Baseline");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

        gameState = new GameState();
        gamePanel = new GamePanel(gameState);
        add(gamePanel);

        // Input handling
        InputHandler input = new InputHandler(gameState);
        addKeyListener(input);
        setFocusable(true);
    }

    public void startGame() {
        int delay = 1000 / TARGET_FPS;
        gameLoop = new Timer(delay, e -> {
            gameState.update();
            gamePanel.repaint();
        });
        gameLoop.start();
    }
}
