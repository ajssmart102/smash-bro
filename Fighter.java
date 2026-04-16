import java.awt.*;
import java.util.*;

public class Fighter {
    public float x, y, velX, velY;
    public int width = 50, height = 80;
    public String name;
    public Color color;
    public int facingDir = 1; // 1 = Right, -1 = Left

    // Stats (Can be overridden by subclasses)
    protected float walkSpeed = 7f;
    protected float jumpForce = -14f;
    protected float gravity = 0.5f;
    protected int maxJumps = 2;
    protected int jumpsLeft = 2;

    // Combat
    public float damage = 0;
    public int stocks = 3;
    protected int attackTimer = 0;
    protected Set<Fighter> hitTargets = new HashSet<>();

    protected int[] keys; // [Left, Right, Jump, Attack]

    public Fighter(float x, float y, String name, Color color, int[] keys) {
        this.x = x; this.y = y; this.name = name; this.color = color; this.keys = keys;
    }

    public void update(boolean[] keyMap, java.util.List<Platform> platforms) {
        // Movement
        if (keyMap[keys[0]]) { velX = -walkSpeed; facingDir = -1; }
        else if (keyMap[keys[1]]) { velX = walkSpeed; facingDir = 1; }
        else { velX *= 0.8f; } // Friction

        // Jump
        if (keyMap[keys[2]] && jumpsLeft > 0) {
            velY = jumpForce;
            jumpsLeft--;
            keyMap[keys[2]] = false; // Prevent auto-repeat
        }

        // Attack
        if (keyMap[keys[3]] && attackTimer <= 0) {
            attackTimer = 20;
            hitTargets.clear();
        }

        // Physics
        velY += gravity;
        x += velX;
        y += velY;

        // Platform Collision
        for (Platform p : platforms) {
            if (velY > 0 && x + width > p.x && x < p.x + p.width &&
                y + height >= p.y && y + height <= p.y + p.height + velY) {
                y = p.y - height;
                velY = 0;
                jumpsLeft = maxJumps;
            }
        }

        if (attackTimer > 0) attackTimer--;
    }

    public Rectangle getBounds() { return new Rectangle((int)x, (int)y, width, height); }

    public Rectangle getHitbox() {
        if (attackTimer < 5 || attackTimer > 15) return null; // Active frames
        int hx = (facingDir == 1) ? (int)x + width : (int)x - 60;
        return new Rectangle(hx, (int)y + 20, 60, 40);
    }

    public void draw(Graphics2D g) {
        g.setColor(color);
        g.fillRoundRect((int)x, (int)y, width, height, 15, 15);
        if (getHitbox() != null) {
            g.setColor(new Color(255, 255, 0, 150));
            g.fill(getHitbox());
        }
    }
}
