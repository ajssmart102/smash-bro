import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

// ─── Platform ────────────────────────────────────────────────────────────────
class Platform {
    public int x, y, width, height;
    public boolean isSoft;
    public boolean isHard;

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
        float alpha = (float) life / maxLife;
        int size = (int)(40 * (1 - alpha)) + 10;
        g.setColor(new Color(1f, 0.8f, 0f, alpha));
        g.setStroke(new BasicStroke(3));
        g.drawOval(x - size / 2, y - size / 2, size, size);

        // Star lines
        for (int i = 0; i < 6; i++) {
            double angle = Math.toRadians(i * 60 + (1 - alpha) * 120);
            int len = (int)(size * 0.6f);
            g.drawLine(x, y,
                    x + (int)(Math.cos(angle) * len),
                    y + (int)(Math.sin(angle) * len));
        }
    }
}

// ─── InputHandler ────────────────────────────────────────────────────────────
class InputHandler extends KeyAdapter {
    private Gamestate state;

    public InputHandler(Gamestate state) {
        this.state = state;
    }

    @Override public void keyPressed(KeyEvent e)  { state.keys[e.getKeyCode()] = true;  }
    @Override public void keyReleased(KeyEvent e) { state.keys[e.getKeyCode()] = false; }
}

// ─── GamePanel ───────────────────────────────────────────────────────────────
class GamePanel extends JPanel {
    private Gamestate state;
    private static final Color BG_TOP    = new Color(15, 12, 35);
    private static final Color BG_BOTTOM = new Color(25, 20, 55);

    public GamePanel(Gamestate state) {
        this.state = state;
        setPreferredSize(new Dimension(1280, 720));
        setBackground(BG_TOP);
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background gradient
        GradientPaint bg = new GradientPaint(0, 0, BG_TOP, 0, getHeight(), BG_BOTTOM);
        g.setPaint(bg);
        g.fillRect(0, 0, getWidth(), getHeight());

        // Background grid
        g.setColor(new Color(255, 255, 255, 10));
        g.setStroke(new BasicStroke(1));
        for (int gx = 0; gx < getWidth(); gx += 80)  g.drawLine(gx, 0, gx, getHeight());
        for (int gy = 0; gy < getHeight(); gy += 80)  g.drawLine(0, gy, getWidth(), gy);

        // Platforms
        for (Platform p : state.platforms) p.draw(g);

        // Fighters
        for (Fighter f : state.fighters) f.draw(g);

        // Effects
        for (HitEffect e : state.effects) e.draw(g);

        // HUD
        drawHUD(g);
    }

    private void drawHUD(Graphics2D g) {
        int panelW = 220, panelH = 80;
        int[] panelX = {30, getWidth() - panelW - 30};

        for (int i = 0; i < state.fighters.size(); i++) {
            Fighter f = state.fighters.get(i);

            // Panel background
            g.setColor(new Color(0, 0, 0, 160));
            g.fillRoundRect(panelX[i], getHeight() - panelH - 20, panelW, panelH, 16, 16);

            // Color strip
            g.setColor(f.color);
            g.fillRoundRect(panelX[i], getHeight() - panelH - 20, 6, panelH, 6, 6);

            // Name
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 16));
            g.drawString(f.name, panelX[i] + 18, getHeight() - panelH + 5);

            // Damage %
            Color dmgColor = f.damage > 100 ? Color.RED : f.damage > 50 ? Color.ORANGE : Color.WHITE;
            g.setColor(dmgColor);
            g.setFont(new Font("Arial", Font.BOLD, 34));
            g.drawString(String.format("%.0f%%", f.damage), panelX[i] + 18, getHeight() - 35);

            // Stocks (small circles)
            for (int s = 0; s < f.stocks; s++) {
                g.setColor(f.color);
                g.fillOval(panelX[i] + 130 + s * 22, getHeight() - 55, 16, 16);
            }
        }
    }
}
