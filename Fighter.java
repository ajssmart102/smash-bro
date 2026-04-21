import java.awt.*;
import java.util.*;

public class Fighter {
    public float x, y, velX, velY;
    public int width = 50, height = 80;
    public String name;
    public Color color;
    public int facingDir = 1; // 1 = Right, -1 = Left

    // Stats
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
    
    // Attack Directions
    public enum AttackType { NONE, NEUTRAL, SIDE, UP, DOWN }
    protected AttackType currentAttack = AttackType.NONE;

    // Charging Mechanics
    public boolean isCharging = false;
    public int chargeFrames = 0;
    public final int MAX_CHARGE = 60; // 60 frames = approx 1 second of charging
    public float chargeMultiplier = 1.0f; // Will range from 1.0x to 2.0x

    protected int[] keys; 

    public Fighter(float x, float y, String name, Color color, int[] keys) {
        this.x = x; this.y = y; this.name = name; this.color = color; this.keys = keys;
    }
    
    public void respawn(float startX, float startY) {
    this.x = startX;
    this.y = startY;
    this.velX = 0;
    this.velY = 0;
    this.stocks--; // Subtract a life
    this.jumpsLeft = maxJumps; // Reset jumps
    this.currentAttack = AttackType.NONE; 
    this.attackTimer = 0;
    }

    public void update(boolean[] keyMap, java.util.List<Platform> platforms) {
        // UPDATED: Removed "!isCharging" so you can move while charging!
        // Movement is only locked when the actual attack animation is playing (attackTimer > 0)
        if (attackTimer <= 0) {
            if (keyMap[keys[0]]) { velX = -walkSpeed; facingDir = -1; }
            else if (keyMap[keys[1]]) { velX = walkSpeed; facingDir = 1; }
            else { velX *= 0.8f; } // Friction
        } else {
            velX *= 0.5f; // Slide to a halt while actively attacking
        }

        // UPDATED: Removed "!isCharging" so you can jump while charging!
        if (keyMap[keys[2]] && jumpsLeft > 0 && attackTimer <= 0) {
            velY = jumpForce;
            jumpsLeft--;
            keyMap[keys[2]] = false; 
        }

        // --- Charging and Attacking Logic ---
        
        // 1. Start Charging
        if (keyMap[keys[4]] && attackTimer <= 0 && !isCharging) {
            isCharging = true;
            chargeFrames = 0;
            
            // Lock in the Smash direction
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

        // 2. Handle Charging & Releasing
        if (isCharging) {
            if (keyMap[keys[4]] && chargeFrames < MAX_CHARGE) {
                // Keep holding to charge
                chargeFrames++;
            } else {
                // Key released OR max charge reached: UNLEASH THE SMASH!
                isCharging = false;
                attackTimer = 20;
                hitTargets.clear();
                
                // Calculate power: 1.0 base + up to 1.0 extra based on charge time
                chargeMultiplier = 1.0f + ((float)chargeFrames / MAX_CHARGE);
            }
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

        // Timer management
        if (attackTimer > 0) {
            attackTimer--;
            if (attackTimer == 0) {
                currentAttack = AttackType.NONE;
            }
        }
    }

    public Rectangle getBounds() { return new Rectangle((int)x, (int)y, width, height); }

    public Rectangle getHitbox() {
        if (attackTimer < 5 || attackTimer > 15 || currentAttack == AttackType.NONE) return null; 

        int hx, hy, hw, hh;

        switch (currentAttack) {
            case UP: // Strong vertical reach
                hx = (int)x - 10;
                hy = (int)y - 40; 
                hw = width + 20;  
                hh = 50;
                break;
            case DOWN: // Good for low percents (hits both sides)
                hx = (int)x - 20;
                hy = (int)y + height - 20; 
                hw = width + 40; 
                hh = 30;
                break;
            case SIDE: // High damage/knockback, reaches far forward
                hx = (facingDir == 1) ? (int)x + width : (int)x - 80;
                hy = (int)y + 20;
                hw = 80; 
                hh = 40;
                break;
            case NEUTRAL:
            default:
                hx = (facingDir == 1) ? (int)x + width : (int)x - 50;
                hy = (int)y + 20;
                hw = 50; 
                hh = 40;
                break;
        }
        
        return new Rectangle(hx, hy, hw, hh);
    }

    public void draw(Graphics2D g) {
        g.setColor(color);
        g.fillRoundRect((int)x, (int)y, width, height, 15, 15);
        
        // Draw a visual indicator when charging so the player knows it's working
        if (isCharging) {
            g.setColor(Color.WHITE);
            // Draws a little growing white bar above the player's head
            g.fillRect((int)x, (int)y - 15, (int)((float)width * ((float)chargeFrames / MAX_CHARGE)), 5);
        }

        if (getHitbox() != null) {
            g.setColor(new Color(255, 255, 0, 150)); 
            g.fill(getHitbox());
        }
    }
}