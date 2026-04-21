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

    public boolean isCrouching = false;
    protected int normalHeight = 80;
    protected int crouchHeight = 40;

    // Combat
    public float damage = 0;
    public int stocks = 3;
    protected int attackTimer = 0;
    protected Set<Fighter> hitTargets = new HashSet<>();
    
    // NEW: Enum to define our attack directions
    public enum AttackType { NONE, NEUTRAL, SIDE, UP, DOWN }
    protected AttackType currentAttack = AttackType.NONE;

    // UPDATED: Added a "Down" key at index 3. 
    // New format: [Left, Right, Jump, Down, Attack]
    protected int[] keys; 

    public Fighter(float x, float y, String name, Color color, int[] keys) {
        this.x = x; this.y = y; this.name = name; this.color = color; this.keys = keys;
    }
    public void update(boolean[] keyMap, java.util.List<Platform> platforms) 
    {
        // --- CROUCH LOGIC ---
        // Only allow crouching if grounded (jumpsLeft == maxJumps) and not currently attacking
        if (keyMap[keys[3]] && jumpsLeft == maxJumps && attackTimer <= 0) {
            if (!isCrouching) {
                isCrouching = true;
                y += (normalHeight - crouchHeight); // Shift Y down to keep feet on floor
                height = crouchHeight;
            }
        } else {
            if (isCrouching) {
                isCrouching = false;
                y -= (normalHeight - crouchHeight); // Shift Y up to stand back up
                height = normalHeight;
            }
        }

        // --- MOVEMENT (Modified for Crouch Speed) ---
        float currentSpeed = isCrouching ? walkSpeed * 0.3f : walkSpeed;
        if (keyMap[keys[0]]) { velX = -currentSpeed; facingDir = -1; }
        else if (keyMap[keys[1]]) { velX = currentSpeed; facingDir = 1; }
        else { velX *= 0.8f; }

        // Jump
        if (keyMap[keys[2]] && jumpsLeft > 0) {
            velY = jumpForce;
            jumpsLeft--;
            keyMap[keys[2]] = false; // Prevent auto-repeat
        }

        // Attack (Notice keys[4] is now the attack key)
        if (keyMap[keys[4]] && attackTimer <= 0) {
            attackTimer = 20;
            hitTargets.clear();
            
            // NEW: Determine which directional attack to use
            if (keyMap[keys[3]]) {
                currentAttack = AttackType.DOWN;
            } else if (keyMap[keys[2]]) {
                currentAttack = AttackType.UP;
            } else if (keyMap[keys[0]] || keyMap[keys[1]]) {
                currentAttack = AttackType.SIDE;
            } else {
                currentAttack = AttackType.NEUTRAL;
            }
        }

        // Physics
        velY += gravity;
        x += velX;
        y += velY;

        // Platform Collision
        for (Platform p : platforms) {
            // We check velY > 0 so collision only happens when falling/landing
            if (velY > 0 && x + width > p.x && x < p.x + p.width &&
                y + height >= p.y && y + height <= p.y + p.height + velY) {
                
                // This is the critical part: 
                // 'height' will be 40 if isCrouching is true, or 80 if false.
                y = p.y - height; 
                
                velY = 0;
                jumpsLeft = maxJumps; // Resetting jumps allows the player to crouch again
            }
        }

        // Timer management
        if (attackTimer > 0) {
            attackTimer--;
            // NEW: Reset attack type when the animation finishes
            if (attackTimer == 0) {
                currentAttack = AttackType.NONE;
            }
        }
    }

    public Rectangle getBounds() { return new Rectangle((int)x, (int)y, width, height); }

    public Rectangle getHitbox() {
        // Active frames check
        if (attackTimer < 5 || attackTimer > 15 || currentAttack == AttackType.NONE) return null; 

        int hx, hy, hw, hh;

        // NEW: Generate a different hitbox based on the current attack
        switch (currentAttack) {
            case UP:
                hx = (int)x - 10;
                hy = (int)y - 40; // Placed above the player's head
                hw = width + 20;  // Slightly wider than the player
                hh = 50;
                break;
            case DOWN:
                hx = (int)x - 20;
                hy = (int)y + height - 20; // Placed at the player's feet
                hw = width + 40; // Hits on both sides
                hh = 30;
                break;
            case SIDE:
                hx = (facingDir == 1) ? (int)x + width : (int)x - 80;
                hy = (int)y + 20;
                hw = 80; // Long forward reach
                hh = 40;
                break;
            case NEUTRAL:
            default:
                hx = (facingDir == 1) ? (int)x + width : (int)x - 50;
                hy = (int)y + 20;
                hw = 50; // Shorter forward reach
                hh = 40;
                break;
        }
        
        return new Rectangle(hx, hy, hw, hh);
    }

    public void draw(Graphics2D g) {
        g.setColor(color);
        g.fillRoundRect((int)x, (int)y, width, height, 15, 15);
        if (getHitbox() != null) {
            // Drawing the hitbox in translucent yellow so you can debug it
            g.setColor(new Color(255, 255, 0, 150)); 
            g.fill(getHitbox());
        }
    }
}