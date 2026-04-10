import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.*;

public class Fighter {
    // Position & size
    public float x, y;
    public int width = 50, height = 70;
    public float velX = 0, velY = 0;

    // Identity
    public Color color;
    public String name;
    public int stocks = 3;
    public float damage = 0;  // percentage

    // Movement constants
    private static final float WALK_SPEED   = 5f;
    private static final float JUMP_FORCE   = -14f;
    private static final float AIR_FRICTION = 0.85f;
    private static final float GROUND_FRICTION = 0.7f;
    private static final int   MAX_JUMPS    = 2;

    // State
    public boolean onGround = false;
    private int jumpsLeft = MAX_JUMPS;
    private int facingDir = 1;  // 1 = right, -1 = left

    // Attack state
    private int attackTimer = 0;
    private static final int ATTACK_DURATION = 15; // frames
    private static final int ATTACK_COOLDOWN  = 25;
    private int attackCooldown = 0;
    private Set<Fighter> hitThisAttack = new HashSet<>();

    // Hitstun
    private int hitstunTimer = 0;

    // Key bindings: [left, right, jump, attack]
    private int[] keyBindings;

    // For spawning after losing a stock
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

        // Horizontal movement
        if (keys[keyBindings[0]]) { velX -= WALK_SPEED; facingDir = -1; }
        if (keys[keyBindings[1]]) { velX += WALK_SPEED; facingDir = 1; }

        // Jump
        if (keys[keyBindings[2]] && jumpsLeft > 0) {
            velY = JUMP_FORCE;
            jumpsLeft--;
            onGround = false;
            keys[keyBindings[2]] = false; // consume key to prevent repeat
        }

        // Attack
        if (keys[keyBindings[3]] && attackTimer == 0 && attackCooldown == 0) {
            attackTimer = ATTACK_DURATION;
            hitThisAttack.clear();
        }
    }

    public void applyGravity(float gravity) {
        if (!onGround) velY += gravity;
    }

    public void move() {
        if (respawnTimer > 0) { respawnTimer--; return; }
        if (hitstunTimer > 0) hitstunTimer--;

        // Friction
        if (onGround) velX *= GROUND_FRICTION;
        else          velX *= AIR_FRICTION;

        // Clamp horizontal speed
        velX = Math.max(-10, Math.min(10, velX));

        x += velX;
        y += velY;

        // Timers
        if (attackTimer > 0) attackTimer--;
        if (attackCooldown > 0) attackCooldown--;
        if (attackTimer == 0 && attackCooldown == 0 && !hitThisAttack.isEmpty()) {
            attackCooldown = ATTACK_COOLDOWN;
            hitThisAttack.clear();
        }
    }

    public void collideWithPlatforms(java.util.List<Platform> platforms) {
        onGround = false;
        for (Platform p : platforms) {
            // Simple top-surface landing
            if (velY >= 0
                    && x + width > p.x && x < p.x + p.width
                    && y + height >= p.y && y + height <= p.y + p.height + velY + 2) {
                y = p.y - height;
                velY = 0;
                onGround = true;
                jumpsLeft = MAX_JUMPS;
            }
        }
    }

    public void checkBlastZone(float left, float right, float bottom, float top) {
        if (x + width < left || x > right || y > bottom || y + height < top) {
            loseStock();
        }
    }

    private void loseStock() {
        stocks--;
        damage = 0;
        velX = 0;
        velY = 0;
        x = SPAWN_X;
        y = SPAWN_Y;
        respawnTimer = 120;  // 2 seconds at 60fps
        hitstunTimer = 0;
    }

    public void receiveHit(float dmg, float[] knockback) {
        damage += dmg;
        velX = knockback[0] * (1 + damage / 50f);
        velY = knockback[1] * (1 + damage / 50f);
        hitstunTimer = (int)(20 + damage / 5f);
        onGround = false;
    }

    public boolean isAttacking() { return attackTimer > 0; }
    public boolean isHitstun()   { return hitstunTimer > 0; }
    public boolean hasAlreadyHit(Fighter target) { return hitThisAttack.contains(target); }
    public void markHit(Fighter target) { hitThisAttack.add(target); }

    public float getAttackDamage() { return 8f; }

    public float[] getKnockback(Fighter target) {
        float kbX = facingDir * 6f;
        float kbY = -8f;
        return new float[]{kbX, kbY};
    }

    public Rectangle getHitbox() {
        if (!isAttacking()) return null;
        // Hitbox extends in facing direction
        int hx = facingDir == 1 ? (int) x + width : (int) x - 55;
        return new Rectangle(hx, (int) y + 10, 55, 50);
    }

    public Rectangle getBounds() {
        return new Rectangle((int) x, (int) y, width, height);
    }

    public void draw(Graphics2D g) {
        if (respawnTimer > 0) {
            // Flicker during respawn invincibility
            if ((respawnTimer / 5) % 2 == 0) return;
        }

        // Body
        g.setColor(color);
        g.fillRoundRect((int) x, (int) y, width, height, 10, 10);

        // Face direction dot
        g.setColor(Color.WHITE);
        int eyeX = facingDir == 1 ? (int) x + width - 15 : (int) x + 10;
        g.fillOval(eyeX, (int) y + 15, 12, 12);

        // Hitbox (debug, semi-transparent)
        if (isAttacking() && getHitbox() != null) {
            g.setColor(new Color(255, 255, 0, 80));
            g.fill(getHitbox());
            g.setColor(Color.YELLOW);
            g.draw(getHitbox());
        }

        // Name tag
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 13));
        g.drawString(name, (int) x + 10, (int) y - 5);
    }
}
