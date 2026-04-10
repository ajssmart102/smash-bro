import javax.swing.*;
import javax.swing.Timer; // add: forces Timer to resolve to Swing, not java.util
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class Gamewindow extends JFrame {
    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;
    private static final int TARGET_FPS = 60;

    private GamePanel gamePanel;
    private Gamestate gamestate;
    private javax.swing.Timer gameLoop; // add: explicit type to avoid ambiguity

    public Gamewindow() {
        setTitle("Smash Bros Baseline");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

        gamestate = new Gamestate();
        gamePanel = new GamePanel(gamestate);
        add(gamePanel);

        InputHandler input = new InputHandler(gamestate);
        addKeyListener(input);
        setFocusable(true);
        setVisible(true);
    }

    public void startGame() {
        int delay = 1000 / TARGET_FPS;
        gameLoop = new javax.swing.Timer(delay, e -> { // add: explicit instantiation
            gamestate.update();
            gamePanel.repaint();
        });
        gameLoop.start();
    }
}
