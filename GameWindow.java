import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GameWindow extends JFrame {
    private Gamestate state = new Gamestate();

    public GameWindow() {
        state.setupSession("Tank"); // Hardcoded for now
        
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D)g;
                for (Platform p : state.platforms) p.draw(g2);
                for (Fighter f : state.fighters) f.draw(g2);
                for (HitEffect e : state.effects) e.draw(g2);
            }
        };

        addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) { if(e.getKeyCode()<256) state.keys[e.getKeyCode()] = true; }
            @Override public void keyReleased(KeyEvent e) { if(e.getKeyCode()<256) state.keys[e.getKeyCode()] = false; }
        });

        add(panel);
        setSize(1280, 720);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);

        new Timer(16, e -> {
            state.update();
            panel.repaint();
        }).start();
    }

    public static void main(String[] args) { new GameWindow(); }
}
