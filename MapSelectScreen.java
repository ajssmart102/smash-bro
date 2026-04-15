import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
 
public class MapSelectScreen extends JPanel implements MouseListener, MouseMotionListener {
 
    // -------------------------------------------------------
    // Map data
    // -------------------------------------------------------
    private static final String[] MAP_NAMES = {
        "Final Destination",
        "Battlefield",
        "Lava Ruins",
        "Sky Temple",
        "Neon City",
        "Frozen Peak"
    };
 
    private static final String[] MAP_DESCRIPTIONS = {
        "Flat platform, no distractions",
        "Three floating platforms above main stage",
        "Shifting platforms over molten rock",
        "High altitude arena among the clouds",
        "Urban rooftop with electric hazards",
        "Icy slopes with slippery edges"
    };
 
    private static final Color[] MAP_COLORS = {
        new Color(233, 69, 96),
        new Color(106, 159, 216),
        new Color(255, 90, 0),
        new Color(126, 200, 227),
        new Color(188, 19, 254),
        new Color(168, 216, 234)
    };
 
    private static final Color[] MAP_BG_COLORS = {
        new Color(26, 26, 46),
        new Color(15, 52, 96),
        new Color(45, 0, 0),
        new Color(13, 27, 42),
        new Color(13, 13, 26),
        new Color(10, 22, 40)
    };
 
    // -------------------------------------------------------
    // Layout constants
    // -------------------------------------------------------
    private static final int COLS        = 3;
    private static final int CARD_W      = 220;
    private static final int CARD_H      = 140;
    private static final int CARD_GAP    = 20;
    private static final int GRID_TOP    = 160;  // y offset where grid starts
 
    // -------------------------------------------------------
    // State
    // -------------------------------------------------------
    private int selectedIndex  = -1;   // -1 = nothing chosen yet
    private int hoveredIndex   = -1;
 
    // Confirm button bounds (computed in paintComponent)
    private Rectangle confirmBtn = new Rectangle();
 
    // Callback interface — implement this in GameWindow or SmashGame
    // to know when the player picked a map.
    public interface MapSelectListener {
        void onMapSelected(int mapIndex, String mapName);
    }
 
    private MapSelectListener listener;
 
    // -------------------------------------------------------
    // Constructor
    // -------------------------------------------------------
    public MapSelectScreen(MapSelectListener listener) {
        this.listener = listener;
        setBackground(new Color(8, 8, 16));
        addMouseListener(this);
        addMouseMotionListener(this);
    }
 
    // -------------------------------------------------------
    // Rendering
    // -------------------------------------------------------
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
 
        int w = getWidth();
        int h = getHeight();
 
        // Background
        g2.setColor(new Color(8, 8, 16));
        g2.fillRect(0, 0, w, h);
 
        drawHeader(g2, w);
        drawCards(g2, w);
        drawConfirmButton(g2, w, h);
    }
 
    private void drawHeader(Graphics2D g2, int w) {
        // Subtitle
        g2.setFont(new Font("Courier New", Font.PLAIN, 11));
        g2.setColor(new Color(255, 90, 0));
        String sub = "— SELECT STAGE —";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(sub, (w - fm.stringWidth(sub)) / 2, 60);
 
        // Title
        g2.setFont(new Font("Courier New", Font.BOLD, 42));
        g2.setColor(Color.WHITE);
        String title = "CHOOSE YOUR ARENA";
        fm = g2.getFontMetrics();
        g2.drawString(title, (w - fm.stringWidth(title)) / 2, 115);
 
        // Divider
        g2.setColor(new Color(255, 90, 0, 120));
        g2.setStroke(new BasicStroke(1f));
        int divY = 130;
        g2.drawLine(w / 4, divY, 3 * w / 4, divY);
    }
 
    private void drawCards(Graphics2D g2, int w) {
        int rows = (int) Math.ceil((double) MAP_NAMES.length / COLS);
        int gridW = COLS * CARD_W + (COLS - 1) * CARD_GAP;
        int startX = (w - gridW) / 2;
 
        for (int i = 0; i < MAP_NAMES.length; i++) {
            int col = i % COLS;
            int row = i / COLS;
            int x = startX + col * (CARD_W + CARD_GAP);
            int y = GRID_TOP + row * (CARD_H + CARD_GAP);
 
            drawCard(g2, i, x, y);
        }
    }
 
    private void drawCard(Graphics2D g2, int index, int x, int y) {
        boolean selected = (index == selectedIndex);
        boolean hovered  = (index == hoveredIndex);
 
        Color accent = MAP_COLORS[index];
        Color bg     = selected ? MAP_BG_COLORS[index] : new Color(255, 255, 255, 8);
 
        // Card background
        g2.setColor(bg);
        g2.fillRoundRect(x, y, CARD_W, CARD_H, 14, 14);
 
        // Border
        float borderW = selected ? 2.5f : 1f;
        Color borderColor = selected ? accent
                          : hovered  ? new Color(255, 255, 255, 80)
                                     : new Color(255, 255, 255, 30);
        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(borderW));
        g2.drawRoundRect(x, y, CARD_W, CARD_H, 14, 14);
        g2.setStroke(new BasicStroke(1f));
 
        // Accent bar at top when selected
        if (selected) {
            g2.setColor(accent);
            g2.fillRoundRect(x + 1, y + 1, CARD_W - 2, 4, 4, 4);
        }
 
        // Map name
        g2.setFont(new Font("Courier New", Font.BOLD, 15));
        g2.setColor(selected ? accent : Color.WHITE);
        g2.drawString(MAP_NAMES[index], x + 16, y + 38);
 
        // Description
        g2.setFont(new Font("Courier New", Font.PLAIN, 11));
        g2.setColor(new Color(180, 180, 180));
        drawWrappedString(g2, MAP_DESCRIPTIONS[index], x + 16, y + 60, CARD_W - 32);
 
        // SELECTED badge
        if (selected) {
            String badge = "SELECTED";
            g2.setFont(new Font("Courier New", Font.BOLD, 9));
            FontMetrics fm = g2.getFontMetrics();
            int bw = fm.stringWidth(badge) + 12;
            int bh = 16;
            int bx = x + CARD_W - bw - 10;
            int by = y + 10;
            g2.setColor(accent);
            g2.fillRoundRect(bx, by, bw, bh, 6, 6);
            g2.setColor(new Color(8, 8, 16));
            g2.drawString(badge, bx + 6, by + bh - 4);
        }
    }
 
    private void drawConfirmButton(Graphics2D g2, int w, int h) {
        int rows = (int) Math.ceil((double) MAP_NAMES.length / COLS);
        int gridBottom = GRID_TOP + rows * (CARD_H + CARD_GAP) - CARD_GAP;
 
        int btnW = 220;
        int btnH = 48;
        int btnX = (w - btnW) / 2;
        int btnY = gridBottom + 30;
 
        confirmBtn.setBounds(btnX, btnY, btnW, btnH);
 
        boolean canConfirm = selectedIndex >= 0;
        Color btnColor = canConfirm
            ? MAP_COLORS[selectedIndex]
            : new Color(60, 60, 70);
 
        g2.setColor(btnColor);
        g2.fillRoundRect(btnX, btnY, btnW, btnH, 10, 10);
 
        g2.setFont(new Font("Courier New", Font.BOLD, 16));
        String label = canConfirm ? "CONFIRM  →" : "SELECT A STAGE";
        FontMetrics fm = g2.getFontMetrics();
        g2.setColor(canConfirm ? new Color(8, 8, 16) : new Color(100, 100, 110));
        g2.drawString(label, btnX + (btnW - fm.stringWidth(label)) / 2, btnY + 31);
    }
 
    // Wraps long description text inside a card
    private void drawWrappedString(Graphics2D g2, String text, int x, int y, int maxW) {
        FontMetrics fm = g2.getFontMetrics();
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        int lineY = y;
 
        for (String word : words) {
            String test = line.length() == 0 ? word : line + " " + word;
            if (fm.stringWidth(test) > maxW) {
                g2.drawString(line.toString(), x, lineY);
                line = new StringBuilder(word);
                lineY += fm.getHeight() + 2;
            } else {
                line = new StringBuilder(test);
            }
        }
        if (line.length() > 0) {
            g2.drawString(line.toString(), x, lineY);
        }
    }
 
    // -------------------------------------------------------
    // Mouse events
    // -------------------------------------------------------
    @Override
    public void mouseClicked(MouseEvent e) {
        int index = getCardIndexAt(e.getX(), e.getY());
        if (index >= 0) {
            selectedIndex = index;
            repaint();
            return;
        }
 
        // Confirm button
        if (confirmBtn.contains(e.getPoint()) && selectedIndex >= 0) {
            if (listener != null) {
                listener.onMapSelected(selectedIndex, MAP_NAMES[selectedIndex]);
            }
        }
    }
 
    @Override
    public void mouseMoved(MouseEvent e) {
        int prev = hoveredIndex;
        hoveredIndex = getCardIndexAt(e.getX(), e.getY());
        if (hoveredIndex != prev) repaint();
 
        // Change cursor over confirm button
        if (confirmBtn.contains(e.getPoint()) && selectedIndex >= 0) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else if (hoveredIndex >= 0) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else {
            setCursor(Cursor.getDefaultCursor());
        }
    }
 
    // Returns which card index the point falls in, or -1
    private int getCardIndexAt(int px, int py) {
        int gridW = COLS * CARD_W + (COLS - 1) * CARD_GAP;
        int startX = (getWidth() - gridW) / 2;
 
        for (int i = 0; i < MAP_NAMES.length; i++) {
            int col = i % COLS;
            int row = i / COLS;
            int x = startX + col * (CARD_W + CARD_GAP);
            int y = GRID_TOP + row * (CARD_H + CARD_GAP);
            if (px >= x && px <= x + CARD_W && py >= y && py <= y + CARD_H) {
                return i;
            }
        }
        return -1;
    }
 
    // Unused mouse events
    @Override public void mousePressed(MouseEvent e)  {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e)  {}
    @Override public void mouseExited(MouseEvent e)   {}
    @Override public void mouseDragged(MouseEvent e)  {}
}
