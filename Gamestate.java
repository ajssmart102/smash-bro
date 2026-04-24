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
    private final int RESPAWN_DELAY = 600; // 600 frames = 10 seconds at 60fps

    public void setupSession(String p1Char) {
        fighters.clear();
        platforms.clear();
        effects.clear();

        // NEW: Initialize the SmashBall
        smashBall = new SmashBall(640, 200);

        platforms.add(new Platform(200, 500, 880, 30));
        platforms.add(new Platform(300, 350, 200, 30));
        platforms.add(new Platform(550, 200, 200, 30)); 
        platforms.add(new Platform(800, 350, 200, 30));
        
        // Player 1: A, D, W, S, F (Attack), G (Grab)
        int[] p1Bindings = {KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_F, KeyEvent.VK_G};
        
        // Player 2: Left, Right, Up, Down, L (Attack), K (Grab)
        int[] p2Bindings = {KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_L, KeyEvent.VK_K};

        fighters.add(new Fighter(300, 300, "P1", Color.BLUE, p1Bindings));
        fighters.add(new Fighter(800, 300, "P2", Color.RED, p2Bindings));
    }

    public void update() {
        for (Fighter f : fighters) {
            f.update(keys, platforms);
            if (f.y > 800 || f.y < -500 || f.x < -100 || f.x > 1380) {
                if (f.stocks > 0) f.respawn(640, 300);
            }
        }

// 2. SMASH BALL LIFECYCLE
        if (smashBall == null || smashBall.isBroken) {
            respawnTimer++;
            if (respawnTimer >= RESPAWN_DELAY) {
                smashBall = new SmashBall(640, 200); // Respawn in center
                respawnTimer = 0; // Reset timer
            }
        } else {
            // Update movement
            smashBall.update(1280, 720); // Pass screen dimensions

            // Check collision with fighters
            for (Fighter attacker : fighters) {
                if (attacker.getHitbox() != null && attacker.getHitbox().intersects(smashBall.getHitbox())) {
                    if (attacker.currentAttack != Fighter.AttackType.NONE) {
                        int damageDealt = (int)(5 * attacker.chargeMultiplier);
                        smashBall.health -= damageDealt;
                        if (smashBall.health <= 0) {
                            smashBall.isBroken = true;
                            attacker.hasFinalSmash = true; // GRANT ABILITY
                        }
                    }
                }
            }
        }
        for (Fighter attacker : fighters) {
            if (attacker.isBeingHeld) continue;
            Rectangle hb = attacker.getHitbox();
            
            if (hb != null) {
                for (Fighter victim : fighters) {
                    if (attacker == victim) continue;
                    if (victim.isBeingHeld && attacker.grabbedEnemy != victim) continue;

                    if (victim.getBounds() != null && hb.intersects(victim.getBounds())) {
                        if (!attacker.hitTargets.contains(victim)) {
                            
                            if (attacker.currentAttack == Fighter.AttackType.GRAB) {
                                attacker.grabbedEnemy = victim;
                                victim.isBeingHeld = true;
                                attacker.attackTimer = 0;
                                attacker.hitTargets.add(victim); 
                            }
                                else if (attacker.currentAttack == Fighter.AttackType.FINAL_SMASH) {
                                victim.damage += 50; // High base damage
                                victim.velX = attacker.facingDir * 25; // High horizontal knockback
                                victim.velY = -15.0f; // High vertical launch
                            }
                             else {
                                victim.damage += (10 * attacker.chargeMultiplier);
                                victim.velX = (float) (attacker.facingDir * (5 + (double)victim.damage / 10) * attacker.chargeMultiplier);
                                victim.velY = -8.0f;
                                attacker.hitTargets.add(victim);
                                effects.add(new HitEffect((int)victim.x, (int)victim.y));
                            }
                            // Shared cleanup for successful hits
                            attacker.hitTargets.add(victim);
                            effects.add(new HitEffect((int)victim.x, (int)victim.y));
                        }
                    }
                }
            }
        }
        
        for (HitEffect e : effects) e.life--; 
        effects.removeIf(e -> e.life <= 0);
    }
}