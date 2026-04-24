import java.awt.*;
import java.util.*;

public class Fighter {
    public float x, y, velX, velY;
    public int width = 50, height = 80;
    public String name;
    public Color color;
    public int facingDir = 1;

    protected float walkSpeed = 7f;
    protected float jumpForce = -14f;
    protected float gravity = 0.5f;
    protected int maxJumps = 2;
    protected int jumpsLeft = 2;

    public float damage = 0;
    public int stocks = 3;
    protected int attackTimer = 0;
    protected Set<Fighter> hitTargets = new HashSet<>();
    public boolean isShielding = false; 
    
    public enum AttackType { NONE, NEUTRAL, SIDE, UP, DOWN, GRAB }
    protected AttackType currentAttack = AttackType.NONE;

    public Fighter grabbedEnemy = null; 
    public boolean isBeingHeld = false;

    public boolean isCharging = false;
    public int chargeFrames = 0;
    public final int MAX_CHARGE = 60;
    public float chargeMultiplier = 1.0f;

    protected int[] keys; // 0:L, 1:R, 2:Up, 3:Shield, 4:Attack, 5:Grab. 
    // Note: We use the raw KeyEvent for "Down" detection to allow Down Attacks.

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
        this.isShielding = false;
    }

    public void update(boolean[] keyMap, java.util.List<Platform> platforms) {
        if (isBeingHeld) {
            velX = 0; velY = 0;
            isShielding = false;
            return; 
        }

        if (grabbedEnemy != null) {
            grabbedEnemy.x = this.x + (this.facingDir * 40);
            grabbedEnemy.y = this.y;
            handleThrows(keyMap);
            isShielding = false;
            return; 
        }

        // --- SHIELD (Index 3: C/M) ---
        if (keyMap[keys[3]] && attackTimer <= 0 && !isCharging && Math.abs(velY) < 1.0f) {
            isShielding = true;
            velX = 0;
        } else {
            isShielding = false;
        }

        // --- ATTACK DIRECTIONAL CHECKS ---
        // We check for "S" or "Down Arrow" specifically for the Down Attack
        boolean isDownPressed = (name.equals("P1")) ? keyMap[java.awt.event.KeyEvent.VK_S] : keyMap[java.awt.event.KeyEvent.VK_DOWN];
        boolean isTryingToUpAttack = keyMap[keys[4]] && keyMap[keys[2]];
        boolean isTryingToDownAttack = keyMap[keys[4]] && isDownPressed;

        // Movement
        if (attackTimer <= 0 && !isShielding) {
            if (keyMap[keys[0]]) { velX = -walkSpeed; facingDir = -1; }
            else if (keyMap[keys[1]]) { velX = walkSpeed; facingDir = 1; }
            else { velX *= 0.8f; }
        } else if (attackTimer > 0) {
            velX *= 0.85f;
        }

        // Jumping
        if (keyMap[keys[2]] && jumpsLeft > 0 && attackTimer <= 5 && !isTryingToUpAttack && !isShielding) {
            velY = jumpForce;
            jumpsLeft--;
            keyMap[keys[2]] = false; 
            attackTimer = 0;
            currentAttack = AttackType.NONE;
        }

        // Grab
        if (keyMap[keys[5]] && attackTimer <= 0 && !isCharging && !isShielding) {
            currentAttack = AttackType.GRAB;
            attackTimer = 18;
            hitTargets.clear();
        }

        // --- ATTACK BUTTON (Including Down Attack) ---
        if (keyMap[keys[4]] && attackTimer <= 0 && !isCharging && !isShielding) {
            isCharging = true;
            chargeFrames = 0;
            
            if (keyMap[keys[2]]) {
                currentAttack = AttackType.UP;
                keyMap[keys[2]] = false; 
            }
            else if (isDownPressed) {
                currentAttack = AttackType.DOWN;
            }
            else if (keyMap[keys[0]] || keyMap[keys[1]]) {
                currentAttack = AttackType.SIDE;
            }
            else {
                currentAttack = AttackType.NEUTRAL;
            }
        }

        if (isCharging) {
            if (keyMap[keys[4]] && chargeFrames < MAX_CHARGE) {
                chargeFrames++;
                if (keyMap[keys[0]]) velX = -walkSpeed * 0.5f;
                if (keyMap[keys[1]]) velX = walkSpeed * 0.5f;
            } else {
                isCharging = false;
                attackTimer = 22;
                hitTargets.clear();
                chargeMultiplier = 1.0f + ((float)chargeFrames / MAX_CHARGE);
            }
        }

        // Physics
        velY += gravity;
        x += velX; y += velY;

        for (Platform p : platforms) {
            if (velY > 0 && x + width > p.x && x < p.x + p.width &&
                y + height >= p.y && y + height <= p.y + p.height + velY) {
                // Grounding logic
                y = p.y - height; velY = 0; jumpsLeft = maxJumps;
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
        boolean isDownPressed = (name.equals("P1")) ? keyMap[java.awt.event.KeyEvent.VK_S] : keyMap[java.awt.event.KeyEvent.VK_DOWN];

        if (keyMap[keys[0]] || keyMap[keys[1]]) { tx = facingDir * 14; ty = -4; throwTriggered = true; }
        else if (keyMap[keys[2]]) { tx = 0; ty = -16; throwTriggered = true; }
        else if (isDownPressed) { tx = 0; ty = 12; throwTriggered = true; }

        if (throwTriggered) {
            grabbedEnemy.velX = tx; grabbedEnemy.velY = ty;
            grabbedEnemy.isBeingHeld = false;
            grabbedEnemy.damage += 6;
            grabbedEnemy = null;
            attackTimer = 15;
        }
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
            case UP: hx = (int)x - 15; hy = (int)y - 50; hw = width + 30; hh = 60; break;
            case DOWN: hx = (int)x - 10; hy = (int)y + height; hw = width + 20; hh = 40; break; // Hitbox below feet
            case SIDE: hx = (facingDir == 1) ? (int)x + width : (int)x - 80; hy = (int)y + 20; hw = 80; hh = 40; break;
            default: hx = (facingDir == 1) ? (int)x + width : (int)x - 50; hy = (int)y + 20; hw = 50; hh = 40; break;
        }
        return new Rectangle(hx, hy, hw, hh);
    }

    public void draw(Graphics2D g) {
        if (isShielding) {
            g.setColor(new Color(100, 200, 255, 120));
            g.fillOval((int)x - 15, (int)y - 5, width + 30, height + 10);
            g.setColor(Color.WHITE);
            g.drawOval((int)x - 15, (int)y - 5, width + 30, height + 10);
        }

        g.setColor(color);
        g.fillRoundRect((int)x, (int)y, width, height, 15, 15);
        
        if (isCharging) {
            g.setColor(Color.WHITE);
            g.fillRect((int)x, (int)y - 15, (int)((float)width * ((float)chargeFrames / MAX_CHARGE)), 5);
        }
        
        if (getHitbox() != null) {
            g.setColor(currentAttack == AttackType.GRAB ? new Color(0, 255, 255, 100) : new Color(255, 255, 0, 100));
            g.fill(getHitbox());
        }
    }
}