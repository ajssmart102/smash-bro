import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;

public class MapSelectPanel extends JPanel {
    private MapData previewMap = null;
    private final JPanel previewArea;

    public MapSelectPanel(Consumer<MapData> onMapSelected) {
        // Use a 1280x720 base, but the layout will handle resizing
        setLayout(new BorderLayout());
        setBackground(new Color(20, 20, 25));

        // --- LEFT SIDE: SCROLLABLE LIST ---
        JPanel listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setBackground(new Color(35, 35, 45));

        List<MapData> maps = MapData.getAllMaps();
        for (MapData map : maps) {
            JButton btn = new JButton(map.name);
            btn.setMaximumSize(new Dimension(300, 50));
            btn.setFont(new Font("Verdana", Font.BOLD, 16));
            btn.setFocusPainted(false);
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            // Modern Styling
            btn.setBackground(new Color(60, 60, 75));
            btn.setForeground(Color.WHITE);
            btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(20, 20, 25), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));

            btn.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    previewMap = map;
                    btn.setBackground(new Color(100, 100, 255));
                    repaint();
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    btn.setBackground(new Color(60, 60, 75));
                }
            });

            btn.addActionListener(e -> onMapSelected.accept(map));
            listContainer.add(btn);
            listContainer.add(Box.createRigidArea(new Dimension(0, 5))); // Spacer
        }

        JScrollPane scrollPane = new JScrollPane(listContainer);
        scrollPane.setPreferredSize(new Dimension(300, 0));
        scrollPane.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 2, Color.BLACK));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.WEST);

        // --- RIGHT SIDE: PREVIEW AREA ---
        previewArea = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawMapPreview((Graphics2D) g);
            }
        };
        previewArea.setBackground(new Color(10, 10, 15));
        add(previewArea, BorderLayout.CENTER);

        // --- TITLE ---
        JLabel title = new JLabel("CHOOSE YOUR ARENA", SwingConstants.CENTER);
        title.setFont(new Font("Impact", Font.PLAIN, 44));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        add(title, BorderLayout.NORTH);
    }

    private void drawMapPreview(Graphics2D g2) {
        if (previewMap == null) {
            g2.setColor(Color.GRAY);
            g2.setFont(new Font("Arial", Font.PLAIN, 20));
            g2.drawString("Hover over a map to preview", previewArea.getWidth()/2 - 120, previewArea.getHeight()/2);
            return;
        }

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Calculate Scaling
        // The game internal resolution is roughly 1280x720. 
        // We scale the preview to fit the current window size.
        double scaleX = (double) previewArea.getWidth() / 1280.0;
        double scaleY = (double) previewArea.getHeight() / 720.0;
        double scale = Math.min(scaleX, scaleY) * 0.8; // 80% size for padding

        int offsetX = (int) (previewArea.getWidth() - (1280 * scale)) / 2;
        int offsetY = (int) (previewArea.getHeight() - (720 * scale)) / 2;

        // 2. Draw Background Box
        g2.setColor(previewMap.backgroundColor);
        g2.fillRect(offsetX, offsetY, (int)(1280 * scale), (int)(720 * scale));
        g2.setColor(Color.WHITE);
        g2.drawRect(offsetX, offsetY, (int)(1280 * scale), (int)(720 * scale));

        // 3. Draw Platforms (Scaled)
        for (Platform p : previewMap.platforms) {
            int px = offsetX + (int) (p.x * scale);
            int py = offsetY + (int) (p.y * scale);
            int pw = (int) (p.width * scale);
            int ph = (int) (p.height * scale);

            // Platform Shadow
            g2.setColor(new Color(0, 0, 0, 100));
            g2.fillRect(px + 3, py + 3, pw, ph);

            // Platform Body
            g2.setColor(new Color(100, 100, 255));
            g2.fillRect(px, py, pw, ph);
            g2.setColor(Color.CYAN);
            g2.drawRect(px, py, pw, ph);
        }

        // 4. Map Name Overlay
        g2.setFont(new Font("Impact", Font.PLAIN, 50));
        g2.setColor(new Color(255, 255, 255, 40));
        g2.drawString(previewMap.name.toUpperCase(), offsetX + 20, offsetY + (int)(720 * scale) - 20);
    }
}