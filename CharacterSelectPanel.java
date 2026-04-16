import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class CharacterSelectPanel extends JPanel {
    
    // This allows us to "send" the choice back to the GameWindow
    public CharacterSelectPanel(Consumer<String> onChoiceMade) {
        setLayout(new BorderLayout());
        setBackground(new Color(20, 20, 40));

        JLabel title = new JLabel("CHOOSE YOUR FIGHTER", SwingConstants.CENTER);
        title.setFont(new Font("Verdana", Font.BOLD, 40));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(50, 0, 50, 0));
        add(title, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 40, 20));

        // Create buttons
        buttonPanel.add(createCharButton("Standard", Color.BLUE, onChoiceMade));
        buttonPanel.add(createCharButton("Tank", Color.RED, onChoiceMade));

        add(buttonPanel, BorderLayout.CENTER);
    }

    private JButton createCharButton(String name, Color color, Consumer<String> callback) {
        JButton btn = new JButton(name);
        btn.setPreferredSize(new Dimension(250, 350));
        btn.setFont(new Font("Arial", Font.BOLD, 24));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        
        // When clicked, run the callback function with the name of the character
        btn.addActionListener(e -> callback.accept(name));
        
        return btn;
    }
}
