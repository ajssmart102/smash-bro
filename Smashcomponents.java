import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.ArrayList;

// ─── Platform ────────────────────────────────────────────────────────────────
class Platform {
    public int x, y, width, height;

    public Platform(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void draw(Graphics2D g) {
        g.setColor(new Color(60, 60, 80));
        g.fillRoundRect(x, y, width, height, 8, 8);
        g.setColor(new Color(100, 100, 140));
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(x, y, width, height, 8, 8);
    }
}

// ─── HitEffect ───────────────────────────────────────────────────────────────
class HitEffect {
    private int x, y;
    private int life = 20;
    private int maxLife = 20;

    public HitEffect(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void update() { life--; }
    public boolean isAlive() { return life > 0; }

    public void draw(Graphics2D g) {
        float alpha = Math.max(0, Math.min(1, (float) life / maxLife));
        int size = (int)(40 * (1 - alpha)) + 10;
        
        // Safety check for alpha values
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g.setColor(new Color(1f, 0.8f, 0f)); 
        
        g.setStroke(new BasicStroke(3));
        g.drawOval(x - size / 2, y - size / 2, size, size);

        for (int i = 0; i < 6; i++) {
            double angle = Math.toRadians(i * 60 + (1 - alpha) * 120);
            int len = (int)(size * 0.6f);
            g.drawLine(x, y,
                    x + (int)(Math.cos(angle) * len),
                    y + (int)(Math.sin(angle) * len));
        }
        // Reset composite so HUD isn't transparent
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }
}

// ─── InputHandler ────────────────────────────────────────────────────────────
class InputHandler extends KeyAdapter {
    private Gamestate state;

    public InputHandler(Gamestate state) {
        this.state = state;
    }

    @Override 
    public void keyPressed(KeyEvent e) { 
        if (e.getKeyCode() < state.keys.length)
            state.keys[e.getKeyCode()] = true;  
    }
    
    @Override 
    public void keyReleased(KeyEvent e) { 
        if (e.getKeyCode() < state.keys.length)
            state.keys[e.getKeyCode()] = false; 
    }
}

// ─── GamePanel ───────────────────────────────────────────────────────────────
class GamePanel extends JPanel {
    private Gamestate state;
    private static final Color BG_TOP    = new Color(15, 12, 35);
    private static final Color BG_BOTTOM = new Color(25, 20, 55);

    public GamePanel(Gamestate state) {
        this.state = state;
        setPreferredSize(new Dimension(1280, 720));
        setDoubleBuffered(true); // Prevents flickering
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        if (state == null) return; // Safety check

        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Background
        GradientPaint bg = new GradientPaint(0, 0, BG_TOP, 0, getHeight(), BG_BOTTOM);
        g.setPaint(bg);
        g.fillRect(0, 0, getWidth(), getHeight());

        // 2. Grid
        g.setColor(new Color(255, 255, 255, 15));
        for (int gx = 0; gx < getWidth(); gx += 80)  g.drawLine(gx, 0, gx, getHeight());
        for (int gy = 0; gy < getHeight(); gy += 80)  g.drawLine(0, gy, getWidth(), gy);

        // 3. Platforms
        if (state.platforms != null) {
            for (Platform p : state.platforms) p.draw(g);
        }

        // 4. Fighters (Critical Fix: Check if null or empty)
        if (state.fighters != null && !state.fighters.isEmpty()) {
            // Using a standard for loop to avoid ConcurrentModificationException
            for (int i = 0; i < state.fighters.size(); i++) {
                state.fighters.get(i).draw(g);
            }
        }

        // 5. Effects
        if (state.effects != null) {
            for (int i = 0; i < state.effects.size(); i++) {
                state.effects.get(i).draw(g);
            }
        }

        // 6. HUD
        drawHUD(g);
    }

    private void drawHUD(Graphics2D g) {
        if (state.fighters == null || state.fighters.isEmpty()) return;

        int panelW = 220, panelH = 100;
        int[] panelX = {30, getWidth() - panelW - 30};

        for (int i = 0; i < state.fighters.size(); i++) {
            if (i >= 2) break; // Limit HUD to 2 players for this layout
            Fighter f = state.fighters.get(i);

            int yPos = getHeight() - panelH - 30;

            // Panel shadow/bg
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRoundRect(panelX[i], yPos, panelW, panelH, 20, 20);

            // Border based on character color
            g.setColor(f.color);
            g.setStroke(new BasicStroke(3));
            g.drawRoundRect(panelX[i], yPos, panelW, panelH, 20, 20);

            // Name
            g.setColor(Color.WHITE);
            g.setFont(new Font("Verdana", Font.BOLD, 18));
            g.drawString(f.name.toUpperCase(), panelX[i] + 15, yPos + 25);

            // Damage %
            float dmg = f.damage;
            Color dmgColor = Color.WHITE;
            if (dmg > 150) dmgColor = new Color(255, 0, 0);
            else if (dmg > 100) dmgColor = new Color(255, 100, 0);
            else if (dmg > 50) dmgColor = new Color(255, 200, 0);
            
            g.setColor(dmgColor);
            g.setFont(new Font("Arial", Font.BOLD, 42));
            g.drawString(String.format("%.0f%%", dmg), panelX[i] + 15, yPos + 75);

            // Stocks
            for (int s = 0; s < f.stocks; s++) {
                g.setColor(f.color);
                g.fillOval(panelX[i] + 140 + (s * 20), yPos + 15, 14, 14);
            }
        }
    }
}
