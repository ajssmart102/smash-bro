import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List;

public class Gamestate {
    // Stage Constants
    public static final int STAGE_LEFT   = 100;
    public static final int STAGE_RIGHT  = 1180;
    public static final int STAGE_TOP    = 400;
    public static final int STAGE_FLOOR  = 410;
    public static final float GRAVITY    = 0.5f;
    public static final float BLAST_ZONE_LEFT   = -200;
    public static final float BLAST_ZONE_RIGHT  = 1480;
    public static final float BLAST_ZONE_BOTTOM = 900;
    public static final float BLAST_ZONE_TOP    = -200;

    // Game Objects
    public List<Fighter> fighters = new ArrayList<>();
    public List<Platform> platforms = new ArrayList<>();
    public List<HitEffect> effects = new ArrayList<>();
    public boolean[] keys = new boolean[65536];
    private int currentMap = 0;

    public Gamestate() {
        // Just a safety initialization
        setupPlatforms(0);
    }

    public void initSession(int mapIndex, String characterName) {
        this.currentMap = mapIndex;
        
        // Clear all old data
        fighters.clear();
        platforms.clear();
        effects.clear();

        setupPlatforms(mapIndex);
        setupFighters(characterName);
    }

    private void setupPlatforms(int mapIndex) {
        platforms.clear();
        switch (mapIndex) {
            case 0: // Final Destination
                platforms.add(new Platform(200, 500, 880, 20)); 
                break;
            case 1: // Battlefield
                platforms.add(new Platform(200, 500, 880, 20));
                platforms.add(new Platform(150, 380, 280, 15));
                platforms.add(new Platform(850, 380, 280, 15));
                platforms.add(new Platform(490, 280, 300, 15));
                break;
            default:
                platforms.add(new Platform(200, 500, 880, 20));
                break;
        }
    }

    private void setupFighters(String p1Choice) {
        int[] p1Keys = {KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_W, KeyEvent.VK_F};
        int[] p2Keys = {KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_UP, KeyEvent.VK_L};

        Fighter p1;
        // Check for null and use equalsIgnoreCase for safety
        if (p1Choice != null && p1Choice.equalsIgnoreCase("Tank")) {
            p1 = new TankFighter(400, 400, Color.decode("#3A86FF"), "P1", p1Keys);
        } else {
            p1 = new Fighter(400, 400, Color.decode("#3A86FF"), "P1", p1Keys);
        }

        Fighter p2 = new Fighter(800, 400, Color.decode("#FF006E"), "P2", p2Keys);

        fighters.add(p1);
        fighters.add(p2);
    }

    public void update() {
        // 1. Movement & Physics
        for (Fighter f : fighters) {
            f.handleInput(keys);
            f.applyGravity(GRAVITY);
            f.move();
            f.collideWithPlatforms(platforms);
            f.checkBlastZone(BLAST_ZONE_LEFT, BLAST_ZONE_RIGHT, BLAST_ZONE_BOTTOM, BLAST_ZONE_TOP);
        }

        // 2. Combat & Collision Detection
        for (int i = 0; i < fighters.size(); i++) {
            Fighter attacker = fighters.get(i);
            
            // CRITICAL FIX: Check if getHitbox() is null BEFORE calling intersects()
            Rectangle hitbox = attacker.getHitbox();
            
            if (attacker.isAttacking() && hitbox != null) {
                for (int j = 0; j < fighters.size(); j++) {
                    if (i == j) continue; 
                    
                    Fighter target = fighters.get(j);
                    
                    if (hitbox.intersects(target.getBounds())) {
                        if (!target.isHitstun() && !attacker.hasAlreadyHit(target)) {
                            target.receiveHit(attacker.getAttackDamage(), attacker.getKnockback(target));
                            attacker.markHit(target);
                            
                            // Center the hit effect on the target
                            effects.add(new HitEffect(
                                (int) (target.x + target.width / 2),
                                (int) (target.y + target.height / 2)));
                        }
                    }
                }
            }
        }

        // 3. Effects cleanup
        effects.removeIf(e -> !e.isAlive());
        for (HitEffect e : effects) {
            e.update();
        }
    }

    public void setMap(int mapIndex) { 
        this.currentMap = mapIndex; 
        setupPlatforms(mapIndex); 
    }
    
    public int getMap() { return currentMap; }
}
