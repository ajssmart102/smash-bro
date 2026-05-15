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
        // Updated to handle the BiConsumer (p1, p2)
        menuPanel = new CharacterSelectPanel((p1Choice, p2Choice) -> 
        {
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
        gameLoop = new Timer(16, e -> 
        {
            if (state != null) 
            {
                state.update();
                gamePanel.repaint();

                // --- UPDATED GAME OVER & WINNER CHECK ---
                Fighter loser = state.getLoser();
                if (loser != null) 
                {
                    gameLoop.stop(); // Stop the game engine ticking
                    
                    // Identify the winner based on who lost
                    String winnerName = "Player 1";
                    if (state.fighters.indexOf(loser) == 0) {
                        winnerName = "Player 2"; // Player 1 is out, Player 2 wins!
                    }
                    
                    showGameOverMenu(winnerName); // Switch screens and display the winner
                }
            }
        });
        gameLoop.start();
    }

    // --- UPDATED METHOD TO SHOW THE WINNING PLAYER ---
    private void showGameOverMenu(String winner) 
    {
        JPanel gameOverPanel = new JPanel();
        gameOverPanel.setBackground(Color.BLACK);
        gameOverPanel.setLayout(new BoxLayout(gameOverPanel, BoxLayout.Y_AXIS));

        // Game Over header
        JLabel gameOverLabel = new JLabel("GAME OVER", SwingConstants.CENTER);
        gameOverLabel.setForeground(Color.RED);
        gameOverLabel.setFont(new Font("Arial", Font.BOLD, 60));
        gameOverLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Winner display text
        JLabel winnerLabel = new JLabel(winner + " WINS!", SwingConstants.CENTER);
        // Set text color to match player UI slot styling
        if (winner.equals("Player 1")) {
            winnerLabel.setForeground(Color.BLUE);
        } else {
            winnerLabel.setForeground(Color.RED);
        }
        winnerLabel.setFont(new Font("Arial", Font.BOLD, 36));
        winnerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Reset menu button
        JButton restartButton = new JButton("Return to Character Select");
        restartButton.setFont(new Font("Arial", Font.PLAIN, 20));
        restartButton.setFocusPainted(false);
        restartButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        restartButton.addActionListener(e -> showCharacterMenu());

        // UI Component Spacing Layout
        gameOverPanel.add(Box.createVerticalGlue());
        gameOverPanel.add(gameOverLabel);
        gameOverPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        gameOverPanel.add(winnerLabel);
        gameOverPanel.add(Box.createRigidArea(new Dimension(0, 35)));
        gameOverPanel.add(restartButton);
        gameOverPanel.add(Box.createVerticalGlue());

        // Clean frame canvas layout swaps
        getContentPane().removeAll();
        add(gameOverPanel);
        revalidate();
        repaint();
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