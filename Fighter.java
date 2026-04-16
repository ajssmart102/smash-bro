import java.awt.*;
import java.util.*;

public class Fighter {
    // Change to protected so subclasses (like Mario) can access them
    protected float x, y;
    protected int width = 50, height = 70;
    protected float velX = 0, velY = 0;
    protected Color color;
    protected String name;
    protected int facingDir = 1;

    // Movement Stats (These can now be overridden)
    protected float walkSpeed = 8f;
    protected float jumpForce = -14f;
    protected int maxJumps = 2;
    protected int jumpsLeft = maxJumps;

    // Combat Stats
    protected float damage = 0;
    protected int stocks = 3;
    protected int attackTimer = 0;
    protected int attackCooldown = 0;
    protected Set<Fighter> hitThisAttack = new HashSet<>();
    protected int hitstunTimer = 0;

    // New: Unique Move Stats
    protected int attackDuration = 15;
    protected int attackCooldownMax = 25;
    protected float baseDamage = 8f;

    private int[] keyBindings;
    public boolean onGround = false;
    private int respawnTimer = 0;
    private static final float SPAWN_X = 640, SPAWN_Y = 200;

    public Fighter(float x, float y, Color color, String name, int[] keyBindings) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.name = name;
        this.keyBindings = keyBindings;
    }

    public void handleInput(boolean[] keys) {
        if (respawnTimer > 0 || hitstunTimer > 0) return;

        // Horizontal movement using walkSpeed variable
        if (keys[keyBindings[0]]) { velX -= walkSpeed; facingDir = -1; }
        if (keys[keyBindings[1]]) { velX += walkSpeed; facingDir = 1; }

        // Jump using jumpForce variable
        if (keys[keyBindings[2]] && jumpsLeft > 0) {
            velY = jumpForce;
            jumpsLeft--;
            onGround = false;
            keys[keyBindings[2]] = false; 
        }

        // Attack trigger
        if (keys[keyBindings[3]] && attackTimer == 0 && attackCooldown == 0) {
            startAttack();
        }
    }

    // Overridable attack logic
    protected void startAttack() {
        attackTimer = attackDuration;
        hitThisAttack.clear();
    }

    public void move() {
        if (respawnTimer > 0) { respawnTimer--; return; }
        if (hitstunTimer > 0) hitstunTimer--;

        float friction = onGround ? 0.75f : 0.92f;
        velX *= friction;
        velX = Math.max(-10, Math.min(10, velX));

        x += velX;
        y += velY;

        if (attackTimer > 0) attackTimer--;
        if (attackCooldown > 0) attackCooldown--;
        if (attackTimer == 0 && attackCooldown == 0 && !hitThisAttack.isEmpty()) {
            attackCooldown = attackCooldownMax;
            hitThisAttack.clear();
        }
    }

    // --- Getters for unique character traits ---
    public float getAttackDamage() { return baseDamage; }

    public float[] getKnockback(Fighter target) {
        return new float[]{facingDir * 6f, -8f};
    }

    public Rectangle getHitbox() {
        if (attackTimer <= 0) return null;
        int hx = facingDir == 1 ? (int) x + width : (int) x - 55;
        return new Rectangle(hx, (int) y + 10, 55, 50);
    }
    
    // Existing methods (collideWithPlatforms, draw, etc.) remain the same...
    // [Keep your existing collideWithPlatforms, checkBlastZone, loseStock, receiveHit, and draw methods here]
}
