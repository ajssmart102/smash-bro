import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class InputHandler extends KeyAdapter {
    private Gamestate state;

    // This constructor matches the "new InputHandler(state)" in your Window code
    public InputHandler(Gamestate state) {
        this.state = state;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        // Map your keys to your Gamestate methods
        if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) {
            state.setMovingLeft(true);
        }
        if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) {
            state.setMovingRight(true);
        }
        if (key == KeyEvent.VK_SPACE || key == KeyEvent.VK_W) {
            state.jump();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) {
            state.setMovingLeft(false);
        }
        if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) {
            state.setMovingRight(false);
        }
    }
}
