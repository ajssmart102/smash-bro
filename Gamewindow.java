
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

        // Flow: Map Select -> Character Select -> Game Start
        showMapSelect();

        setVisible(true);
    }

    private void showMapSelect() {
        MapSelectScreen mapSelect = new MapSelectScreen((mapIndex, mapName) -> {
            showCharacterSelect(mapIndex);
        });

        getContentPane().removeAll();
        add(mapSelect);
        revalidate();
        repaint();
    }

    private void showCharacterSelect(int mapIndex) {
        CharacterSelectScreen charSelect = new CharacterSelectScreen((charName) -> {
            getContentPane().removeAll();
            initGame(mapIndex, charName);
            revalidate();
            repaint();
        });

        getContentPane().removeAll();
        add(charSelect);
        revalidate();
        repaint();
    }

    private void initGame(int mapIndex, String charName) {
        // 1. Initialize Gamestate
        gamestate = new Gamestate();
        
        // 2. Call the new initSession method to set the map AND spawn the chosen fighter
        gamestate.initSession(mapIndex, charName); 

        // 3. Setup the Panel
        gamePanel = new GamePanel(gamestate);
        add(gamePanel);

        // 4. Handle Input Cleanup
        // Removing old listeners is critical so menu clicks don't interfere with movement
        for (KeyListener kl : getKeyListeners()) {
            removeKeyListener(kl);
        }
        
        InputHandler input = new InputHandler(gamestate);
        addKeyListener(input);
        
        setFocusable(true);
        requestFocusInWindow();

        // 5. Start the engine
        startGame();
    }

    public void startGame() {
        if (gameLoop != null && gameLoop.isRunning()) {
            gameLoop.stop();
        }

        int delay = 1000 / TARGET_FPS;
        gameLoop = new javax.swing.Timer(delay, e -> {
            gamestate.update();
            gamePanel.repaint();
        });
        gameLoop.start();
    }
}
