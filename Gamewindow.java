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

        gamestate = new Gamestate();       // fix: was "Gamestate ="
        gamePanel = new GamePanel(gamestate); // fix: was "GamePanel =", wrong var name
        add(gamePanel);

        InputHandler input = new InputHandler(gamestate); // fix: consistent casing
        addKeyListener(input);
        setFocusable(true);
        setVisible(true); // fix: added so the window actually appears
    }

    public void startGame() {
        int delay = 1000 / TARGET_FPS;
        gameLoop = new Timer(delay, e -> {
            gamestate.update(); // fix: was "gameState" (wrong casing)
            gamePanel.repaint();
        });
        gameLoop.start();
    }
}
