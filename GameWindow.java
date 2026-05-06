import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class GameWindow extends JFrame implements KeyListener
{
    private Gamestate state;
    private GamePanel gamePanel;
    private CharacterSelectPanel menuPanel;
    private MapSelectPanel mapPanel;
    private Timer gameLoop;

    public GameWindow() 
    {
        setTitle("Java Smash");
        setSize(1280, 720);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        showCharacterMenu(); 
        setVisible(true);
    }

    private void showCharacterMenu() 
    {
        menuPanel = new CharacterSelectPanel(characterChoice -> 
        {
            showMapMenu(characterChoice); 
        });
        
        getContentPane().removeAll();
        add(menuPanel);
        revalidate();
        repaint();
    }

    private void showMapMenu(String characterChoice) 
    {
        mapPanel = new MapSelectPanel(chosenMap -> 
        {
            startGame(characterChoice, chosenMap); 
        });

        getContentPane().removeAll();
        add(mapPanel);
        revalidate();
        repaint();
    }

    private void startGame(String characterChoice, MapData chosenMap) {
        state = new Gamestate();
        
        // Match the 2-parameter signature in Gamestate
        state.setupSession(characterChoice, chosenMap); 

        gamePanel = new GamePanel(state);
        gamePanel.setFocusable(true); 
        gamePanel.addKeyListener(this);

        getContentPane().removeAll();
        add(gamePanel);

        revalidate();
        repaint();

        gamePanel.requestFocusInWindow();

        if (gameLoop != null) gameLoop.stop();
        gameLoop = new Timer(16, e -> 
        {
            if (state != null) 
            {
                state.update();
                gamePanel.repaint();
            }
        });
        gameLoop.start();
    }

    @Override
    public void keyPressed(KeyEvent e)
    {
        int code = e.getKeyCode();
        if (state != null && code >= 0 && code < state.keys.length) 
        {
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