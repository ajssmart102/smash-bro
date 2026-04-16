import java.awt.Color;

public class TankFighter extends Fighter {
    public TankFighter(float x, float y, String name, Color color, int[] keys) {
        super(x, y, name, color, keys);
        this.walkSpeed = 4f;    // Slower
        this.width = 70;        // Bigger
        this.height = 100;
        this.jumpForce = -12f;  // Heavier
    }

    // You can override getHitbox() here to make it huge!
}
