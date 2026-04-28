import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List; 

public class Gamestate {
    public List<Fighter> fighters = new ArrayList<>();
    public List<Platform> platforms = new ArrayList<>();
    public List<HitEffect> effects = new ArrayList<>();
    public boolean[] keys = new boolean[65536];

    public void setupSession(String p1Char, MapData chosenMap) {
        fighters.clear();
        platforms.clear();
        effects.clear();

        // Load map platforms from the MapData
        if (chosenMap != null && chosenMap.platforms != null) {
            this.platforms.addAll(chosenMap.platforms);
        } else {
            platforms.add(new Platform(200, 500, 880, 30)); // Fallback floor
        }
        
        // P1: A, D, W, C, F, G, V (Special)
        int[] p1Keys = {KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_W, KeyEvent.VK_C, KeyEvent.VK_F, KeyEvent.VK_G, KeyEvent.VK_V};
        
        // P2: Left, Right, Up, M, L, K, PERIOD (Special)
        int[] p2Keys = {KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_UP, KeyEvent.VK_M, KeyEvent.VK_L, KeyEvent.VK_K, KeyEvent.VK_PERIOD};

        fighters.add(new Fighter(300, 300, p1Char, Color.BLUE, p1Keys));
        fighters.add(new Fighter(880, 300, "P2", Color.RED, p2Keys));
    }

    public void update() {
        // 1. UPDATE PLATFORMS FIRST (For moving platforms)
        for (Platform p : platforms) {
            p.update();
        }

        // 2. UPDATE FIGHTERS
        for (Fighter f : fighters) {
            f.update(keys, platforms);
            
            // Blast zone detection (Death)
            if (f.y > 850 || f.y < -600 || f.x < -200 || f.x > 1480) {
                if (f.stocks > 0) f.respawn(640, 300);
            }
        }

        // 3. COLLISION DETECTION (Attacks)
        for (Fighter attacker : fighters) {
            // Cannot attack if you are currently grabbed or hanging
            if (attacker.isBeingHeld || attacker.ledgeGrabbed) continue;

            Rectangle hb = attacker.getHitbox();
            if (hb != null) {
                for (Fighter victim : fighters) {
                    if (attacker == victim) continue;

                    // Check if attack hits and ensure it only hits once per animation
                    if (hb.intersects(victim.getBounds()) && !attacker.hitTargets.contains(victim)) {
                        handleHit(attacker, victim);
                    }
                }
            }
        }

        // 4. UPDATE VISUAL EFFECTS
        for (HitEffect e : effects) e.life--; 
        effects.removeIf(e -> e.life <= 0);
    }

    private void handleHit(Fighter attacker, Fighter victim) {
        // --- GRAB LOGIC ---
        if (attacker.currentAttack == Fighter.AttackType.GRAB) {
            attacker.grabbedEnemy = victim;
            victim.isBeingHeld = true;
            victim.isShielding = false;
            victim.ledgeGrabbed = false;
            victim.velX = 0; victim.velY = 0;
        } 
        // --- SHIELD LOGIC ---
        else if (victim.isShielding) {
            // Attacker gets pushed back (Shield Stun)
            attacker.velX = -attacker.facingDir * 6;
        } 
        // --- DAMAGE & KNOCKBACK ---
        else {
            // Apply damage based on charge
            float baseDamage = 12f;
            victim.damage += (baseDamage * attacker.chargeMultiplier);
            
            // Break states
            victim.ledgeGrabbed = false;
            victim.isHelpless = false; 
            
            // Calculate Knockback scaled by damage and charge
            float knockbackScale = (5 + victim.damage / 8) * attacker.chargeMultiplier;

            if (attacker.currentAttack == Fighter.AttackType.DOWN) {
                victim.velY = 12.0f; // Meteor Smash (Spike)
                victim.velX = 0;
            } else if (attacker.currentAttack == Fighter.AttackType.UP_SPECIAL) {
                victim.velY = -15.0f; // Launcher
                victim.velX = attacker.facingDir * 4;
            } else if (attacker.currentAttack == Fighter.AttackType.UP) {
                victim.velY = -knockbackScale * 1.2f;
                victim.velX = attacker.facingDir * 2;
            } else {
                // Standard Side/Neutral Knockback
                victim.velX = attacker.facingDir * knockbackScale;
                victim.velY = -knockbackScale * 0.6f;
            }

            // Create hit spark
            effects.add(new HitEffect((int)victim.x + (victim.width/2), (int)victim.y + (victim.height/2)));
        }
        
        // Register the hit so it doesn't multi-hit in one frame
        attacker.hitTargets.add(victim);
    }
}