import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List;

public class Gamestate {
    public static final int STAGE_LEFT   = 100;
    public static final int STAGE_RIGHT  = 1180;
    public static final int STAGE_TOP    = 400;   // platform surface Y
    public static final int STAGE_FLOOR  = 410;   // bottom of platform
    public static final float GRAVITY    = 0.5f;
    public static final float BLAST_ZONE_LEFT   = -200;
    public static final float BLAST_ZONE_RIGHT  = 1480;
    public static final float BLAST_ZONE_BOTTOM = 900;
    public static final float BLAST_ZONE_TOP    = -200;

    public List<Fighter> fighters = new ArrayList<>();
    public List<Platform> platforms = new ArrayList<>();
    public List<HitEffect> effects = new ArrayList<>();

    // Input state (simple 2-player keyboard)
    public boolean[] keys = new boolean[65536]; // KeyEvent codes can exceed 255

    public GameState() {
        setupPlatforms();
        setupFighters();
    }

    private void setupPlatforms() {
        // Main platform
        platforms.add(new Platform(200, 500, 880, 20));
        // Side platforms
        platforms.add(new Platform(150, 380, 280, 15));
        platforms.add(new Platform(850, 380, 280, 15));
        // Top platform
        platforms.add(new Platform(490, 280, 300, 15));
    }

    private void setupFighters() {
        // Player 1: WASD + F to attack
        Fighter p1 = new Fighter(400, 400, Color.decode("#3A86FF"), "P1",
                new int[]{KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_W, KeyEvent.VK_F});
        // Player 2: Arrow keys + L to attack
        Fighter p2 = new Fighter(800, 400, Color.decode("#FF006E"), "P2",
                new int[]{KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_UP, KeyEvent.VK_L});
        fighters.add(p1);
        fighters.add(p2);
    }

    public void update() {
        // Update all fighters
        for (Fighter f : fighters) {
            f.handleInput(keys);
            f.applyGravity(GRAVITY);
            f.move();
            f.collideWithPlatforms(platforms);
            f.checkBlastZone(BLAST_ZONE_LEFT, BLAST_ZONE_RIGHT, BLAST_ZONE_BOTTOM, BLAST_ZONE_TOP);
        }

        // Check attacks hitting opponents
        for (int i = 0; i < fighters.size(); i++) {
            Fighter attacker = fighters.get(i);
            for (int j = 0; j < fighters.size(); j++) {
                if (i == j) continue;
                Fighter target = fighters.get(j);
                if (attacker.isAttacking() && attacker.getHitbox() != null
                        && attacker.getHitbox().intersects(target.getBounds())) {
                    if (!target.isHitstun() && !attacker.hasAlreadyHit(target)) {
                        target.receiveHit(attacker.getAttackDamage(), attacker.getKnockback(target));
                        attacker.markHit(target);
                        effects.add(new HitEffect(
                                (int) target.x + target.width / 2,
                                (int) target.y + target.height / 2));
                    }
                }
            }
        }

        // Update effects
        effects.removeIf(e -> !e.isAlive());
        effects.forEach(HitEffect::update);
    }
}
