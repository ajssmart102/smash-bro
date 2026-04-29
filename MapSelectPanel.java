import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;

public class MapSelectPanel extends JPanel {
    private MapData previewMap = null; 

    public MapSelectPanel(Consumer<MapData> onMapSelected) {
        setLayout(new BorderLayout());
        setBackground(Color.BLACK); 

        // --- TITLE ---
        JLabel title = new JLabel("SELECT MAP", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 48));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        // --- BUTTON ROW (Short and Horizontal) ---
        List<MapData> maps = MapData.getAllMaps();
        // 1 row, columns match map count, 5px gap between boxes
        JPanel buttonRow = new JPanel(new GridLayout(1, maps.size(), 5, 0));
        buttonRow.setOpaque(false);

        for (MapData map : maps) {
            JButton btn = new JButton(map.name);
            btn.setFont(new Font("Arial", Font.BOLD, 18));
            btn.setFocusPainted(false);
            
            // Light blue-white styling
            btn.setBackground(new Color(225, 240, 255));
            btn.setForeground(new Color(30, 30, 50));
            btn.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
            
            btn.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    previewMap = map;
                    btn.setBackground(Color.WHITE); 
                    repaint(); 
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    previewMap = null;
                    btn.setBackground(new Color(225, 240, 255));
                    repaint(); 
                }
            });

            btn.addActionListener(e -> onMapSelected.accept(map));
            buttonRow.add(btn);
        }

        // --- THE FIX: TOP CONTAINER ---
        // This panel holds the title and the buttons at the top of the screen
        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setOpaque(false);
        topContainer.add(title, BorderLayout.NORTH);
        
        // Give the button row a specific height (e.g., 100 pixels)
        buttonRow.setPreferredSize(new Dimension(0, 100)); 
        topContainer.add(buttonRow, BorderLayout.CENTER);

        // Add the topContainer to the NORTH so it doesn't stretch vertically
        add(topContainer, BorderLayout.NORTH);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // The area below the buttons is now empty for the map preview
        if (previewMap != null) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Draw platforms in their actual locations in the big empty space
            for (Platform p : previewMap.platforms) {
                g2.setColor(new Color(100, 100, 255, 150)); 
                g2.fillRect((int)p.x, (int)p.y, (int)p.width, (int)p.height);
                
                g2.setColor(Color.CYAN);
                g2.drawRect((int)p.x, (int)p.y, (int)p.width, (int)p.height);
            }

            g2.setFont(new Font("Arial", Font.ITALIC, 60));
            g2.setColor(new Color(255, 255, 255, 20));
            g2.drawString(previewMap.name, 50, getHeight() - 50);
        }
    }
}