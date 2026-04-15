import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List;
 
public class Gamestate {
    public static final int STAGE_LEFT   = 100;
    public static final int STAGE_RIGHT  = 1180;
    public static final int STAGE_TOP    = 400;
    public static final int STAGE_FLOOR  = 410;
    public static final float GRAVITY    = 0.5f;
    public static final float BLAST_ZONE_LEFT   = -200;
    public static final float BLAST_ZONE_RIGHT  = 1480;
    public static final float BLAST_ZONE_BOTTOM = 900;
    public static final float BLAST_ZONE_TOP    = -200;
 
    public List<Fighter> fighters = new ArrayList<>();
    public List<Platform> platforms = new ArrayList<>();
    public List<HitEffect> effects = new ArrayList<>();
 
    public boolean[] keys = new boolean[65536];
 
    private int currentMap = 0;
 
    public Gamestate() {
        // Default: load map 0 until setMap() is called
        setupPlatforms(0);
        setupFighters();
    }
 
    // Called by Gamewindow after the player picks a map
    public void setMap(int mapIndex) {
        currentMap = mapIndex;
        platforms.clear();
        setupPlatforms(mapIndex);
    }
 
    public int getMap() {
        return currentMap;
    }
 
    private void setupPlatforms(int mapIndex) {
        switch (mapIndex) {
            case 0: // Final Destination — flat, no side platforms
                platforms.add(new Platform(200, 500, 880, 20));
                break;
 
            case 1: // Battlefield — main + three floating platforms
                platforms.add(new Platform(200, 500, 880, 20));
                platforms.add(new Platform(150, 380, 280, 15));
                platforms.add(new Platform(850, 380, 280, 15));
                platforms.add(new Platform(490, 280, 300, 15));
                break;
 
            case 2: // Lava Ruins — asymmetric platforms
                platforms.add(new Platform(150, 520, 980, 20));
                platforms.add(new Platform(100, 380, 200, 15));
                platforms.add(new Platform(900, 360, 200, 15));
                platforms.add(new Platform(480, 260, 320, 15));
                break;
 
            case 3: // Sky Temple — wide main, high platforms
                platforms.add(new Platform(180, 520, 920, 20));
                platforms.add(new Platform(200, 350, 200, 15));
                platforms.add(new Platform(880, 350, 200, 15));
                platforms.add(new Platform(440, 220, 400, 15));
                break;
 
            case 4: // Neon City — staggered urban layout
                platforms.add(new Platform(200, 500, 880, 20));
                platforms.add(new Platform(150, 400, 220, 15));
                platforms.add(new Platform(500, 340, 280, 15));
                platforms.add(new Platform(910, 400, 220, 15));
                break;
 
            case 5: // Frozen Peak — narrow main, sloped feel via platform placement
                platforms.add(new Platform(280, 500, 720, 20));
                platforms.add(new Platform(180, 390, 180, 15));
                platforms.add(new Platform(920, 390, 180, 15));
                break;
 
            default: // Fallback to Battlefield layout
                platforms.add(new Platform(200, 500, 880, 20));
                platforms.add(new Platform(150, 380, 280, 15));
                platforms.add(new Platform(850, 380, 280, 15));
                platforms.add(new Platform(490, 280, 300, 15));
                break;
        }
    }
 
    private void setupFighters() {
        Fighter p1 = new Fighter(400, 400, Color.decode("#3A86FF"), "P1",
                new int[]{KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_W, KeyEvent.VK_F});
        Fighter p2 = new Fighter(800, 400, Color.decode("#FF006E"), "P2",
                new int[]{KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_UP, KeyEvent.VK_L});
        fighters.add(p1);
        fighters.add(p2);
    }
 
    public void update() {
        for (Fighter f : fighters) {
            f.handleInput(keys);
            f.applyGravity(GRAVITY);
            f.move();
            f.collideWithPlatforms(platforms);
            f.checkBlastZone(BLAST_ZONE_LEFT, BLAST_ZONE_RIGHT, BLAST_ZONE_BOTTOM, BLAST_ZONE_TOP);
        }
 
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
 
        effects.removeIf(e -> !e.isAlive());
        effects.forEach(HitEffect::update);
    }
}
 
