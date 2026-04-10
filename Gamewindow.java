import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class Gamewindow extends JFrame {
    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;
    private static final int TARGET_FPS = 60;

    private GamePanel gamePanel;
    private Gamestate gamestate;
    private Timer gameLoop;

    public Gamewindow() {
        setTitle("Smash Bros Baseline");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

        gamestate = new Gamestate();
        gamePanel = new GamePanel(gamestate);
        add(gamePanel);

        // Input handling
        InputHandler input = new InputHandler(gamestate);
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
