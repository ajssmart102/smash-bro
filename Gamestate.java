import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List; 

public class Gamestate {
    public List<Fighter> fighters = new ArrayList<>();
    public List<Platform> platforms = new ArrayList<>();
    public List<HitEffect> effects = new ArrayList<>();
    public Color currentBg = Color.BLACK;
    public boolean[] keys = new boolean[65536];

    // UPDATED: Now takes a MapData object
    public void setupSession(String p1Char, String p2Char, MapData chosenMap) {
        fighters.clear();
        platforms.clear();
        effects.clear();

        // Load map platforms and background
        this.platforms.addAll(chosenMap.platforms);
        this.currentBg = chosenMap.backgroundColor;
        
        // P1 Bindings: A, D, W, C(Shield), F(Attack), G(Grab)
        int[] p1Keys = {KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_W, KeyEvent.VK_C, KeyEvent.VK_F, KeyEvent.VK_G};
        // P2 Bindings: Left, Right, Up, M(Shield), L(Attack), K(Grab)
        int[] p2Keys = {KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_UP, KeyEvent.VK_M, KeyEvent.VK_L, KeyEvent.VK_K};

        fighters.add(new Fighter(400, 300, "P1", Color.BLUE, p1Keys));
        fighters.add(new Fighter(880, 300, "P2", Color.RED, p2Keys));
    }

    public void update() {
        for (Fighter f : fighters) {
            f.update(keys, platforms);
            if (f.y > 900 || f.y < -600 || f.x < -300 || f.x > 1580) {
                if (f.stocks > 0) f.respawn(640, 300);
            }
        }

        // Combat collision logic (same as before)
        processCombat();
        
        for (HitEffect e : effects) e.life--; 
        effects.removeIf(e -> e.life <= 0);
    }

    private void processCombat() {
        for (Fighter attacker : fighters) {
            if (attacker.isBeingHeld) continue;
            Rectangle hb = attacker.getHitbox();
            if (hb == null) continue;

            for (Fighter victim : fighters) {
                if (attacker == victim) continue;
                if (hb.intersects(victim.getBounds()) && !attacker.hitTargets.contains(victim)) {
                    handleHit(attacker, victim);
                }
            }
        }
    }

    private void handleHit(Fighter attacker, Fighter victim) {
        if (attacker.currentAttack == Fighter.AttackType.GRAB) {
            attacker.grabbedEnemy = victim;
            victim.isBeingHeld = true;
            victim.isShielding = false;
        } else if (victim.isShielding) {
            attacker.velX = -attacker.facingDir * 6;
        } else {
            victim.damage += (12 * attacker.chargeMultiplier);
            if (attacker.currentAttack == Fighter.AttackType.DOWN) {
                victim.velY = 12.0f; // Spike!
            } else {
                victim.velX = attacker.facingDir * (5 + victim.damage/10);
                victim.velY = -8.0f;
            }
            effects.add(new HitEffect((int)victim.x, (int)victim.y));
        }
        attacker.hitTargets.add(victim);
    }
}