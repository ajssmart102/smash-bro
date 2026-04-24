import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

public class MapSelectPanel extends JPanel {
    public MapSelectPanel(Consumer<MapData> onMapSelected) {
        setLayout(new BorderLayout());
        setBackground(new Color(20, 20, 20));

        JLabel title = new JLabel("SELECT MAP", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 40));
        title.setForeground(Color.WHITE);
        add(title, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        
        // Get maps from your MapData file
        List<MapData> maps = MapData.getAllMaps();

        for (MapData map : maps) {
            JButton btn = new JButton(map.name);
            btn.setPreferredSize(new Dimension(200, 100));
            btn.setFont(new Font("Arial", Font.BOLD, 20));
            btn.addActionListener(e -> onMapSelected.accept(map));
            buttonPanel.add(btn);
        }

        add(buttonPanel, BorderLayout.CENTER);
    }
}