import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.BiConsumer;

public class CharacterSelectPanel extends JPanel {
    
    private String hoveredChar = "";
    private String p1Choice = null;
    private String p2Choice = null;
    private JLabel title;

    // We change Consumer<String> to BiConsumer<String, String> to send both choices back
    public CharacterSelectPanel(BiConsumer<String, String> onBothChoicesMade) {
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);

        // --- TITLE ---
        title = new JLabel("PLAYER 1: CHOOSE YOUR FIGHTER", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 48));
        title.setForeground(Color.CYAN); // P1 is Cyan
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        // --- CHARACTER BUTTON ROW ---
        JPanel charRow = new JPanel(new GridLayout(1, 0, 5, 0));
        charRow.setOpaque(false);
        charRow.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        String[] roster = {"Standard", "Tank", "Speedster", "Brawler", "Floaty", "Glass Cannon"};

        for (String name : roster) {
            charRow.add(createCharButton(name, onBothChoicesMade));
        }

        // --- TOP CONTAINER ---
        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setOpaque(false);
        topContainer.add(title, BorderLayout.NORTH);
        charRow.setPreferredSize(new Dimension(0, 120)); 
        topContainer.add(charRow, BorderLayout.CENTER);

        add(topContainer, BorderLayout.NORTH);
    }

    private JButton createCharButton(String name, BiConsumer<String, String> callback) {
        JButton btn = new JButton(name);
        btn.setFont(new Font("Arial", Font.BOLD, 18));
        btn.setFocusPainted(false);
        btn.setBackground(new Color(225, 240, 255));
        btn.setForeground(new Color(30, 30, 50));
        btn.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hoveredChar = name;
                btn.setBackground(Color.WHITE);
                repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                hoveredChar = "";
                btn.setBackground(new Color(225, 240, 255));
                repaint();
            }
        });

        btn.addActionListener(e -> {
            if (p1Choice == null) {
                // Player 1 just picked
                p1Choice = name;
                title.setText("PLAYER 2: CHOOSE YOUR FIGHTER");
                title.setForeground(Color.RED); // P2 is Red
                System.out.println("P1 Selected: " + p1Choice);
            } else if (p2Choice == null) {
                // Player 2 just picked
                p2Choice = name;
                System.out.println("P2 Selected: " + p2Choice);
                // Both are done! Send the data back to the main game window
                callback.accept(p1Choice, p2Choice);
            }
        });

        return btn;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        if (!hoveredChar.equals("")) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Background Ghost Text
            g2.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 120));
            g2.setColor(new Color(255, 255, 255, 10)); 
            
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(hoveredChar)) / 2;
            int y = (getHeight() / 2) + 100;
            g2.drawString(hoveredChar, x, y);

            // Selection Indicator
            String currentPicker = (p1Choice == null) ? "PLAYER 1" : "PLAYER 2";
            g2.setFont(new Font("Arial", Font.PLAIN, 30));
            g2.setColor(p1Choice == null ? Color.CYAN : Color.RED);
            g2.drawString(currentPicker + " PREVIEWING: " + hoveredChar, (getWidth()/2) - 200, y + 60);
        }
    }
}