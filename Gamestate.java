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
    private final int RESPAWN_DELAY = 600; // 10 seconds at 60fps

    // Fixed signature: Accepts P1 character and the chosen MapData
    public void setupSession(String p1Char, MapData chosenMap) {
        fighters.clear();
        platforms.clear();
        effects.clear();

        // Load map platforms
        if (chosenMap != null && chosenMap.platforms != null) {
            this.platforms.addAll(chosenMap.platforms);
        } else {
            platforms.add(new Platform(200, 500, 880, 30)); // Fallback
        }
        
        // P1: A, D, W, C, F, G, V (Index 6 is Special)
        int[] p1Keys = {KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_W, KeyEvent.VK_C, KeyEvent.VK_F, KeyEvent.VK_G, KeyEvent.VK_V};
        
        // P2: Left, Right, Up, M, L, K, PERIOD (Index 6 is Special)
        int[] p2Keys = {KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_UP, KeyEvent.VK_M, KeyEvent.VK_L, KeyEvent.VK_K, KeyEvent.VK_PERIOD};

        fighters.add(new Fighter(300, 300, p1Char, Color.BLUE, p1Keys));
        fighters.add(new Fighter(880, 300, "P2", Color.RED, p2Keys));
    }

public void update() {
    // 1. Update Fighters
    for (Fighter f : fighters) {
        f.update(keys, platforms);
        if (f.y > 850 || f.y < -600 || f.x < -200 || f.x > 1480) {
            if (f.stocks > 0) f.respawn(640, 300);
        }
    }

    // 2. SmashBall Logic (The missing part)
    if (smashBall == null || smashBall.isBroken) {
        respawnTimer++;
        if (respawnTimer >= RESPAWN_DELAY) {
            smashBall = new SmashBall(640, 200); // Spawn at center-top
            respawnTimer = 0;
        }
    } else {
        // Move the ball
        smashBall.update(1280, 720); 

        // Check for fighter hits on the SmashBall
        for (Fighter f : fighters) {
            Rectangle hb = f.getHitbox();
            if (hb != null && hb.intersects(smashBall.getHitbox())) {
                // If fighter is attacking, damage the ball
                if (f.currentAttack != Fighter.AttackType.NONE) {
                    smashBall.health -= 2; // Adjust damage as needed
                    if (smashBall.health <= 0) {
                        smashBall.isBroken = true;
                        f.hasFinalSmash = true;
                        // --- EXPLOSION DAMAGE (50 to everyone else) ---
                            for (Fighter target : fighters) {
                                if (target != f) {
                                    target.velY = -10.0f; // Launch up
                                    target.velX = (target.x > smashBall.x) ? 15.0f : -15.0f; // Push away from ball
                                    effects.add(new HitEffect((int)target.x, (int)target.y));
                                }
                    }
                }
            }
        }
    }
}
// 3. Combat Logic (FIXED: Bypassing hitTargets for Final Smash)
        for (Fighter attacker : fighters) {
            if (attacker.isBeingHeld || attacker.ledgeGrabbed) continue;
            Rectangle hb = attacker.getHitbox();
            if (hb != null) {
                for (Fighter victim : fighters) {
                    if (attacker == victim) continue;
                    
                    // The Fix: Check if Final Smash OR if they haven't been hit yet
                    boolean isFinalSmash = attacker.currentAttack == Fighter.AttackType.FINAL_SMASH;
                    boolean notAlreadyHit = !attacker.hitTargets.contains(victim);
                    
                   if (hb.intersects(victim.getBounds()) && !attacker.hitTargets.contains(victim)) {
                handleHit(attacker, victim);
                    }
                }
            }
        }

    // 4. Effects Logic (Existing)
    for (HitEffect e : effects) e.life--; 
    effects.removeIf(e -> e.life <= 0);
}

    private void handleHit(Fighter attacker, Fighter victim) {
        if (attacker.currentAttack == Fighter.AttackType.GRAB) {
            attacker.grabbedEnemy = victim;
            victim.isBeingHeld = true;
            victim.isShielding = false;
            victim.ledgeGrabbed = false;
        } else if (victim.isShielding && attacker.currentAttack != Fighter.AttackType.FINAL_SMASH) {
            attacker.velX = -attacker.facingDir * 6;
        } else {
// --- FINAL SMASH DAMAGE LOGIC ---
            if (attacker.currentAttack == Fighter.AttackType.FINAL_SMASH) {
                victim.damage +=50;
                victim.velY = -20.0f; 
                victim.velX = attacker.facingDir * 15.0f;
            } else {
                // Standard Attack
                victim.damage += (12 * attacker.chargeMultiplier);
                victim.ledgeGrabbed = false;
                victim.isHelpless = false;
            
            if (attacker.currentAttack == Fighter.AttackType.DOWN) {
                victim.velY = 12.0f; // Spike
            } else if (attacker.currentAttack == Fighter.AttackType.UP_SPECIAL) {
                victim.velY = -15.0f; // Launcher
                victim.velX = attacker.facingDir * 4;
            } else {
                victim.velX = attacker.facingDir * (5 + victim.damage/10);
                victim.velY = -8.0f;
            }
            }
            effects.add(new HitEffect((int)victim.x, (int)victim.y));
        }
        attacker.hitTargets.add(victim);
    }
}