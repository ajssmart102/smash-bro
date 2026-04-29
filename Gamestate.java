import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List; 

public class Gamestate {
    public List<Fighter> fighters = new ArrayList<>();
    public List<Platform> platforms = new ArrayList<>();
    public List<HitEffect> effects = new ArrayList<>();
    public boolean[] keys = new boolean[65536];

    public SmashBall smashBall;
    private int respawnTimer = 0;
    private final int RESPAWN_DELAY = 600; 

    // FIXED: Now accepts P1 choice, P2 choice, and the Map object
    public void setupSession(String p1Char, String p2Char, MapData chosenMap) {
        fighters.clear();
        platforms.clear();
        effects.clear();

        // 1. Load map platforms from the MapData object
        if (chosenMap != null && chosenMap.platforms != null) {
            this.platforms.addAll(chosenMap.platforms);
        } else {
            platforms.add(new Platform(200, 500, 880, 30)); // Fallback floor
        }
        
        // 2. Define Controls
        // P1: A, D, W, C, F, G, V
        int[] p1Keys = {KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_W, KeyEvent.VK_C, KeyEvent.VK_F, KeyEvent.VK_G, KeyEvent.VK_V};
        
        // P2: Left, Right, Up, M, L, K, PERIOD
        int[] p2Keys = {KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_UP, KeyEvent.VK_M, KeyEvent.VK_L, KeyEvent.VK_K, KeyEvent.VK_PERIOD};

        // 3. Add Fighters using the names selected from the menu
        Fighter player1 = new Fighter(300, 300, p1Char, Color.BLUE, p1Keys);
        Fighter player2 = new Fighter(880, 300, p2Char, Color.RED, p2Keys);
        
        // Apply unique stats based on character name (Example)
        applyStats(player1, p1Char);
        applyStats(player2, p2Char);

        fighters.add(player1);
        fighters.add(player2);
    }

    // Helper to make different characters feel unique
    private void applyStats(Fighter f, String charName) {
        switch (charName) {
            case "Tank": f.weight = 1.3f; f.walkSpeed = 3.5f; break;
            case "Speedster": f.weight = 0.7f; f.walkSpeed = 8.0f; break;
            case "Floaty": f.gravity = 0.15f; break;
        }
    }

    public void update() {
        // 1. Update Fighters & Blast Zones
        for (Fighter f : fighters) {
            f.update(keys, platforms);
            // Blast Zone detection
            if (f.y > 850 || f.y < -600 || f.x < -200 || f.x > 1480) {
                if (f.stocks > 0) f.respawn(640, 300);
            }
        }

        // 2. SmashBall Logic
        if (smashBall == null || smashBall.isBroken) {
            respawnTimer++;
            if (respawnTimer >= RESPAWN_DELAY) {
                smashBall = new SmashBall(640, 200);
                respawnTimer = 0;
            }
        } else {
            smashBall.update(1280, 720); 

            for (Fighter f : fighters) {
                Rectangle hb = f.getHitbox();
                if (hb != null && hb.intersects(smashBall.getHitbox())) {
                    if (f.currentAttack != Fighter.AttackType.NONE) {
                        smashBall.health -= 2; 
                        if (smashBall.health <= 0) {
                            smashBall.isBroken = true;
                            f.hasFinalSmash = true;
                            // Blast everyone else away
                            for (Fighter target : fighters) {
                                if (target != f) {
                                    target.velY = -12.0f;
                                    target.velX = (target.x > smashBall.x) ? 18.0f : -18.0f;
                                    effects.add(new HitEffect((int)target.x, (int)target.y));
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Combat Logic
        for (Fighter attacker : fighters) {
            if (attacker.isBeingHeld || attacker.ledgeGrabbed) continue;
            Rectangle hb = attacker.getHitbox();
            if (hb != null) {
                for (Fighter victim : fighters) {
                    if (attacker == victim) continue;
                    
                    // Logic to ensure hit only registers once per attack animation
                    if (hb.intersects(victim.getBounds()) && !attacker.hitTargets.contains(victim)) {
                        handleHit(attacker, victim);
                    }
                }
            }
        }

        // 4. Effects Cleanup
        for (HitEffect e : effects) e.life--; 
        effects.removeIf(e -> e.life <= 0);
    }

    private void handleHit(Fighter attacker, Fighter victim) {
        if (attacker.currentAttack == Fighter.AttackType.GRAB) {
            attacker.grabbedEnemy = victim;
            victim.isBeingHeld = true;
            victim.isShielding = false;
        } else if (victim.isShielding && attacker.currentAttack != Fighter.AttackType.FINAL_SMASH) {
            attacker.velX = -attacker.facingDir * 6; // Shield push-back
        } else {
            if (attacker.currentAttack == Fighter.AttackType.FINAL_SMASH) {
                victim.damage += 50;
                victim.velY = -22.0f; 
                victim.velX = attacker.facingDir * 20.0f;
            } else {
                // Standard Attack Calculation
                victim.damage += (12 * attacker.chargeMultiplier);
                victim.ledgeGrabbed = false;
                
                if (attacker.currentAttack == Fighter.AttackType.DOWN) {
                    victim.velY = 14.0f; // Spike effect
                } else {
                    victim.velX = attacker.facingDir * (5 + victim.damage/8);
                    victim.velY = -10.0f;
                }
            }
            effects.add(new HitEffect((int)victim.x, (int)victim.y));
        }
        attacker.hitTargets.add(victim);
    }
}