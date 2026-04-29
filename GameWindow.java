import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class GameWindow extends JFrame implements KeyListener {
    private Gamestate state;
    private GamePanel gamePanel;
    private CharacterSelectPanel menuPanel;
    private MapSelectPanel mapPanel;
    private Timer gameLoop;

    public GameWindow() {
        setTitle("Java Smash");
        setSize(1280, 720);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        showCharacterMenu(); 
        setVisible(true);
    }

    private void showCharacterMenu() {
        // Updated to handle the BiConsumer (p1, p2)
        menuPanel = new CharacterSelectPanel((p1Choice, p2Choice) -> {
            showMapMenu(p1Choice, p2Choice); 
        });
        
        getContentPane().removeAll();
        add(menuPanel);
        revalidate();
        repaint();
    }

    private void showMapMenu(String p1Choice, String p2Choice) {
        // Carry both choices into the map selection phase
        mapPanel = new MapSelectPanel(chosenMap -> {
            startGame(p1Choice, p2Choice, chosenMap); 
        });

        getContentPane().removeAll();
        add(mapPanel);
        revalidate();
        repaint();
    }

    private void startGame(String p1Choice, String p2Choice, MapData chosenMap) {
        state = new Gamestate();
        
        // Ensure your Gamestate.setupSession is updated to handle (String p1, String p2, MapData map)
        state.setupSession(p1Choice, p2Choice, chosenMap); 

        gamePanel = new GamePanel(state);
        gamePanel.setFocusable(true); 
        gamePanel.addKeyListener(this);

        getContentPane().removeAll();
        add(gamePanel);

        revalidate();
        repaint();

        gamePanel.requestFocusInWindow();

        if (gameLoop != null) gameLoop.stop();
        gameLoop = new Timer(16, e -> {
            if (state != null) {
                state.update();
                gamePanel.repaint();
            }
        });
        gameLoop.start();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (state != null && code >= 0 && code < state.keys.length) {
            state.keys[code] = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (state != null && code >= 0 && code < state.keys.length) {
            state.keys[code] = false;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GameWindow());
    }
}