import java.awt.*;
import java.util.*;

public class Fighter {
    public float x, y, velX, velY;
    public int width = 50, height = 80;
    public String name;
    public Color color;
    public int facingDir = 1;

    // Stats
    protected float walkSpeed = 7f;
    protected float jumpForce = -14f;
    protected float gravity = 0.5f;
    protected int maxJumps = 2;
    protected int jumpsLeft = 2;
    protected float baseKnockback = 5.0f;    // The "nudge" at 0% damage
    protected float knockbackScaling = 0.15f; // How much damage increases the push

    // Combat
    public float damage = 0;
    public int stocks = 3;
    protected int attackTimer = 0;
    protected Set<Fighter> hitTargets = new HashSet<>();
    
    public enum AttackType { NONE, NEUTRAL, SIDE, UP, DOWN, GRAB }
    protected AttackType currentAttack = AttackType.NONE;

    // Grab Mechanics
    public Fighter grabbedEnemy = null; 
    public boolean isBeingHeld = false;

    // Charging Mechanics
    public boolean isCharging = false;
    public int chargeFrames = 0;
    public final int MAX_CHARGE = 60;
    public float chargeMultiplier = 1.0f;

    protected int[] keys; // Index 0:L, 1:R, 2:Up, 3:Down, 4:Attack, 5:Grab

    public Fighter(float x, float y, String name, Color color, int[] keys) {
        this.x = x; this.y = y; this.name = name; this.color = color; this.keys = keys;
    }
    
    public void respawn(float startX, float startY) {
        this.x = startX; this.y = startY;
        this.velX = 0; this.velY = 0;
        this.stocks--;
        this.jumpsLeft = maxJumps;
        this.currentAttack = AttackType.NONE; 
        this.attackTimer = 0;
        this.damage = 0;
        this.grabbedEnemy = null;
        this.isBeingHeld = false;
    }

    public void update(boolean[] keyMap, java.util.List<Platform> platforms) {
        if (isBeingHeld) {
            velX = 0; velY = 0;
            return; 
        }

        if (grabbedEnemy != null) {
            grabbedEnemy.x = this.x + (this.facingDir * 40);
            grabbedEnemy.y = this.y;
            handleThrows(keyMap);
            return; // Lock movement while holding someone
        }

        // Movement Logic
        if (attackTimer <= 0) {
            if (keyMap[keys[0]]) { velX = -walkSpeed; facingDir = -1; }
            else if (keyMap[keys[1]]) { velX = walkSpeed; facingDir = 1; }
            else { velX *= 0.8f; }
        } else {
            velX *= 0.85f; // Smooth slide
        }

        // Jumping
        if (keyMap[keys[2]] && jumpsLeft > 0 && attackTimer <= 5) {
            velY = jumpForce;
            jumpsLeft--;
            keyMap[keys[2]] = false; 
            attackTimer = 0;
            currentAttack = AttackType.NONE;
        }

        // --- NEW SEPARATED INPUT LOGIC ---

        // 1. GRAB BUTTON (Index 5)
        if (keyMap[keys[5]] && attackTimer <= 0 && !isCharging) {
            currentAttack = AttackType.GRAB;
            attackTimer = 18; // Slightly longer commitment for missed grabs
            hitTargets.clear();
        }

        // 2. ATTACK BUTTON (Index 4)
        if (keyMap[keys[4]] && attackTimer <= 0 && !isCharging) {
            isCharging = true;
            chargeFrames = 0;
            if (keyMap[keys[3]]) currentAttack = AttackType.DOWN;
            else if (keyMap[keys[2]]) currentAttack = AttackType.UP;
            else if (keyMap[keys[0]] || keyMap[keys[1]]) currentAttack = AttackType.SIDE;
            else currentAttack = AttackType.NEUTRAL;
        }

        // Charging logic
        if (isCharging) {
            if (keyMap[keys[4]] && chargeFrames < MAX_CHARGE) {
                chargeFrames++;
            } else {
                isCharging = false;
                attackTimer = 22;
                hitTargets.clear();
                chargeMultiplier = 1.0f + ((float)chargeFrames / MAX_CHARGE);
            }
        }

        // Physics & Platforms
        velY += gravity;
        x += velX; y += velY;
        for (Platform p : platforms) {
            if (velY > 0 && x + width > p.x && x < p.x + p.width &&
                y + height >= p.y && y + height <= p.y + p.height + velY) {
                y = p.y - height; velY = 0; jumpsLeft = maxJumps;
            }
        }

        if (attackTimer > 0) {
            attackTimer--;
            if (attackTimer == 0) currentAttack = AttackType.NONE;
        }
    }

    private void handleThrows(boolean[] keyMap) 
    {
        boolean throwTriggered = false;
        float tx = 0, ty = 0;

        // STEP 1 APPLIED: Calculate power based on victim's damage
        float knockbackMult = baseKnockback + (grabbedEnemy.damage * knockbackScaling);

        if (keyMap[keys[0]] || keyMap[keys[1]]) { 
            tx = facingDir * (knockbackMult + 5); // Side throws usually need extra oomph
            ty = -2; 
            throwTriggered = true; 
        }
        else if (keyMap[keys[2]]) { 
            tx = 0; 
            ty = -knockbackMult * 1.5f; // Upward throw scales heavily
            throwTriggered = true; 
        }
        else if (keyMap[keys[3]]) { 
            tx = 0; 
            ty = knockbackMult; 
            throwTriggered = true; 
        }

        if (throwTriggered) {
            grabbedEnemy.velX = tx; 
            grabbedEnemy.velY = ty;
            grabbedEnemy.isBeingHeld = false;
            grabbedEnemy.damage += 6; 
            grabbedEnemy = null;
            attackTimer = 15;
        }
    }

    public void takeHit(float attackDamage, int attackerDir, float baseKnockback, float knockbackScaling) {
        // 1. Add the damage first
        this.damage += attackDamage;

        // 2. Calculate Knockback: (Base + (CurrentDamage * Scaling))
        float totalKnockback = baseKnockback + (this.damage * knockbackScaling);

        // 3. Apply to velocities
        this.velX = attackerDir * totalKnockback;
        this.velY = -totalKnockback * 0.5f; // Launches them slightly upward
        
        // 4. Cancel any attacks they were doing (Hitstun)
        this.currentAttack = AttackType.NONE;
        this.attackTimer = 0;
        this.isCharging = false;
    }

    public Rectangle getBounds() { return new Rectangle((int)x, (int)y, width, height); }

    public Rectangle getHitbox() {
        if (attackTimer <= 0 || currentAttack == AttackType.NONE) return null; 
        int hx, hy, hw, hh;
        switch (currentAttack) {
            case GRAB:
                hx = (facingDir == 1) ? (int)x + width : (int)x - 35;
                hy = (int)y + 20; hw = 35; hh = 40;
                break;
            case UP: hx = (int)x - 10; hy = (int)y - 40; hw = width + 20; hh = 50; break;
            case DOWN: hx = (int)x - 20; hy = (int)y + height - 20; hw = width + 40; hh = 30; break;
            case SIDE: hx = (facingDir == 1) ? (int)x + width : (int)x - 80; hy = (int)y + 20; hw = 80; hh = 40; break;
            default: hx = (facingDir == 1) ? (int)x + width : (int)x - 50; hy = (int)y + 20; hw = 50; hh = 40; break;
        }
        return new Rectangle(hx, hy, hw, hh);
    }

    public void draw(Graphics2D g) {
        g.setColor(color);
        g.fillRoundRect((int)x, (int)y, width, height, 15, 15);
        if (isCharging) {
            g.setColor(Color.WHITE);
            g.fillRect((int)x, (int)y - 15, (int)((float)width * ((float)chargeFrames / MAX_CHARGE)), 5);
        }
        if (grabbedEnemy != null) {
            g.setColor(Color.CYAN);
            g.drawRect((int)x - 5, (int)y - 5, width + 10, height + 10);
        }
        if (getHitbox() != null) {
            g.setColor(currentAttack == AttackType.GRAB ? new Color(0, 255, 255, 150) : new Color(255, 255, 0, 150)); 
            g.fill(getHitbox());
        }
    }
}