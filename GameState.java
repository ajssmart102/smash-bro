import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.*;

public class Gamestate {
    public java.util.List<Fighter> fighters = new ArrayList<>();
    public java.util.List<Platform> platforms = new ArrayList<>();
    public java.util.List<HitEffect> effects = new ArrayList<>();
    public boolean[] keys = new boolean[256];

    public void setupSession(String p1Char) {
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
        for (Fighter f : fighters) f.update(keys, platforms);

        for (Fighter attacker : fighters) {
            Rectangle hb = attacker.getHitbox();
            if (hb != null) {
                for (Fighter victim : fighters) {
                    if (attacker == victim) continue;
                    if (hb.intersects(victim.getBounds()) && !attacker.hitTargets.contains(victim)) {
                        victim.damage += 10;
                        victim.velX = attacker.facingDir * (5 + victim.damage/10);
                        victim.velY = -8;
                        attacker.hitTargets.add(victim);
                        effects.add(new HitEffect((int)victim.x, (int)victim.y));
                    }
                }
            }
        }
        effects.removeIf(e -> e.life <= 0);
    }
}
