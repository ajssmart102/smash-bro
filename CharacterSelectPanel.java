import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class CharacterSelectPanel extends JPanel {
    
    private String hoveredChar = "";
    private String p1Choice = null;
    private String p2Choice = null;
    private JLabel title;

    public CharacterSelectPanel(BiConsumer<String, String> onBothChoicesMade) {
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);

        // --- TITLE ---
        title = new JLabel("PLAYER 1: CHOOSE YOUR FIGHTER", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 48));
        title.setForeground(Color.CYAN);
        title.setBorder(BorderFactory.createEmptyBorder(40, 0, 40, 0));

        // --- CHARACTER BUTTON ROW ---
        JPanel charRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        charRow.setOpaque(false);

        // Load roster
        List<String> roster = loadRosterFromFile("roster.txt");
        System.out.println("Loaded " + roster.size() + " characters."); // DEBUG: Check console

        for (String name : roster) {
            charRow.add(createCharButton(name, onBothChoicesMade));
        }

        add(title, BorderLayout.NORTH);
        add(charRow, BorderLayout.CENTER);
    }

    private List<String> loadRosterFromFile(String filePath) {
        List<String> rosterNames = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                // FIXED: Now splits by comma to match Gamestate format
                String[] parts = line.split(",");
                if (parts.length > 0) {
                    rosterNames.add(parts[0].trim());
                }
            }
        } catch (IOException e) {
            System.err.println("Could not load roster file: " + e.getMessage());
            // Fallback for debugging
            rosterNames.add("Standard");
            rosterNames.add("Tank");
        }
        return rosterNames;
    }

    private JButton createCharButton(String name, BiConsumer<String, String> callback) {
        JButton btn = new JButton(name);
        btn.setPreferredSize(new Dimension(150, 60)); // Set explicit size so they appear
        btn.setFont(new Font("Arial", Font.BOLD, 18));
        btn.setFocusPainted(false);
        btn.setBackground(new Color(225, 240, 255));
        
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { hoveredChar = name; repaint(); }
            public void mouseExited(MouseEvent e) { hoveredChar = ""; repaint(); }
        });

        btn.addActionListener(e -> {
            if (p1Choice == null) {
                p1Choice = name;
                title.setText("PLAYER 2: CHOOSE YOUR FIGHTER");
                title.setForeground(Color.RED);
            } else if (p2Choice == null) {
                p2Choice = name;
                callback.accept(p1Choice, p2Choice);
            }
        });

        return btn;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        
        // Background Text
        if (!hoveredChar.isEmpty()) {
            g2.setColor(new Color(255, 255, 255, 20));
            g2.setFont(new Font("Arial", Font.BOLD, 100));
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(hoveredChar)) / 2;
            g2.drawString(hoveredChar, x, getHeight() / 2);
        }
    }
}