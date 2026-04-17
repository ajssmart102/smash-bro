import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List; // Explicitly import List to avoid ambiguity

public class Gamestate {
    // 1. Fixed: Use the generic List interface for consistency
    public List<Fighter> fighters = new ArrayList<>();
    public List<Platform> platforms = new ArrayList<>();
    public List<HitEffect> effects = new ArrayList<>();
    
    // 2. Critical Fix: Increase array size to 65536. 
    // Java KeyEvent codes can exceed 255 (like arrow keys or special keys).
    public boolean[] keys = new boolean[65536];

    public void setupSession(String p1Char) {
        // Clear previous session data if restarting
        fighters.clear();
        platforms.clear();
        effects.clear();

        platforms.add(new Platform(200, 500, 880, 30)); // Main stage
        
        int[] p1Bindings = {KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_W, KeyEvent.VK_F};
        int[] p2Bindings = {KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_UP, KeyEvent.VK_L};

        if (p1Char.equalsIgnoreCase("Tank")) {
            fighters.add(new TankFighter(300, 300, "P1", Color.BLUE, p1Bindings));
        } else {
            fighters.add(new Fighter(300, 300, "P1", Color.BLUE, p1Bindings));
        }
        
        fighters.add(new Fighter(800, 300, "P2", Color.RED, p2Bindings));
    }

    public void update() {
        // Update physics and movement
        for (Fighter f : fighters) {
            f.update(keys, platforms);
        }

        // Collision & Combat Logic
        for (Fighter attacker : fighters) {
            Rectangle hb = attacker.getHitbox();
            
            if (hb != null) {
                for (Fighter victim : fighters) {
                    if (attacker == victim) continue;

                    // 3. Fix: Ensure getBounds() doesn't return null and check intersection
                    if (victim.getBounds() != null && hb.intersects(victim.getBounds())) {
                        
                        // Only hit if the victim hasn't been hit by this specific attack yet
                        if (!attacker.hitTargets.contains(victim)) {
                            victim.damage += 10;
                            
                            // Knockback formula
                            victim.velX = attacker.facingDir * (5 + (double)victim.damage / 10);
                            victim.velY = -8;
                            
                            attacker.hitTargets.add(victim);
                            effects.add(new HitEffect((int)victim.x, (int)victim.y));
                        }
                    }
                }
            }
        }
        
        // Update effects life (assuming HitEffect has a logic for this)
        for (HitEffect e : effects) {
            e.life--; 
        }
        effects.removeIf(e -> e.life <= 0);
    }
}
