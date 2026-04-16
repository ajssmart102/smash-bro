import java.awt.*;

public class TankFighter extends Fighter {
    
    public TankFighter(float x, float y, Color color, String name, int[] keyBindings) {
        // 'super' calls the constructor in Fighter.java
        super(x, y, color, name, keyBindings);
        
        // Overwriting base stats for a "Heavy" feel
        this.walkSpeed = 4.5f;        // Much slower than the default 8f
        this.jumpForce = -12.0f;      // Lower jump (default is -14f)
        this.baseDamage = 18.0f;      // More than double the default 8f
        
        // Attack timing
        this.attackDuration = 25;     // Stays "active" longer (slow swing)
        this.attackCooldownMax = 40;  // Longer wait between punches
        
        // Physical size
        this.width = 65;              // Wider body
        this.height = 85;             // Taller body
    }

    /**
     * Overriding the hitbox to be much larger than a standard fighter.
     * This represents a "Heavy Smash" or a large swipe.
     */
    @Override
    public Rectangle getHitbox() {
        if (attackTimer <= 0) return null;
        
        // A larger box that reaches further out
        int boxSize = 80;
        int hx = (facingDir == 1) ? (int) x + width : (int) x - boxSize;
        
        // Positioned slightly lower to hit grounded targets easily
        return new Rectangle(hx, (int) y + 5, boxSize, boxSize);
    }

    /**
     * Overriding knockback to make the Tank feel powerful.
     */
    @Override
    public float[] getKnockback(Fighter target) {
        // Horizontal force is much higher (12f vs 6f)
        float kbX = facingDir * 12f;
        // Vertical force is slightly higher
        float kbY = -10f;
        
        return new float[]{kbX, kbY};
    }
}
