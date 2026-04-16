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
            // Once map is picked, move to character selection
            showCharacterSelect(mapIndex);
        });

        getContentPane().removeAll();
        add(mapSelect);
        revalidate();
        repaint();
    }

    private void showCharacterSelect(int mapIndex) {
        // Create the character selection screen
        CharacterSelectScreen charSelect = new CharacterSelectScreen((charName) -> {
            // Once character is picked, initialize the game with BOTH choices
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
        gamestate = new Gamestate();
        
        // Pass selections to your gamestate
        gamestate.setMap(mapIndex); 
        // Assuming your Gamestate has a setCharacter method:
        // gamestate.setCharacter(charName); 

        gamePanel = new GamePanel(gamestate);
        add(gamePanel);

        // Handle Input
        // Note: Remove old KeyListeners to prevent "ghost" inputs from previous screens
        for (KeyListener kl : getKeyListeners()) {
            removeKeyListener(kl);
        }
        
        InputHandler input = new InputHandler(gamestate);
        addKeyListener(input);
        
        setFocusable(true);
        requestFocusInWindow();

        startGame();
    }

    public void startGame() {
        // Stop any existing loop if re-running
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
