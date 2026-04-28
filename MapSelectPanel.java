import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;

public class MapSelectPanel extends JPanel {
    private MapData previewMap = null; // The map currently being hovered over

    public MapSelectPanel(Consumer<MapData> onMapSelected) {
        setLayout(new BorderLayout());
        setBackground(new Color(20, 20, 20));

        JLabel title = new JLabel("SELECT MAP", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 40));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(title, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        
        List<MapData> maps = MapData.getAllMaps();

        for (MapData map : maps) {
            JButton btn = new JButton(map.name);
            btn.setPreferredSize(new Dimension(200, 100));
            btn.setFont(new Font("Arial", Font.BOLD, 20));
            btn.setFocusPainted(false);
            
            // Action when clicked
            btn.addActionListener(e -> onMapSelected.accept(map));

            // NEW: Preview logic on hover
            btn.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    previewMap = map;
                    repaint(); // Tells the panel to draw the preview
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    previewMap = null;
                    repaint(); // Clears the preview
                }
            });

            buttonPanel.add(btn);
        }

        add(buttonPanel, BorderLayout.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // If a map is being hovered, draw its layout in the background
        if (previewMap != null) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Draw a subtle "ghost" background for the stage area
            g2.setColor(new Color(255, 255, 255, 10)); // Very faint white
            g2.fillRect(100, 100, getWidth() - 200, getHeight() - 200);

            // Draw the platforms of the hovered map
            for (Platform p : previewMap.platforms) {
                // We draw them slightly transparent so the buttons stay visible
                g2.setColor(new Color(100, 100, 255, 150)); 
                g2.fill(new Rectangle((int)p.x, (int)p.y, (int)p.width, (int)p.height));
                
                // Add a little highlight edge
                g2.setColor(Color.CYAN);
                g2.draw(new Rectangle((int)p.x, (int)p.y, (int)p.width, (int)p.height));
            }

            // Draw the name of the hovered map in the background
            g2.setFont(new Font("Arial", Font.ITALIC, 60));
            g2.setColor(new Color(255, 255, 255, 30));
            g2.drawString(previewMap.name, 50, getHeight() - 50);
        }
    }
}