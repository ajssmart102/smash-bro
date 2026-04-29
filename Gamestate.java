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
        
        // Apply character-specific stat variations
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
        // 1. Update Fighters & Blast Zones
        for (Fighter f : fighters) {
            f.update(keys, platforms);
            if (f.y > 900 || f.y < -700 || f.x < -300 || f.x > 1580) {
                if (f.stocks > 0) f.respawn(640, 300);
            }
        }

        // 2. SmashBall Logic
        updateSmashBall();

        // 3. Combat Logic
        for (Fighter attacker : fighters) {
            if (attacker.isBeingHeld || attacker.ledgeGrabbed) continue;
            Rectangle hb = attacker.getHitbox();
            
            if (hb != null) {
                for (Fighter victim : fighters) {
                    if (attacker == victim) continue;
                    
                    // Check intersection and ensure move only hits once per animation
                    if (hb.intersects(victim.getBounds()) && !attacker.hitTargets.contains(victim)) {
                        handleHit(attacker, victim);
                    }
                }
            }
        }

        // 4. Effects Cleanup
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
                        // Explosive release: pushes everyone else away
                        for (Fighter target : fighters) {
                            if (target != f) {
                                float dir = (target.x > smashBall.x) ? 15f : -15f;
                                target.applyKnockback(dir, -10f);
                                effects.add(new HitEffect((int)target.x, (int)target.y));
                            }
                        }
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
            // Push attacker back on shield hit
            attacker.velX = -attacker.facingDir * 7; 
        } else {
            // 1. Calculate Damage
            float baseDmg = 10f;
            if (attacker.currentAttack == Fighter.AttackType.FINAL_SMASH) baseDmg = 45f;
            
            victim.damage += (baseDmg * attacker.chargeMultiplier * attacker.attackDamageMultiplier);

            // 2. Determine Launch Direction
            float launchX = 0, launchY = 0;

            switch (attacker.currentAttack) {
                case FINAL_SMASH:
                    launchX = attacker.facingDir * 22f;
                    launchY = -18f;
                    break;
                case DOWN:
                    launchX = attacker.facingDir * 2f;
                    launchY = 15f; // Spike
                    break;
                case UP:
                    launchX = attacker.facingDir * 1f;
                    launchY = -18f;
                    break;
                case SIDE:
                    launchX = attacker.facingDir * 14f;
                    launchY = -6f;
                    break;
                default: // Neutral
                    launchX = attacker.facingDir * 8f;
                    launchY = -9f;
                    break;
            }

            // 3. Apply the Scaling Knockback via Fighter method
            victim.applyKnockback(launchX, launchY);
            effects.add(new HitEffect((int)victim.x, (int)victim.y));
        }
        attacker.hitTargets.add(victim);
    }
}