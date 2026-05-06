import java.awt.*;
import java.util.*;

public class Fighter {
    // Basic Physics & Identity
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
    public int hitstun = 0;

    // Combat & States
    public float damage = 0;
    public int stocks = 3;
    protected int attackTimer = 0;
    protected Set<Fighter> hitTargets = new HashSet<>();
    public boolean hasFinalSmash = false;
    public boolean isHelpless = false;
    public boolean ledgeGrabbed = false;

    public enum AttackType { NONE, NEUTRAL, SIDE, UP, DOWN, GRAB, UP_SPECIAL, FINAL_SMASH }
    protected AttackType currentAttack = AttackType.NONE;

    // Grab & Items
    public Fighter grabbedEnemy = null;
    public Items heldItem = null; // Ensure you have an 'Items' class defined
    public boolean isBeingHeld = false;
    public boolean isShielding = false;

    // Charging Mechanics
    public boolean isCharging = false;
    public int chargeFrames = 0;
    public final int MAX_CHARGE = 60;
    public float chargeMultiplier = 1.0f;

    protected int[] keys; 

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
        this.isHelpless = false;
        this.ledgeGrabbed = false;
        this.hasFinalSmash = false;
    }

    public void update(boolean[] keyMap, java.util.List<Platform> platforms, java.util.List<Items> items) 
    {
        if (isBeingHeld) 
        { 
            velX = 0; 
            velY = 0; 
        }
        if (hitstun > 0) 
        {
            hitstun--;
            velX *= 0.98f; // Air friction during knockback
        }

        // 1. ITEM ATTACHMENT
        if (heldItem != null) {
            heldItem.isHeld = true;
            heldItem.x = (facingDir == 1) ? this.x + width - 5 : this.x - heldItem.width + 5;
            heldItem.y = this.y + 25;
            heldItem.velX = 0;
            heldItem.velY = 0;
        }

        // 2. LEDGE LOGIC (Highest Priority)
        if (ledgeGrabbed) {
            velX = 0; velY = 0;
            if (keyMap[keys[2]]) { // Up to jump off
                velY = jumpForce;
                ledgeGrabbed = false;
                isHelpless = false;
                keyMap[keys[2]] = false; 
            }
            return; 
        }

        // 3. INPUT PREPARATION
        // Check for "Down" input based on your P1/P2 logic
        boolean isDownPressed = (name.equals("P1")) ? keyMap[java.awt.event.KeyEvent.VK_S] : keyMap[keys[2] + 1]; // Adjusted for consistency

        // 4. GRAB / ITEM / THROW LOGIC (Consolidated)
        if (keyMap[keys[5]] && attackTimer <= 0 && !isShielding) 
        {
            if (heldItem != null) 
            {
                throwItems(keyMap, isDownPressed);
            } 
            else if (grabbedEnemy != null) 
            {
                handleThrows(keyMap, isDownPressed);
            } 
            else 
            {
                // Try to pick up item first
                Items nearby = null;
                if (items != null) 
                {
                    for (Items i : items) 
                    {
                        if (this.getBounds().intersects(i.getBounds()) && !i.isHeld) 
                        {
                            nearby = i;
                            break;
                        }
                    }
                }

                if (nearby != null) 
                {
                    heldItem = nearby;
                    heldItem.isHeld = true;
                    heldItem.owner = this;
                    attackTimer = 10;
                } 
                else 
                {
                    // Normal Fighter Grab
                    currentAttack = AttackType.GRAB;
                    attackTimer = 18;
                    hitTargets.clear();
                }
            }
            keyMap[keys[5]] = false; // Consume input
        }

        // 5. SPECIALS & SHIELDING
        if (keyMap[keys[3]] && attackTimer <= 0 && !isCharging && Math.abs(velY) < 1.0f) {
            isShielding = true;
            velX = 0;
        } else {
            isShielding = false;
        }

        if (hasFinalSmash && keyMap[keys[6]] && attackTimer <= 0 && !isShielding) {
            currentAttack = AttackType.FINAL_SMASH;
            attackTimer = 60; 
            hasFinalSmash = false; 
            hitTargets.clear();
            keyMap[keys[6]] = false;
        } else if (keyMap[keys[6]] && keyMap[keys[2]] && !isHelpless && !isShielding) {
            // Up Special (Recovery)
            currentAttack = AttackType.UP_SPECIAL;
            velY = -18f; 
            attackTimer = 25;
            isHelpless = true;
            keyMap[keys[2]] = false; 
            keyMap[keys[6]] = false;
        }

        // 6. MOVEMENT & JUMPING
        if (!isBeingHeld)
        {
            if (hitstun > 0) hitstun--;
            {
                float speed = isHelpless ? walkSpeed * 0.6f : walkSpeed;
                if (keyMap[keys[0]]) { velX = -speed; facingDir = -1; }
                else if (keyMap[keys[1]]) { velX = speed; facingDir = 1; }
                else { velX *= 0.8f; }

                if (keyMap[keys[2]] && jumpsLeft > 0 && !isHelpless && attackTimer <= 5) 
                {
                    velY = jumpForce;
                    jumpsLeft--;
                    keyMap[keys[2]] = false;
                    currentAttack = AttackType.NONE;
                }
            }
        }

        // 7. ATTACK CHARGING
        if (keyMap[keys[4]] && attackTimer <= 0 && !isCharging && !isShielding) {
            isCharging = true;
            chargeFrames = 0;
            if (keyMap[keys[2]]) { currentAttack = AttackType.UP; keyMap[keys[2]] = false; }
            else if (isDownPressed) currentAttack = AttackType.DOWN;
            else if (keyMap[keys[0]] || keyMap[keys[1]]) currentAttack = AttackType.SIDE;
            else currentAttack = AttackType.NEUTRAL;
        }

        if (isCharging) {
            if (keyMap[keys[4]] && chargeFrames < MAX_CHARGE) {
                chargeFrames++;
                // Slight air drift while charging
                if (keyMap[keys[0]]) velX = -walkSpeed * 0.3f;
                if (keyMap[keys[1]]) velX = walkSpeed * 0.3f;
            } else {
                isCharging = false;
                attackTimer = 22;
                chargeMultiplier = 1.0f + ((float)chargeFrames / MAX_CHARGE);
                keyMap[keys[4]] = false;
            }
        }

        // 8. PHYSICS & COLLISION
        velY += gravity;
        x += velX;
        y += velY;

        for (Platform p : platforms) 
        {
            // Ledge detection
            if (velY > 0 && !ledgeGrabbed && !isShielding) 
            {
                if (Math.abs(x + width - p.x) < 15 && Math.abs(y - p.y) < 20) 
                {
                    ledgeGrabbed = true; x = p.x - width; y = p.y; velY = 0; return;
                }
                if (Math.abs(x - (p.x + p.width)) < 15 && Math.abs(y - p.y) < 20) 
                {
                    ledgeGrabbed = true; x = p.x + p.width; y = p.y; velY = 0; return;
                }
            }

            // Floor collision
            if (velY > 0 && x + width > p.x && x < p.x + p.width &&
                y + height >= p.y && y + height <= p.y + p.height + velY) 
            {
                y = p.y - height; velY = 0; jumpsLeft = maxJumps;
                isHelpless = false; 
            }
        }

        if (attackTimer > 0) 
        {
            attackTimer--;
            if (attackTimer == 0) currentAttack = AttackType.NONE;
        }
    }

    private void handleThrows(boolean[] keyMap, boolean isDownPressed) 
    {
        float tx = 0, ty = 0;
        if (keyMap[keys[0]] || keyMap[keys[1]]) { tx = facingDir * 14; ty = -4; }
        else if (keyMap[keys[2]]) { tx = 0; ty = -16; }
        else if (isDownPressed) { tx = 0; ty = 12; }
        else { tx = facingDir * 10; ty = -2; } // Default forward throw

        grabbedEnemy.velX = tx; 
        grabbedEnemy.velY = ty;
        grabbedEnemy.isBeingHeld = false;
        grabbedEnemy.hitstun = 20;
        grabbedEnemy = null;
        attackTimer = 15;
    }

    private void throwItems(boolean[] keyMap, boolean isDownPressed) 
    {   
        heldItem.isHeld = false;
        heldItem.isThrown = true;
        heldItem.owner = this;
        heldItem.x = (facingDir == 1) ? this.x + width + 5 : this.x - heldItem.width - 5;

        if (keyMap[keys[2]]) { heldItem.velX = 0; heldItem.velY = -18; }
        else if (isDownPressed) { heldItem.velX = facingDir * 2; heldItem.velY = 10; }
        else { heldItem.velX = facingDir * 18; heldItem.velY = -4; }

        heldItem = null;
        attackTimer = 15;
    }

    public Rectangle getBounds() { return new Rectangle((int)x, (int)y, width, height); }

    public Rectangle getHitbox() {
        if (attackTimer <= 0 || currentAttack == AttackType.NONE) return null; 
        int hx, hy, hw, hh;
        switch (currentAttack) {
            case GRAB: hx = (facingDir == 1) ? (int)x + width : (int)x - 35; hy = (int)y + 20; hw = 35; hh = 40; break;
            case UP: hx = (int)x - 15; hy = (int)y - 50; hw = width + 30; hh = 60; break;
            case DOWN: hx = (int)x - 10; hy = (int)y + height; hw = width + 20; hh = 40; break;
            case UP_SPECIAL: hx = (int)x - 5; hy = (int)y - 10; hw = width + 10; hh = height + 10; break;
            case SIDE: hx = (facingDir == 1) ? (int)x + width : (int)x - 80; hy = (int)y + 20; hw = 80; hh = 40; break;
            case FINAL_SMASH: hx = (int)x - 150; hy = (int)y - 150; hw = 350; hh = 350; break;
            default: return null;
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
        
        if (isCharging) {
            g.setColor(Color.WHITE);
            g.fillRect((int)x, (int)y - 15, (int)((float)width * ((float)chargeFrames / MAX_CHARGE)), 5);
        }
        
        Rectangle hb = getHitbox();
        if (hb != null) {
            if (currentAttack == AttackType.FINAL_SMASH) g.setColor(new Color(255, 255, 0, 200));
            else if (currentAttack == AttackType.GRAB) g.setColor(new Color(0, 255, 255, 150));
            else g.setColor(new Color(255, 255, 0, 150));
            g.fill(hb);
        }
    }
}