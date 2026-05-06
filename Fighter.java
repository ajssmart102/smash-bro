import java.awt.*;
import java.util.*;

public class Fighter {
    public float x, y, velX, velY;
    public int width = 50, height = 80;
    public String name;
    public Color color;
    public int facingDir = 1;

    // --- CHARACTER STATS (Applied by Gamestate) ---
    public CharacterStats stats;
    public float walkSpeed;
    public float jumpForce;
    public float gravity;
    public float weight; // Multiplier for gravity/launch speed
    public float attackDamageMultiplier;

    public float maxHealth = 100f;
    public float currentHealth = 100f;
    
    protected int maxJumps = 2;
    protected int jumpsLeft = 2;

    // --- COMBAT ---
    public float damage = 0; // % Damage (Smash style)
    public int stocks = 3;
    protected int attackTimer = 0;
    protected Set<Fighter> hitTargets = new HashSet<>();
    public boolean hasFinalSmash = false;
    
    // --- STATES ---
    public boolean isHelpless = false;    
    public boolean ledgeGrabbed = false; 

    public enum AttackType { NONE, NEUTRAL, SIDE, UP, DOWN, GRAB, UP_SPECIAL, FINAL_SMASH }
    protected AttackType currentAttack = AttackType.NONE;

    // --- GRAB & SHIELD ---
    public Fighter grabbedEnemy = null; 
    public boolean isBeingHeld = false;
    public boolean isShielding = false;

    // --- CHARGING ---
    public boolean isCharging = false;
    public int chargeFrames = 0;
    public final int MAX_CHARGE = 60;
    public float chargeMultiplier = 1.0f;

    protected int[] keys; 

    public Fighter(float x, float y, CharacterStats stats, Color color, int[] keys) {
    this.x = x; 
    this.y = y; 
    this.color = color; 
    this.keys = keys;

    this.stats = stats;
    
    // Now Java knows what 'stats' is!
    this.name = stats.name;
    this.walkSpeed = stats.walkSpeed;
    this.jumpForce = stats.jumpForce;
    this.gravity = stats.gravity;
    this.weight = stats.weight;
    this.attackDamageMultiplier = stats.dm;
    }
    
    public void respawn(float startX, float startY) {
        this.x = startX; this.y = startY;
        this.velX = 0; this.velY = 0;
        this.stocks--;
        this.jumpsLeft = maxJumps;
        this.currentAttack = AttackType.NONE; 
        this.attackTimer = 0;
        this.damage = 0;
        this.currentHealth = maxHealth; // Reset health if using health bar logic
        this.grabbedEnemy = null;
        this.isBeingHeld = false;
        this.isShielding = false;
        this.isHelpless = false;
        this.ledgeGrabbed = false;
        this.hasFinalSmash = false;
    }

    public void update(boolean[] keyMap, java.util.List<Platform> platforms) {
        if (isBeingHeld) { velX = 0; velY = 0; return; }

        if (grabbedEnemy != null) {
            grabbedEnemy.x = this.x + (this.facingDir * 40);
            grabbedEnemy.y = this.y;
            handleThrows(keyMap);
            if (keyMap[keys[0]]) { x -= 2; facingDir = -1; }
            if (keyMap[keys[1]]) { x += 2; facingDir = 1; }
            return; 
        }

        if (ledgeGrabbed) {
            velX = 0; velY = 0;
            if (keyMap[keys[2]]) { 
                velY = jumpForce;
                ledgeGrabbed = false;
                isHelpless = false;
            }
            return;
        }

        // Shielding logic
        if (keyMap[keys[3]] && attackTimer <= 0 && !isCharging && Math.abs(velY) < 1.5f) {
            isShielding = true;
            velX = 0;
        } else {
            isShielding = false;
        }

        // Final Smash activation
        if (hasFinalSmash && keyMap[keys[6]] && attackTimer <= 0 && !isShielding) {
            currentAttack = AttackType.FINAL_SMASH;
            attackTimer = 60; 
            hasFinalSmash = false; 
            hitTargets.clear();
        }

        // Up Special
        if (keyMap[keys[6]] && keyMap[keys[2]] && !isHelpless && !isShielding) {
            currentAttack = AttackType.UP_SPECIAL;
            velY = -18f; 
            attackTimer = 25;
            isHelpless = true;
            keyMap[keys[2]] = false; 
        }

        // Horizontal Movement
        if (attackTimer <= 0 && !isShielding) {
            float speed = isHelpless ? walkSpeed * 0.6f : walkSpeed;
            if (keyMap[keys[0]]) { velX = -speed; facingDir = -1; }
            else if (keyMap[keys[1]]) { velX = speed; facingDir = 1; }
            else { velX *= 0.8f; }
        } else {
            velX *= 0.85f;
        }

        // Jumping
        boolean isTryingToUpAttack = keyMap[keys[4]] && keyMap[keys[2]];
        if (keyMap[keys[2]] && jumpsLeft > 0 && attackTimer <= 5 && !isTryingToUpAttack && !isShielding && !isHelpless) {
            velY = jumpForce;
            jumpsLeft--;
            keyMap[keys[2]] = false; 
            attackTimer = 0;
            currentAttack = AttackType.NONE;
        }

        // Attack & Grab Triggers
        if (keyMap[keys[5]] && attackTimer <= 0 && !isCharging && !isShielding) {
            currentAttack = AttackType.GRAB;
            attackTimer = 18;
            hitTargets.clear();
        }

        if (keyMap[keys[4]] && attackTimer <= 0 && !isCharging && !isShielding) {
            isCharging = true;
            chargeFrames = 0;
            // Simplified check for direction of attack
            boolean isDownPressed = (keys[0] == 65) ? keyMap[83] : keyMap[40]; // Quick S/Down check
            if (keyMap[keys[2]]) { currentAttack = AttackType.UP; keyMap[keys[2]] = false; }
            else if (isDownPressed) currentAttack = AttackType.DOWN;
            else if (keyMap[keys[0]] || keyMap[keys[1]]) currentAttack = AttackType.SIDE;
            else currentAttack = AttackType.NEUTRAL;
        }

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

        // --- PHYSICS (Weighted) ---
        velY += (gravity * weight); // Weight affects fall speed
        x += velX; 
        y += velY;

        for (Platform p : platforms) {
            // Ledge detection
            if (velY > 0 && !ledgeGrabbed) {
                if (Math.abs(x + width - p.x) < 15 && Math.abs(y - p.y) < 25) {
                    ledgeGrabbed = true; x = p.x - width; y = p.y; return;
                }
                if (Math.abs(x - (p.x + p.width)) < 15 && Math.abs(y - p.y) < 25) {
                    ledgeGrabbed = true; x = p.x + p.width; y = p.y; return;
                }
            }

            // Platform collision
            if (velY >= 0 && x + width > p.x && x < p.x + p.width &&
                y + height >= p.y && y + height <= p.y + p.height + velY + 2) {
                y = p.y - height; 
                velY = 0; 
                jumpsLeft = maxJumps;
                isHelpless = false;
                x += p.velX; // Move with platform
                y += p.velY; 
            }
        }

        if (attackTimer > 0) {
            attackTimer--;
            if (attackTimer == 0) currentAttack = AttackType.NONE;
        }
    }

    private void handleThrows(boolean[] keyMap) {
        boolean throwTriggered = false;
        float tx = 0, ty = 0;
        boolean isDownPressed = (keys[0] == 65) ? keyMap[83] : keyMap[40];

        if (keyMap[keys[0]]) { tx = -14; ty = -4; throwTriggered = true; }
        else if (keyMap[keys[1]]) { tx = 14; ty = -4; throwTriggered = true; }
        else if (keyMap[keys[2]]) { tx = 0; ty = -16; throwTriggered = true; }
        else if (isDownPressed) { tx = 0; ty = 12; throwTriggered = true; }
        
        if (throwTriggered && grabbedEnemy != null) {
            grabbedEnemy.velX = tx; grabbedEnemy.velY = ty;
            grabbedEnemy.isBeingHeld = false;
            grabbedEnemy.damage += (6 * attackDamageMultiplier);
            grabbedEnemy = null;
            attackTimer = 15;
            currentAttack = AttackType.NONE;
        }
    }

    public Rectangle getBounds() { return new Rectangle((int)x, (int)y, width, height); }

    public Rectangle getHitbox() {
        if (attackTimer <= 0 || currentAttack == AttackType.NONE || isCharging) return null; 
        int hx, hy, hw, hh;
        switch (currentAttack) {
            case GRAB: hx = (facingDir == 1) ? (int)x + width : (int)x - 35; hy = (int)y + 20; hw = 35; hh = 40; break;
            case UP: hx = (int)x - 15; hy = (int)y - 50; hw = width + 30; hh = 60; break;
            case DOWN: hx = (int)x - 10; hy = (int)y + height; hw = width + 20; hh = 40; break;
            case UP_SPECIAL: hx = (int)x - 5; hy = (int)y - 10; hw = width + 10; hh = height + 10; break;
            case SIDE: hx = (facingDir == 1) ? (int)x + width : (int)x - 80; hy = (int)y + 20; hw = 80; hh = 40; break;
            case FINAL_SMASH: hx = (int)x - 150; hy = (int)y - 150; hw = 350; hh = 350; break; 
            default: hx = (facingDir == 1) ? (int)x + width : (int)x - 50; hy = (int)y + 20; hw = 50; hh = 40; break;
        }
        return new Rectangle(hx, hy, hw, hh);
    }

    public void draw(Graphics2D g) {
        if (isShielding) {
            g.setColor(new Color(100, 200, 255, 120));
            g.fillOval((int)x - 15, (int)y - 5, width + 30, height + 10);
        }
        if (hasFinalSmash) {
            g.setColor(new Color(255, 255, 0, 50));
            g.fillOval((int)x - 10, (int)y - 10, width + 20, height + 20);
        }
        
        g.setColor(isHelpless ? Color.DARK_GRAY : color);
        g.fillRoundRect((int)x, (int)y, width, height, 15, 15);
        
        // Show charging bar
        if (isCharging) {
            g.setColor(Color.WHITE);
            g.fillRect((int)x, (int)y - 15, (int)((float)width * ((float)chargeFrames / MAX_CHARGE)), 5);
        }

        // Debug: Draw hitbox
        Rectangle hb = getHitbox();
        if (hb != null) {
            g.setColor(new Color(255, 255, 0, 100));
            g.fill(hb);
        }
    }
}