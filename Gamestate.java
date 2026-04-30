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

    public void setupSession(String p1Char, String p2Char, MapData chosenMap) {
        fighters.clear();
        platforms.clear();
        effects.clear();

        // 1. Load Map Platforms
        if (chosenMap != null && chosenMap.platforms != null) {
            this.platforms.addAll(chosenMap.platforms);
        } else {
            platforms.add(new Platform(200, 500, 880, 30)); // Default floor
        }
        
        // 2. Define Keybinds
        int[] p1Keys = {KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_W, KeyEvent.VK_C, KeyEvent.VK_F, KeyEvent.VK_G, KeyEvent.VK_V};
        int[] p2Keys = {KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_UP, KeyEvent.VK_M, KeyEvent.VK_L, KeyEvent.VK_K, KeyEvent.VK_PERIOD};

        // 3. Create Fighters
        Fighter player1 = new Fighter(300, 300, p1Char, Color.BLUE, p1Keys);
        Fighter player2 = new Fighter(880, 300, p2Char, Color.RED, p2Keys);
        
        applyStats(player1, p1Char);
        applyStats(player2, p2Char);

        fighters.add(player1);
        fighters.add(player2);
    }

    private void applyStats(Fighter f, String charName) {
        switch (charName) {
            case "Tank": 
                f.weight = 1.4f; f.walkSpeed = 4.5f; f.attackDamageMultiplier = 1.2f; break;
            case "Speedster": 
                f.weight = 0.7f; f.walkSpeed = 9.0f; f.jumpForce = -15f; break;
            case "Floaty": 
                f.gravity = 0.25f; f.weight = 0.8f; break;
        }
    }

    public void update() {
        // --- 1. Update Platforms (CRITICAL for moving maps) ---
        for (Platform p : platforms) {
            p.update();
        }

        // --- 2. Update Fighters & Blast Zones ---
        for (Fighter f : fighters) {
            f.update(keys, platforms);
            // Blast Zone detection (Respawn)
            if (f.y > 1000 || f.y < -800 || f.x < -400 || f.x > 1680) {
                if (f.stocks > 0) f.respawn(640, 300);
            }
        }

        // --- 3. Items & Combat ---
        updateSmashBall();
        updateCombat();

        // --- 4. Effects Cleanup ---
        for (HitEffect e : effects) e.life--; 
        effects.removeIf(e -> e.life <= 0);
    }

    private void updateSmashBall() {
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
                    smashBall.health -= (2 * f.attackDamageMultiplier); 
                    if (smashBall.health <= 0) {
                        smashBall.isBroken = true;
                        f.hasFinalSmash = true;
                        // Explosion uses the scaling knockback system
                        for (Fighter target : fighters) {
                            if (target != f) {
                                float dir = (target.x > smashBall.x) ? 18f : -18f;
                                target.applyKnockback(dir, -12f);
                                effects.add(new HitEffect((int)target.x, (int)target.y));
                            }
                        }
                    }
                }
            }
        }
    }

    private void updateCombat() {
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
    }

    private void handleHit(Fighter attacker, Fighter victim) {
        if (attacker.currentAttack == Fighter.AttackType.GRAB) {
            attacker.grabbedEnemy = victim;
            victim.isBeingHeld = true;
            victim.isShielding = false;
        } else if (victim.isShielding && attacker.currentAttack != Fighter.AttackType.FINAL_SMASH) {
            attacker.velX = -attacker.facingDir * 7; 
        } else {
            // Calculate Damage
            float baseDmg = 10f;
            if (attacker.currentAttack == Fighter.AttackType.FINAL_SMASH) baseDmg = 45f;
            
            victim.damage += (baseDmg * attacker.chargeMultiplier * attacker.attackDamageMultiplier);

            // Determine Base Launch Angle
            float launchX = 0, launchY = 0;

            switch (attacker.currentAttack) {
                case FINAL_SMASH:
                    launchX = attacker.facingDir * 22f; launchY = -18f; break;
                case DOWN:
                    launchX = attacker.facingDir * 2f; launchY = 16f; break; // Spike
                case UP:
                    launchX = attacker.facingDir * 1f; launchY = -19f; break;
                case SIDE:
                    launchX = attacker.facingDir * 15f; launchY = -7f; break;
                case UP_SPECIAL:
                    launchX = attacker.facingDir * 4f; launchY = -14f; break;
                default: // Neutral
                    launchX = attacker.facingDir * 9f; launchY = -10f; break;
            }

            // Apply scaling knockback
            victim.applyKnockback(launchX, launchY);
            effects.add(new HitEffect((int)victim.x, (int)victim.y));
        }
        attacker.hitTargets.add(victim);
    }
}