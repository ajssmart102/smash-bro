import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.*;
import java.util.List;

public class Gamestate {
    public List<Fighter> fighters = new ArrayList<>();
    public List<Platform> platforms = new ArrayList<>();
    public List<HitEffect> effects = new ArrayList<>();
    public boolean[] keys = new boolean[65536];

    private Map<String, CharacterStats> characterRegistry = new HashMap<>();

    public SmashBall smashBall;
    private int respawnTimer = 0;
    private final int RESPAWN_DELAY = 600; 

    public Gamestate() {
        loadCharacterData();
    }

    private void loadCharacterData() {
        try (BufferedReader br = new BufferedReader(new FileReader("roster.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] v = line.split(",");
                if (v.length < 8) continue;

                String name = v[0].trim();
                float ws = Float.parseFloat(v[1]);
                float jf = Float.parseFloat(v[2]);
                float gr = Float.parseFloat(v[3]);
                float wt = Float.parseFloat(v[4]);
                float dm = Float.parseFloat(v[5]);
                int w = Integer.parseInt(v[6]); 
                int h = Integer.parseInt(v[7]); 

                characterRegistry.put(name, new CharacterStats(name, ws, jf, gr, wt, dm, w, h));
            }
        } catch (Exception e) {
            System.err.println("Could not load roster.txt: " + e.getMessage());
            characterRegistry.put("Standard", new CharacterStats("Standard", 7.0f, -14f, 0.5f, 1.0f, 1.0f, 50, 80));
        }
    }

    public CharacterStats getStatsFor(String name) {
        if (characterRegistry.containsKey(name)) {
            return characterRegistry.get(name);
        } else {
            return characterRegistry.get("Standard");
        }
    }

    public void setupSession(String p1Char, String p2Char, MapData chosenMap) {
        fighters.clear();
        platforms.clear();
        effects.clear();

        if (chosenMap != null && chosenMap.platforms != null) {
            this.platforms.addAll(chosenMap.platforms);
        } else {
            platforms.add(new Platform(200, 500, 880, 30)); 
        }
        
        int[] p1Keys = {KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_W, KeyEvent.VK_C, KeyEvent.VK_F, KeyEvent.VK_G, KeyEvent.VK_V};
        int[] p2Keys = {KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_UP, KeyEvent.VK_M, KeyEvent.VK_L, KeyEvent.VK_K, KeyEvent.VK_PERIOD};

        Fighter player1 = new Fighter(300, 300, getStatsFor(p1Char.trim()), Color.BLUE, p1Keys);
        Fighter player2 = new Fighter(880, 300, getStatsFor(p2Char.trim()), Color.RED, p2Keys);
        
        fighters.add(player1);
        fighters.add(player2);
    }

    public void update() {
        for (Platform p : platforms) p.update();

        for (Fighter f : fighters) {
            f.update(keys, platforms);
            if (f.y > 1000 || f.y < -800 || f.x < -400 || f.x > 1680) {
                if (f.stocks > 0) f.respawn(640, 300);
            }
        }

        updateSmashBall();
        updateCombat();

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
                    // FIXED: Using stats.dm to match CharacterStats field
                    smashBall.health -= (2 * f.stats.dm); 
                    if (smashBall.health <= 0) {
                        smashBall.isBroken = true;
                        f.hasFinalSmash = true;
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
            return;
        } 
        
        if (victim.isShielding && attacker.currentAttack != Fighter.AttackType.FINAL_SMASH) {
            attacker.velX = -attacker.facingDir * 7; 
            return;
        }

        // --- Calculate Damage and Knockback ---
        float damageDealt = 0;
        float launchX = 0;
        float launchY = 0;

        if (attacker.currentAttack == Fighter.AttackType.FINAL_SMASH) {
            damageDealt = 50;
            launchY = -25.0f;
            launchX = attacker.facingDir * 35.0f;
        } else {
            // 1. Calculate Standard Damage
            float baseDamage = 10.0f;
            damageDealt = baseDamage * attacker.stats.dm * attacker.chargeMultiplier;
            
            // 2. Base Knockback Power scaling
            float power = 5 + (victim.damage / 8);
            
            // 3. THE 300% DEATH MULTIPLIER
            // If victim is at or above 250%, we multiply launch power significantly
            if (victim.damage >= 250f) {
                power *= 5.0f;
            }

            victim.ledgeGrabbed = false;
            
            if (attacker.currentAttack == Fighter.AttackType.DOWN) {
                launchX = attacker.facingDir * 2; 
                launchY = Math.min(power, 45.0f); // Spike down
            } else {
                // Horizontal launch: Capped at 60.0f for engine stability
                launchX = attacker.facingDir * Math.min(power, 60.0f);
                launchY = -12.0f;
            }
        }

        // --- Apply Results & Hard Cap at 300% ---
        victim.damage += damageDealt;
        if (victim.damage > 300f) {
            victim.damage = 300f;
        }

        victim.applyKnockback(launchX, launchY);
        effects.add(new HitEffect((int)victim.x, (int)victim.y));
        attacker.hitTargets.add(victim);
    }
}