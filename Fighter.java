import java.awt.*;
import java.util.*;

public class Fighter 
{
    public float x, y, velX, velY;
    public int width = 50, height = 80;
    public String name;
    public Color color;
    public int facingDir = 1;

    // --- CHARACTER STATS ---
    public CharacterStats stats;
    public float walkSpeed;
    public float jumpForce;
    public float gravity;
    public float weight; 
    public float attackDamageMultiplier;

    public float maxHealth = 100f;
    public float currentHealth = 100f;
    
    protected int maxJumps = 2;
    protected int jumpsLeft = 2;

    // --- COMBAT & PERCENTAGE ---
    public float damage = 0; 
    public int stocks = 3;
    protected int attackTimer = 0;
    protected Set<Fighter> hitTargets = new HashSet<>();
    public boolean hasFinalSmash = false;
    
    // --- STATES ---
    public boolean isHelpless = false;    
    public boolean ledgeGrabbed = false; 

    public enum AttackType { NONE, NEUTRAL, SIDE, UP, DOWN, GRAB, UP_SPECIAL, FINAL_SMASH }
    protected AttackType currentAttack = AttackType.NONE;

    public Fighter grabbedEnemy = null; 
    public boolean isBeingHeld = false;
    public boolean isShielding = false;

    // --- ITEM SYSTEM INTEGRATION ---
    public ThrowableItem heldItem = null; // Changed from Object to ThrowableItem for compiler type-safety
    public boolean isHoldingItem = false;

    // --- INPUT SAFETY STATE ---
    private boolean grabKeyWasPressed = false; // Tracks historical keyboard state to prevent holding down spam
    
    // --- GRAB EXPLOIT PREVENTION SYSTEM ---
    protected int grabHoldFrames = 0;           // Tracks how long an enemy has been held
    protected final int MIN_HOLD_TIME = 20;     // Prevents instant frame-1 buffering exploits
    protected final int MAX_HOLD_TIME = 90;     // Automatically releases enemy if attacker stalls too long
    protected int pummelCooldown = 0;           // Controls pacing of pummels while holding

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
        
        this.name = stats.name;
        this.walkSpeed = stats.walkSpeed;
        this.jumpForce = stats.jumpForce;
        this.gravity = stats.gravity;
        this.weight = stats.weight;
        this.attackDamageMultiplier = stats.dm;
    }

    public void applyKnockback(float launchX, float launchY) {
        float knockbackMultiplier = 1.0f + (this.damage / 60.0f);
        float weightFactor = 2.0f - weight; 

        this.velX = launchX * knockbackMultiplier * weightFactor;
        this.velY = launchY * knockbackMultiplier * weightFactor;
        
        this.ledgeGrabbed = false;
        this.isShielding = false;
        this.isCharging = false;
        
        // Drop item if hit with heavy knockback
        if (Math.abs(velX) > 4 || Math.abs(velY) > 4) {
            dropItem();
        }
    }

    public void respawn(float startX, float startY) {
        this.x = startX; this.y = startY;
        this.velX = 0; this.velY = 0;
        this.stocks--;
        this.damage = 0;
        this.jumpsLeft = maxJumps;
        this.isHelpless = false;
        this.hasFinalSmash = false;
        this.currentAttack = AttackType.NONE;
        dropItem();
    }

    public void update(boolean[] keyMap, java.util.List<Platform> platforms, java.util.List<ThrowableItem> items)
    {
        if (isBeingHeld) { velX = 0; velY = 0; return; }

        // Keep item snapped to player's position if holding one
        if (isHoldingItem && heldItem != null) {
            // Update your heldItem's x/y properties here if your Item class requires it manually
            // e.g., heldItem.x = this.x + (facingDir * 15);
        }

        if (grabbedEnemy != null) {
            grabbedEnemy.x = this.x + (this.facingDir * 40);
            grabbedEnemy.y = this.y;
            
            grabHoldFrames++;
            if (pummelCooldown > 0) pummelCooldown--;

            handleThrows(keyMap);
            
            if (grabbedEnemy != null && grabHoldFrames >= MAX_HOLD_TIME) {
                grabbedEnemy.isBeingHeld = false;
                grabbedEnemy.applyKnockback(facingDir * -4, -3); 
                grabbedEnemy = null;
                attackTimer = 25; 
                return;
            }
            
            if (grabbedEnemy != null) {
                if (keyMap[keys[0]]) { x -= 2; facingDir = -1; }
                if (keyMap[keys[1]]) { x += 2; facingDir = 1; }
            }
            return; 
        }

        // Ledge logic
        if (ledgeGrabbed) {
            velX = 0; velY = 0;
            if (keyMap[keys[2]]) { 
                velY = jumpForce; 
                ledgeGrabbed = false; 
                isHelpless = false; 
                jumpsLeft = maxJumps - 1; 
            }
            boolean isDown = (keys[0] == 65) ? keyMap[83] : keyMap[40];
            if (isDown) {
                ledgeGrabbed = false;
                velY = 2; 
            }
            return;
        }

        // Shielding
        if (keyMap[keys[3]] && attackTimer <= 0 && !isCharging && Math.abs(velY) < 1.5f) {
            isShielding = true; velX = 0;
        } else {
            isShielding = false;
        }

        // Movement
        if (attackTimer <= 0 && !isShielding) {
            float speed = isHelpless ? walkSpeed * 0.6f : walkSpeed;
            if (keyMap[keys[0]]) { velX = -speed; facingDir = -1; }
            else if (keyMap[keys[1]]) { velX = speed; facingDir = 1; }
            else { velX *= 0.8f; }
        } else {
            velX *= 0.85f;
        }

        // Jump
        if (keyMap[keys[2]] && jumpsLeft > 0 && attackTimer <= 5 && !isShielding && !isHelpless) {
            velY = jumpForce;
            jumpsLeft--;
            keyMap[keys[2]] = false; 
        }

        handleAttackInputs(keyMap, items);

        // Physics
        velY += (gravity * weight);
        x += velX; 
        y += velY;

        for (Platform p : platforms) {
            if (velY > 0 && !ledgeGrabbed && p.isMainStage) {
                if (Math.abs(x + width - p.x) < 20 && Math.abs(y - p.y) < 30) { 
                    ledgeGrabbed = true; x = p.x - width; y = p.y; 
                    isHelpless = false; return; 
                }
                if (Math.abs(x - (p.x + p.width)) < 20 && Math.abs(y - p.y) < 30) { 
                    ledgeGrabbed = true; x = p.x + p.width; y = p.y; 
                    isHelpless = false; return; 
                }
            }

            if (velY >= 0 && x + width > p.x && x < p.x + p.width && y + height >= p.y && y + height <= p.y + p.height + velY + 2) {
                y = p.y - height; velY = 0; jumpsLeft = maxJumps; isHelpless = false;
                x += p.velX; y += p.velY; 
            }
        }

        if (attackTimer > 0) {
            attackTimer--;
            if (attackTimer == 0) currentAttack = AttackType.NONE;
        }
    }

    private void handleAttackInputs(boolean[] keyMap, java.util.List<ThrowableItem> items)
    {
        // keys[5] is your GRAB key
        if (keyMap[keys[5]] && heldItem == null && attackTimer <= 0) 
        {
            for (ThrowableItem item : items)
            {
                if (!item.isHeld && getBounds().intersects(item.getBounds())) 
                {
                    heldItem = item;
                    item.isHeld = true;
                    isHoldingItem = true; // Set this true since we successfully picked it up
                    attackTimer = 10;
                    return;
                }
            }
        }

        // keys[4] is your ATTACK key
        // If holding an item, pressing Attack throws it
        if (keyMap[keys[4]] && heldItem != null && attackTimer <= 0) 
        {
            heldItem.isHeld = false;
            heldItem.isFlying = true;
            heldItem.thrower = this;
            
            // Use your facingDir to determine the horizontal launch
            heldItem.velX = facingDir * 15f; 
            heldItem.velY = -4f; // Slight arc

            heldItem = null;
            isHoldingItem = false;
            attackTimer = 15; // Animation lag for the throw
        }

        if (keyMap[keys[6]] && attackTimer <= 0 && !isShielding) 
        {
            if (hasFinalSmash) 
            {
                currentAttack = AttackType.FINAL_SMASH;
                attackTimer = 60; hasFinalSmash = false;
            } 
            else if (keyMap[keys[2]]) 
            {
                currentAttack = AttackType.UP_SPECIAL;
                velY = -18f; attackTimer = 25; isHelpless = true;
            }
            hitTargets.clear();
        }

        // Check for Item Throwing instead of standard attacking if an item is equipped
        if (keyMap[keys[4]] && attackTimer <= 0 && !isShielding) {
            if (isHoldingItem) {
                boolean isDown = (keys[0] == 65) ? keyMap[83] : keyMap[40];
                if (keyMap[keys[2]]) throwItem(0, -15);         // Up Throw
                else if (isDown) throwItem(0, 12);               // Down Throw
                else if (keyMap[keys[0]]) throwItem(-15, -2);    // Left Throw
                else if (keyMap[keys[1]]) throwItem(15, -2);     // Right Throw
                else throwItem(facingDir * 14, -3);              // Forward Throw Neutral
                return;
            }

            if (!isCharging) {
                isCharging = true; chargeFrames = 0;
                boolean isDown = (keys[0] == 65) ? keyMap[83] : keyMap[40];
                if (keyMap[keys[2]]) currentAttack = AttackType.UP;
                else if (isDown) currentAttack = AttackType.DOWN;
                else if (keyMap[keys[0]] || keyMap[keys[1]]) currentAttack = AttackType.SIDE;
                else currentAttack = AttackType.NEUTRAL;
            }
        }

        if (isCharging) 
        {
            if (keyMap[keys[4]] && chargeFrames < MAX_CHARGE) chargeFrames++;
            else 
            {
                isCharging = false; attackTimer = 22; hitTargets.clear();
                chargeMultiplier = 1.0f + ((float)chargeFrames / MAX_CHARGE);
            }
        }

        // --- SECURE GRAB & PUMMEL LOGIC ---
        if (keyMap[keys[5]]) {
            if (!grabKeyWasPressed && attackTimer <= 0 && !isCharging && !isShielding) {
                if (grabbedEnemy == null) {
                    currentAttack = AttackType.GRAB; 
                    attackTimer = 18; 
                    grabHoldFrames = 0; 
                    hitTargets.clear(); 
                } else if (pummelCooldown <= 0) {
                    grabbedEnemy.damage += (2 * attackDamageMultiplier);
                    pummelCooldown = 20; 
                }
            }
            grabKeyWasPressed = true; 
        } else {
            grabKeyWasPressed = false; 
        }
    }

    private void handleThrows(boolean[] keyMap) {
        if (grabbedEnemy == null) return;
        if (grabHoldFrames < MIN_HOLD_TIME) return;

        boolean throwTriggered = false;
        float tx = 0, ty = 0;
        
        // Dynamically compute downKeyIndex matching update logic
        int downKeyIndex = (keys[0] == 65) ? 83 : 40; 
        boolean isDown = keyMap[downKeyIndex]; // Fixed the missing 'isDown' definition error

        if (keyMap[keys[0]]) { 
            tx = -14; ty = -4; 
            throwTriggered = true; 
        } else if (keyMap[keys[1]]) { 
            tx = 14; ty = -4; 
            throwTriggered = true; 
        } else if (keyMap[keys[2]]) { 
            tx = 0; ty = -18; 
            throwTriggered = true; 
        } else if (isDown) { 
            tx = 0; ty = 14; 
            throwTriggered = true; 
        }
        
        if (throwTriggered) {
            Fighter victim = this.grabbedEnemy;
            this.grabbedEnemy = null;
            
            victim.isBeingHeld = false;
            victim.applyKnockback(tx, ty);
            victim.damage += (8 * this.attackDamageMultiplier);
            
            this.attackTimer = 25; 
            this.currentAttack = AttackType.NONE;
            this.grabKeyWasPressed = true; 
        }
    }

    // --- ITEM UTILITY FUNCTIONS ---
    public void pickUpItem(ThrowableItem item) { // Type updated to match ThrowableItem
        if (!isHoldingItem && grabbedEnemy == null) {
            this.heldItem = item;
            this.isHoldingItem = true;
        }
    }

    public void dropItem() {
        if (isHoldingItem && heldItem != null) {
            this.heldItem = null;
            this.isHoldingItem = false;
        }
    }

    private void throwItem(float launchX, float launchY) {
        if (isHoldingItem && heldItem != null) {
            this.heldItem = null;
            this.isHoldingItem = false;
            this.attackTimer = 15; 
        }
    }

    public Rectangle getBounds() { return new Rectangle((int)x, (int)y, width, height); }

    public Rectangle getHitbox() 
    {
        if (attackTimer <= 0 || currentAttack == AttackType.NONE || isCharging) return null; 
        int hx, hy, hw, hh;
        switch (currentAttack) 
        {
            case GRAB: hx = (facingDir == 1) ? (int)x + width : (int)x - 35; hy = (int)y + 20; hw = 35; hh = 40; break;
            case UP: hx = (int)x - 15; hy = (int)y - 50; hw = width + 30; hh = 60; break;
            case DOWN: hx = (int)x - 10; hy = (int)y + height; hw = width + 20; hh = 40; break;
            case SIDE: hx = (facingDir == 1) ? (int)x + width : (int)x - 80; hy = (int)y + 20; hw = 80; hh = 40; break;
            case UP_SPECIAL: hx = (int)x - 5; hy = (int)y - 10; hw = width + 10; hh = height + 10; break;
            case FINAL_SMASH: hx = (int)x - 150; hy = (int)y - 150; hw = 350; hh = 350; break; 
            default: hx = (facingDir == 1) ? (int)x + width : (int)x - 50; hy = (int)y + 20; hw = 50; hh = 40; break;
        }
        return new Rectangle(hx, hy, hw, hh);
    }

    public void draw(Graphics2D g) 
    {
        if (isShielding) 
        {
            g.setColor(new Color(100, 200, 255, 120));
            g.fillOval((int)x - 15, (int)y - 5, width + 30, height + 10);
        }

        if (hasFinalSmash) 
        {
            g.setColor(new Color(255, 255, 0, 50));
            g.fillOval((int)x - 10, (int)y - 10, width + 20, height + 20);
        }

        g.setColor(isHelpless ? Color.DARK_GRAY : color);
        g.fillRoundRect((int)x, (int)y, width, height, 15, 15);

        if (isCharging) 
        {
            g.setColor(Color.WHITE);
            g.fillRect((int)x, (int)y - 15, (int)((float)width * ((float)chargeFrames / MAX_CHARGE)), 6);
            g.setColor(Color.BLACK);
            g.drawRect((int)x, (int)y - 15, width, 6);
        }

        Rectangle hb = getHitbox();
        if (hb != null) 
        {
            if (currentAttack == AttackType.FINAL_SMASH) g.setColor(new Color(255, 255, 0, 180));
            else if (currentAttack == AttackType.GRAB) g.setColor(new Color(0, 255, 255, 150));
            else g.setColor(new Color(255, 255, 0, 130)); 
            g.fill(hb);
        }
    }
}