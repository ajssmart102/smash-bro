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

    // Registry for your character stats
    private Map<String, CharacterStats> characterRegistry = new HashMap<>();

    public SmashBall smashBall;
    private int respawnTimer = 0;
    private final int RESPAWN_DELAY = 600; 

    public Gamestate() {
        // Load the data as soon as the game state is created
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
                int w = Integer.parseInt(v[6]); // Parse Width
                int h = Integer.parseInt(v[7]); // Parse Height

                characterRegistry.put(name, new CharacterStats(name, ws, jf, gr, wt, dm, w, h));
            }
        } catch (Exception e) {
            System.err.println("Could not load roster.txt: " + e.getMessage());
            // Fallback for safety
            characterRegistry.put("Standard", new CharacterStats("Standard", 7.0f, -14f, 0.5f, 1.0f, 1.0f, 50, 80));
        }
    }

    public CharacterStats getStatsFor(String name) {
        if (characterRegistry.containsKey(name)) {
            return characterRegistry.get(name);
        } else {
            System.out.println("Warning: Character '" + name + "' not found. Using Standard.");
            return characterRegistry.get("Standard");
        }
    }

    public void setupSession(String p1Char, String p2Char, MapData chosenMap) {
        System.out.println("Attempting to load: P1='" + p1Char + "', P2='" + p2Char + "'");

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

        // Get stats
        CharacterStats p1Stats = getStatsFor(p1Char.trim());
        CharacterStats p2Stats = getStatsFor(p2Char.trim());

        // Use those stats directly in the constructor
        Fighter player1 = new Fighter(300, 300, p1Stats, Color.BLUE, p1Keys);
        Fighter player2 = new Fighter(880, 300, p2Stats, Color.RED, p2Keys);
        
        fighters.add(player1);
        fighters.add(player2);
    }

    public void update() {
        // 1. Update Fighters & Blast Zones
        for (Fighter f : fighters) {
            f.update(keys, platforms);
            // Blast Zone detection
            if (f.y > 850 || f.y < -600 || f.x < -200 || f.x > 1480) {
                if (f.stocks > 0) f.respawn(640, 300);
            }
        }

        // 2. SmashBall Logic
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
                    if (f.currentAttack != Fighter.AttackType.NONE) {
                        smashBall.health -= 2; 
                        if (smashBall.health <= 0) {
                            smashBall.isBroken = true;
                            f.hasFinalSmash = true;
                            // Blast everyone else away
                            for (Fighter target : fighters) {
                                if (target != f) {
                                    target.velY = -12.0f;
                                    target.velX = (target.x > smashBall.x) ? 18.0f : -18.0f;
                                    effects.add(new HitEffect((int)target.x, (int)target.y));
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Combat Logic
        for (Fighter attacker : fighters) {
            if (attacker.isBeingHeld || attacker.ledgeGrabbed) continue;
            Rectangle hb = attacker.getHitbox();
            if (hb != null) {
                for (Fighter victim : fighters) {
                    if (attacker == victim) continue;
                    
                    // Logic to ensure hit only registers once per attack animation
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

    private void handleHit(Fighter attacker, Fighter victim) {
        if (attacker.currentAttack == Fighter.AttackType.GRAB) {
            attacker.grabbedEnemy = victim;
            victim.isBeingHeld = true;
            victim.isShielding = false;
        } else if (victim.isShielding && attacker.currentAttack != Fighter.AttackType.FINAL_SMASH) {
            attacker.velX = -attacker.facingDir * 6; // Shield push-back
        } else {
            if (attacker.currentAttack == Fighter.AttackType.FINAL_SMASH) {
                victim.damage += 50;
                victim.velY = -22.0f; 
                victim.velX = attacker.facingDir * 20.0f;
            } else {
                // FIXED: Standard Attack Calculation using character stats
                float baseDamage = 10.0f;
                float calculatedDamage = baseDamage * attacker.stats.dm * attacker.chargeMultiplier;
                
                victim.damage += calculatedDamage;
                victim.ledgeGrabbed = false;
                
                if (attacker.currentAttack == Fighter.AttackType.DOWN) {
                    victim.velY = 14.0f; // Spike effect
                } else {
                    victim.velX = attacker.facingDir * (5 + victim.damage/8);
                    victim.velY = -10.0f;
                }
            }
            effects.add(new HitEffect((int)victim.x, (int)victim.y));
        }
        attacker.hitTargets.add(victim);
    }
}