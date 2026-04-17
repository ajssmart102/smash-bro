import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List; 

public class Gamestate {
    public List<Fighter> fighters = new ArrayList<>();
    public List<Platform> platforms = new ArrayList<>();
    public List<HitEffect> effects = new ArrayList<>();
    
    // Increased size to handle all potential Java KeyCodes safely
    public boolean[] keys = new boolean[65536];

    public void setupSession(String p1Char) {
        fighters.clear();
        platforms.clear();
        effects.clear();

        platforms.add(new Platform(200, 500, 880, 30)); 
        
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
        for (Fighter f : fighters) {
            f.update(keys, platforms);
        }

        for (Fighter attacker : fighters) {
            Rectangle hb = attacker.getHitbox();
            
            if (hb != null) {
                for (Fighter victim : fighters) {
                    if (attacker == victim) continue;

                    if (victim.getBounds() != null && hb.intersects(victim.getBounds())) {
                        if (!attacker.hitTargets.contains(victim)) {
                            victim.damage += 10;
                            
                            // FIX: Cast the double result to a float for velX/velY
                            victim.velX = (float) (attacker.facingDir * (5 + (double)victim.damage / 10));
                            victim.velY = -8.0f; // Use 'f' suffix for literal floats
                            
                            attacker.hitTargets.add(victim);
                            effects.add(new HitEffect((int)victim.x, (int)victim.y));
                        }
                    }
                }
            }
        }
        
        // Safely update and remove expired effects
        for (HitEffect e : effects) {
            e.life--; 
        }
        effects.removeIf(e -> e.life <= 0);
    }
}
