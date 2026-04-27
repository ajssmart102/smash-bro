import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List; 

public class Gamestate {
    public List<Fighter> fighters = new ArrayList<>();
    public List<Platform> platforms = new ArrayList<>();
    public List<HitEffect> effects = new ArrayList<>();
    public boolean[] keys = new boolean[65536];

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
        for (Fighter f : fighters) {
            f.update(keys, platforms);
            if (f.y > 850 || f.y < -600 || f.x < -200 || f.x > 1480) {
                if (f.stocks > 0) f.respawn(640, 300);
            }
        }

        for (Fighter attacker : fighters) {
            if (attacker.isBeingHeld || attacker.ledgeGrabbed) continue;
            Rectangle hb = attacker.getHitbox();
            if (hb != null) {
                for (Fighter victim : fighters) {
                    if (attacker == victim) continue;
                    if (hb.intersects(victim.getBounds()) && !attacker.hitTargets.contains(victim)) {
                        handleHit(attacker, victim);
                    }
                }
            }
        }
        for (HitEffect e : effects) e.life--; 
        effects.removeIf(e -> e.life <= 0);
    }

    private void handleHit(Fighter attacker, Fighter victim) {
        if (attacker.currentAttack == Fighter.AttackType.GRAB) {
            attacker.grabbedEnemy = victim;
            victim.isBeingHeld = true;
            victim.isShielding = false;
            victim.ledgeGrabbed = false;
        } else if (victim.isShielding) {
            attacker.velX = -attacker.facingDir * 6;
        } else {
            victim.damage += (12 * attacker.chargeMultiplier);
            victim.ledgeGrabbed = false;
            victim.isHelpless = false; // Reset recovery on hit
            
            if (attacker.currentAttack == Fighter.AttackType.DOWN) {
                victim.velY = 12.0f; // Spike
            } else if (attacker.currentAttack == Fighter.AttackType.UP_SPECIAL) {
                victim.velY = -15.0f; // Launcher
                victim.velX = attacker.facingDir * 4;
            } else {
                victim.velX = attacker.facingDir * (5 + victim.damage/10);
                victim.velY = -8.0f;
            }
            effects.add(new HitEffect((int)victim.x, (int)victim.y));
        }
        attacker.hitTargets.add(victim);
    }
}