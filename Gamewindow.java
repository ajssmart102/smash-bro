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
    private javax.swing.Timer gameLoop;
 
    public Gamewindow() {
        setTitle("Smash Bros Baseline");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);
 
        // Show map select screen first instead of jumping straight into the game
        showMapSelect();
 
        setVisible(true);
    }
 
    private void showMapSelect() {
        MapSelectScreen mapSelect = new MapSelectScreen((mapIndex, mapName) -> {
            // Player confirmed a map — remove select screen and start game
            getContentPane().removeAll();
            initGame(mapIndex);
            revalidate();
            repaint();
        });
 
        getContentPane().removeAll();
        add(mapSelect);
        revalidate();
        repaint();
    }
 
    private void initGame(int mapIndex) {
        gamestate = new Gamestate();
        gamestate.setMap(mapIndex); // tells Gamestate which map was picked
 
        gamePanel = new GamePanel(gamestate);
        add(gamePanel);
 
        InputHandler input = new InputHandler(gamestate);
        addKeyListener(input);
        setFocusable(true);
        requestFocusInWindow();
 
        startGame();
    }
 
    public void startGame() {
        int delay = 1000 / TARGET_FPS;
        gameLoop = new javax.swing.Timer(delay, e -> {
            gamestate.update();
            gamePanel.repaint();
        });
        gameLoop.start();
    }
}
