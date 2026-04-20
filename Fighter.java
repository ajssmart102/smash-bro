import java.awt.*;
import java.util.*;

public class Fighter {
    public float x, y, velX, velY;
    public int width = 50, height = 80;
    public String name;
    public Color color;
    public int facingDir = 1; // 1 = Right, -1 = Left

    public boolean isCrouching = false;
    protected int normalHeight = 80;
    protected int crouchHeight = 40;
    
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
        // Check for Crouch Input (Assume index 4 is the Down key)
        // Only allow crouching if touching the ground (jumpsLeft == maxJumps)
        if (keyMap[keys[4]] && jumpsLeft == maxJumps) {
            if (!isCrouching) {
                isCrouching = true;
                y += (normalHeight - crouchHeight); // Shift Y down so feet stay on ground
                height = crouchHeight;
            }
        } else {
            if (isCrouching) {
                isCrouching = false;
                y -= (normalHeight - crouchHeight); // Shift Y up to grow back to normal
                height = normalHeight;
            }
        }
        
        // Movement (Prevent or slow down movement while crouching)
        if (keyMap[keys[0]]) { 
            velX = isCrouching ? -walkSpeed * 0.3f : -walkSpeed; 
            facingDir = -1; 
        }
        else if (keyMap[keys[1]]) { 
            velX = isCrouching ? walkSpeed * 0.3f : walkSpeed; 
            facingDir = 1; 
        }
        else { velX *= 0.8f; }

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

        // Platform Collision section
        for (Platform p : platforms) 
        {
            // We check velY > 0 so collision only happens when falling
            if (velY > 0 && x + width > p.x && x < p.x + p.width &&
                y + height >= p.y && y + height <= p.y + p.height + velY)
            {
                
                // This is the key: 'height' here will be 40 if crouching 
                // or 80 if standing, keeping you glued to the floor.
                y = p.y - height; 
                velY = 0;
                jumpsLeft = maxJumps; // Refreshes jumps and allows crouching
            }
        }

        if (attackTimer > 0) attackTimer--;
    }

    public Rectangle getBounds() { return new Rectangle((int)x, (int)y, width, height); }

    public Rectangle getHitbox() 
    {
        if (attackTimer < 5 || attackTimer > 15) return null; // Active frames
        
        int hx = (facingDir == 1) ? (int)x + width : (int)x - 60;

        // --- STEP 4: Adjust the Y position of the hitbox based on crouching ---
        // If crouching, we set the hitbox 5 pixels from the top of the crouched head.
        // If standing, we keep it at the original 20 pixels.
        int hy = isCrouching ? (int)y + 5 : (int)y + 20; 
        // ----------------------------------------------------------------------

        return new Rectangle(hx, hy, 60, 40);
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
