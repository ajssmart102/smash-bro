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
        // Platform 1: Placed 150 pixels higher and offset to the left
        platforms.add(new Platform(300, 350, 200, 30));
        // Platform 2: Placed 300 pixels higher and offset to the right
        platforms.add(new Platform(550, 200, 200, 30)); 
        // Platform 3: Placed 150 pixels higher and offset to the right
        platforms.add(new Platform(800, 350, 200, 30));
        
        // UPDATED: Added VK_S as the 4th key (Down) for Player 1
        int[] p1Bindings = {KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_F};
        
        // UPDATED: Added VK_DOWN as the 4th key (Down) for Player 2
        int[] p2Bindings = {KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_L};

        if (p1Char.equalsIgnoreCase("Tank")) {
            fighters.add(new TankFighter(300, 300, "P1", Color.BLUE, p1Bindings));
        } else {
            fighters.add(new Fighter(300, 300, "P1", Color.BLUE, p1Bindings));
        }
        
        fighters.add(new Fighter(800, 300, "P2", Color.RED, p2Bindings));
    }

public void update() {
        // 1. Update movement and check for "Blast Zone" death
        for (Fighter f : fighters) {
            f.update(keys, platforms);

            // Blast Zone Logic (If off screen)
            if (f.y > 800 || f.y < -500 || f.x < -100 || f.x > 1380) {
                if (f.stocks > 0) {
                    f.respawn(400, 300);
                }
            }
        }

        // 2. Combat Logic
        for (Fighter attacker : fighters) {
            Rectangle hb = attacker.getHitbox();
            
            if (hb != null) {
                for (Fighter victim : fighters) {
                    if (attacker == victim) continue;

                    if (victim.getBounds() != null && hb.intersects(victim.getBounds())) {
                        if (!attacker.hitTargets.contains(victim)) {
                            
                            // NEW: Apply the attacker's charge multiplier to the damage
                            victim.damage += (10 * attacker.chargeMultiplier);
                            
                            // NEW: Apply the attacker's charge multiplier to the knockback
                            victim.velX = (float) (attacker.facingDir * (5 + (double)victim.damage / 10) * attacker.chargeMultiplier);
                            victim.velY = -8.0f; // Use 'f' suffix for literal floats
                            
                            attacker.hitTargets.add(victim);
                            effects.add(new HitEffect((int)victim.x, (int)victim.y));
                        }
                    }
                }
            }
        }
        
        // 3. Effects Management
        for (HitEffect e : effects) {
            e.life--; 
        }
        effects.removeIf(e -> e.life <= 0);
    } // End of update()
} // End of Class