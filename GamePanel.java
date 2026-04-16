import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class GamePanel extends JPanel {
    private Gamestate state;
    
    // Aesthetic Constants
    private static final Color BG_TOP = new Color(20, 20, 45);
    private static final Color BG_BOTTOM = new Color(40, 40, 80);

    public GamePanel(Gamestate state) {
        this.state = state;
        
        // This ensures the panel fills the window correctly
        setPreferredSize(new Dimension(1280, 720));
        
        // Double buffering prevents the screen from flickering during movement
        setDoubleBuffered(true);
        
        // Focusable allows the panel to be the target of keyboard inputs
        setFocusable(true);
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;

        // Turn on Anti-Aliasing for smooth shapes
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. DRAW BACKGROUND
        drawBackground(g);

        // 2. DRAW PLATFORMS
        if (state.platforms != null) {
            for (Platform p : state.platforms) {
                p.draw(g);
            }
        }

        // 3. DRAW FIGHTERS
        if (state.fighters != null) {
            for (Fighter f : state.fighters) {
                f.draw(g);
            }
        }

        // 4. DRAW EFFECTS (Hit sparks, etc.)
        if (state.effects != null) {
            // Use a standard for-loop to avoid ConcurrentModificationException 
            // if effects are added while we are drawing them.
            for (int i = 0; i < state.effects.size(); i++) {
                state.effects.get(i).draw(g);
            }
        }

        // 5. DRAW HUD (Damage percentages and Stocks)
        drawHUD(g);
    }

    private void drawBackground(Graphics2D g) {
        // Creates a nice dark sky gradient
        GradientPaint gradient = new GradientPaint(0, 0, BG_TOP, 0, getHeight(), BG_BOTTOM);
        g.setPaint(gradient);
        g.fillRect(0, 0, getWidth(), getHeight());

        // Optional: Draw a subtle grid for a "Tech" look
        g.setColor(new Color(255, 255, 255, 10));
        for (int i = 0; i < getWidth(); i += 50) g.drawLine(i, 0, i, getHeight());
        for (int i = 0; i < getHeight(); i += 50) g.drawLine(0, i, getWidth(), i);
    }

    private void drawHUD(Graphics2D g) {
        if (state.fighters == null) return;

        g.setFont(new Font("Arial", Font.BOLD, 30));
        
        for (int i = 0; i < state.fighters.size(); i++) {
            Fighter f = state.fighters.get(i);
            
            // Position HUD: Player 1 on left, Player 2 on right
            int xPos = (i == 0) ? 100 : getWidth() - 300;
            int yPos = getHeight() - 80;

            // Draw Name
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 18));
            g.drawString(f.name, xPos, yPos - 40);

            // Draw Damage % (Changes color as it gets higher)
            Color dmgColor = Color.WHITE;
            if (f.damage > 150) dmgColor = Color.RED;
            else if (f.damage > 75) dmgColor = Color.ORANGE;
            
            g.setColor(dmgColor);
            g.setFont(new Font("Arial", Font.BOLD, 45));
            g.drawString(String.format("%.0f%%", f.damage), xPos, yPos);

            // Draw Stocks (small circles)
            g.setColor(f.color);
            for (int s = 0; s < f.stocks; s++) {
                g.fillOval(xPos + (s * 25), yPos + 15, 15, 15);
            }
        }
    }
}
