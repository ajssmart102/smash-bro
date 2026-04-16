import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List;

public class Gamestate {
    // ... (Keep all your STAGE and BLAST_ZONE constants here) ...
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
        // We leave setupFighters out of the constructor now
        // because we want to wait for the user choice.
        setupPlatforms(0);
    }

    // UPDATED: Called by Gamewindow to start the game with the right data
    public void initSession(int mapIndex, String characterName) {
        this.currentMap = mapIndex;
        
        fighters.clear();
        platforms.clear();
        effects.clear();

        setupPlatforms(mapIndex);
        setupFighters(characterName);
    }

    private void setupPlatforms(int mapIndex) {
        // ... (Keep your existing switch case for platforms here) ...
        switch (mapIndex) {
            case 0: platforms.add(new Platform(200, 500, 880, 20)); break;
            // ... add the rest of your cases ...
            default: platforms.add(new Platform(200, 500, 880, 20)); break;
        }
    }

    // UPDATED: Now creates fighters based on the choice
    private void setupFighters(String p1Choice) {
        int[] p1Keys = {KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_W, KeyEvent.VK_F};
        int[] p2Keys = {KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_UP, KeyEvent.VK_L};

        // Logic to create P1 based on selection
        Fighter p1;
        if (p1Choice.equals("Tank")) {
            p1 = new TankFighter(400, 400, Color.decode("#3A86FF"), "P1 (Tank)", p1Keys);
        } else {
            // Default Fighter
            p1 = new Fighter(400, 400, Color.decode("#3A86FF"), "P1", p1Keys);
        }

        // You can add logic for P2 as well, or keep it as a standard fighter for now
        Fighter p2 = new Fighter(800, 400, Color.decode("#FF006E"), "P2", p2Keys);

        fighters.add(p1);
        fighters.add(p2);
    }

    public void update() {
        // ... (Keep your existing update() logic for movement and collision) ...
        for (Fighter f : fighters) {
            f.handleInput(keys);
            f.applyGravity(GRAVITY);
            f.move();
            f.collideWithPlatforms(platforms);
            f.checkBlastZone(BLAST_ZONE_LEFT, BLAST_ZONE_RIGHT, BLAST_ZONE_BOTTOM, BLAST_ZONE_TOP);
        }

        // Combat logic
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

    // Keep your getter/setter
    public void setMap(int mapIndex) { this.currentMap = mapIndex; setupPlatforms(mapIndex); }
    public int getMap() { return currentMap; }
}
