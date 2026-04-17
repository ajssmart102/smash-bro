import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class InputHandler extends KeyAdapter {
    private Gamestate state;

    public InputHandler(Gamestate state) {
        this.state = state;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        // Set the boolean to true in the Gamestate array
        if (code >= 0 && code < state.keys.length) {
            state.keys[code] = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        // Set the boolean to false when the key is let go
        if (code >= 0 && code < state.keys.length) {
            state.keys[code] = false;
        }
    }
}
