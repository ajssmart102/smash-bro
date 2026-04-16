import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class CharacterSelectScreen extends JPanel implements MouseListener, MouseMotionListener {

    private static final String[] CHAR_NAMES = {"Mario", "Link", "Kirby", "Pikachu"};
    private static final Color[] CHAR_COLORS = {Color.RED, Color.GREEN, Color.PINK, Color.YELLOW};

    private int selectedIndex = -1;
    private int hoveredIndex = -1;
    private Rectangle confirmBtn = new Rectangle();
    
    // The "Bridge" interface
    public interface CharacterSelectListener {
        void onCharacterSelected(String charName);
    }

    private CharacterSelectListener listener;

    public CharacterSelectScreen(CharacterSelectListener listener) {
        this.listener = listener;
        setBackground(new Color(10, 10, 20));
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Title
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Courier New", Font.BOLD, 36));
        String title = "SELECT YOUR FIGHTER";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(title, (w - fm.stringWidth(title)) / 2, 100);

        // Draw Character Slots
        int slotW = 150, slotH = 200, gap = 30;
        int totalW = CHAR_NAMES.length * slotW + (CHAR_NAMES.length - 1) * gap;
        int startX = (w - totalW) / 2;

        for (int i = 0; i < CHAR_NAMES.length; i++) {
            int x = startX + i * (slotW + gap);
            int y = 180;

            // Highlight if hovered or selected
            if (i == selectedIndex) g2.setColor(CHAR_COLORS[i]);
            else if (i == hoveredIndex) g2.setColor(new Color(255, 255, 255, 100));
            else g2.setColor(new Color(255, 255, 255, 20));

            g2.fillRoundRect(x, y, slotW, slotH, 15, 15);
            g2.setColor(Color.WHITE);
            g2.drawRoundRect(x, y, slotW, slotH, 15, 15);
            
            g2.drawString(CHAR_NAMES[i], x + 20, y + slotH - 20);
        }

        // Confirm Button
        confirmBtn.setBounds((w - 200) / 2, h - 100, 200, 50);
        g2.setColor(selectedIndex >= 0 ? Color.WHITE : Color.GRAY);
        g2.fillRoundRect(confirmBtn.x, confirmBtn.y, confirmBtn.width, confirmBtn.height, 10, 10);
        g2.setColor(Color.BLACK);
        g2.drawString("READY!", confirmBtn.x + 70, confirmBtn.y + 32);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // Logic to select character index based on mouse X/Y (similar to your Map code)
        // For brevity, let's assume a simple check or just clicking the button:
        if (confirmBtn.contains(e.getPoint()) && selectedIndex >= 0) {
            listener.onCharacterSelected(CHAR_NAMES[selectedIndex]);
        }
        
        // Simple hitbox detection for character slots
        int slotW = 150, gap = 30;
        int totalW = CHAR_NAMES.length * slotW + (CHAR_NAMES.length - 1) * gap;
        int startX = (getWidth() - totalW) / 2;
        for(int i=0; i < CHAR_NAMES.length; i++) {
            Rectangle rect = new Rectangle(startX + i * (slotW + gap), 180, slotW, 200);
            if(rect.contains(e.getPoint())) {
                selectedIndex = i;
                repaint();
            }
        }
    }

    // Include other MouseListener stubs here...
    @Override public void mouseMoved(MouseEvent e) { /* update hoveredIndex and repaint */ }
    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
    @Override public void mouseDragged(MouseEvent e) {}
}
Step 2: Update Gamewindow.java
You need to modify your logic so that when a map is picked, it doesn't start the game—it shows the character screen instead.

Your logic flow should look like this:

Map Selection: MapSelectScreen triggers onMapSelected.

Transition: Gamewindow removes MapSelectScreen and adds CharacterSelectScreen. It saves the mapIndex for later.

Character Selection: CharacterSelectScreen triggers onCharacterSelected.

Game Start: Gamewindow starts the actual gameplay using both the saved mapIndex and the new charName.

Inside your Gamewindow class, update the listeners:

Java
// Inside your Gamewindow constructor or setup method:

// 1. Set up Map Listener
MapSelectScreen mapScreen = new MapSelectScreen((mapIndex, mapName) -> {
    showCharacterSelection(mapIndex); // Move to next screen, don't start game yet!
});

// 2. The method to switch screens
public void showCharacterSelection(int mapIndex) {
    CharacterSelectScreen charScreen = new CharacterSelectScreen((charName) -> {
        startActualGame(mapIndex, charName); // Finally start the game
    });
    
    this.getContentPane().removeAll();
    this.add(charScreen);
    this.revalidate();
    this.repaint();
}

// 3. The method that starts the actual fight
public void startActualGame(int mapIndex, String characterName) {
    // Pass both variables to your Smashgame components/Fighter
    // Fighter player = new Fighter(characterName);
    // ... setup the map based on mapIndex ...
}
