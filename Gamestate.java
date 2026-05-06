import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List; 

public class Gamestate 
{
    public List<Fighter> fighters = new ArrayList<>();
    public List<Platform> platforms = new ArrayList<>();
    public List<HitEffect> effects = new ArrayList<>();
    public List<Items> itemList = new ArrayList<>();
    public boolean[] keys = new boolean[65536];

    private int spawnTimer = 0;
    private final int MIN_SPAWN_TIME = 300; // 5 seconds at 60fps
    private final int MAX_SPAWN_TIME = 900; // 15 seconds at 60fps
    private int nextSpawnIn = 600;         // Initial wait

    public SmashBall smashBall;
    private int respawnTimer = 0;
    private final int RESPAWN_DELAY = 600; // 10 seconds at 60fps

    // Fixed signature: Accepts P1 character and the chosen MapData
    public void setupSession(String p1Char, MapData chosenMap) 
    {
        fighters.clear();
        platforms.clear();
        effects.clear();
        itemList.clear();

        // Load map platforms
        if (chosenMap != null && chosenMap.platforms != null) 
        {
            this.platforms.addAll(chosenMap.platforms);
        } 
        else 
        {
            platforms.add(new Platform(200, 500, 880, 30)); // Fallback
        }
        
        // P1: A, D, W, C, F, G, V (Index 6 is Special)
        int[] p1Keys = {KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_W, KeyEvent.VK_C, KeyEvent.VK_F, KeyEvent.VK_G, KeyEvent.VK_V};
        
        // P2: Left, Right, Up, M, L, K, PERIOD (Index 6 is Special)
        int[] p2Keys = {KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_UP, KeyEvent.VK_M, KeyEvent.VK_L, KeyEvent.VK_K, KeyEvent.VK_PERIOD};

        fighters.add(new Fighter(300, 300, p1Char, Color.BLUE, p1Keys));
        fighters.add(new Fighter(880, 300, "P2", Color.RED, p2Keys));
    }

    private void spawnItem() 
    {
        if (platforms.isEmpty()) return;

        // Pick a random platform to spawn over
        Platform p = platforms.get((int)(Math.random() * platforms.size()));
        
        // Position it somewhere along the width of that platform
        float spawnX = p.x + (float)(Math.random() * p.width);
        float spawnY = -50; // Start at the top of the screen to "drop" in
        
        itemList.add(new Items(spawnX, spawnY));

        effects.add(new HitEffect((int)spawnX, (int)spawnY));
        // This makes the item "burst" into existence

        double chance = Math.random();
        if (chance < 0.1) 
        {
            // 10% chance for a rare/powerful item
        } 
        else 
        {
            // 90% chance for a standard orange ball
        }
    }

    public void update() 
    {
        // 1. Update Fighters
        for (Fighter f : fighters) 
        {
            f.update(keys, platforms, itemList);
            if (f.y > 850 || f.y < -600 || f.x < -200 || f.x > 1480) 
            {
                if (f.stocks > 0) f.respawn(640, 300);
            }
        }

        // 2. Update Items (Physics and Collision)
        for (Items item : itemList) 
        {
            item.update(platforms);

            if (item.isThrown) 
            {
                for (Fighter victim : fighters) 
                {
                    // THE CRITICAL CHECK: f != item.owner
                    if (victim != item.owner && item.getBounds().intersects(victim.getBounds())) 
                    {
                        victim.damage += 15;
                        victim.velX = item.velX * 0.7f;
                        victim.velY = -6;
                        item.isThrown = false; // Item becomes safe
                        item.velX = 0; // Stop it from flying through the person
                        effects.add(new HitEffect((int)item.x, (int)item.y));
                    }
                }
            }
        }
        
        spawnTimer++;

        if (spawnTimer >= nextSpawnIn)
        {
            // 1. Only spawn if there aren't too many items already (Smash usually limits this)
            if (itemList.size() < 5) 
            {
                spawnItem();
            }
            
            // 2. Reset the timer
            spawnTimer = 0;
            
            // 3. Pick a new random time for the NEXT item
            // This gives that "Smash" feel: sometimes fast, sometimes slow
            nextSpawnIn = MIN_SPAWN_TIME + (int)(Math.random() * (MAX_SPAWN_TIME - MIN_SPAWN_TIME));
        }

        // 2. SmashBall Logic (The missing part)
        if (smashBall == null || smashBall.isBroken) 
        {
            respawnTimer++;
            if (respawnTimer >= RESPAWN_DELAY) 
            {
                smashBall = new SmashBall(640, 200); // Spawn at center-top
                respawnTimer = 0;
            }
        } 
        else 
        {
            // Move the ball
            smashBall.update(1280, 720); 

            // Check for fighter hits on the SmashBall
            for (Fighter f : fighters) 
            {
                Rectangle hb = f.getHitbox();
                if (hb != null && hb.intersects(smashBall.getHitbox())) 
                {
                    // If fighter is attacking, damage the ball
                    if (f.currentAttack != Fighter.AttackType.NONE) 
                    {
                        smashBall.health -= 2; // Adjust damage as needed
                        if (smashBall.health <= 0) 
                        {
                            smashBall.isBroken = true;
                            f.hasFinalSmash = true;
                            // --- EXPLOSION DAMAGE (50 to everyone else) ---
                                for (Fighter target : fighters) 
                                {
                                    if (target != f) 
                                    {
                                        target.velY = -10.0f; // Launch up
                                        target.velX = (target.x > smashBall.x) ? 15.0f : -15.0f; // Push away from ball
                                        effects.add(new HitEffect((int)target.x, (int)target.y));
                                    }
                                }
                        }
                    }
                }
            }
        }
    // 3. Combat Logic (FIXED: Bypassing hitTargets for Final Smash)
            for (Fighter attacker : fighters) {
                if (attacker.isBeingHeld || attacker.ledgeGrabbed) continue;
                Rectangle hb = attacker.getHitbox();
                if (hb != null) {
                    for (Fighter victim : fighters) {
                        if (attacker == victim) continue;
                        
                        // The Fix: Check if Final Smash OR if they haven't been hit yet
                        boolean isFinalSmash = attacker.currentAttack == Fighter.AttackType.FINAL_SMASH;
                        boolean notAlreadyHit = !attacker.hitTargets.contains(victim);
                        
                    if (hb.intersects(victim.getBounds()) && !attacker.hitTargets.contains(victim)) {
                    handleHit(attacker, victim);
                        }
                    }
                }
            }

        // 4. Effects Logic (Existing)
        for (HitEffect e : effects) e.life--; 
        effects.removeIf(e -> e.life <= 0);
    }

        private void handleHit(Fighter attacker, Fighter victim)
        {
            if (attacker.currentAttack == Fighter.AttackType.GRAB)
            {
                attacker.grabbedEnemy = victim;
                victim.isBeingHeld = true;
                victim.hitstun = 0; // Reset hitstun while being held
                victim.isShielding = false;
                victim.velX = 0;
                victim.velY = 0;
                victim.ledgeGrabbed = false;
            } 
            else if (victim.isShielding && attacker.currentAttack != Fighter.AttackType.FINAL_SMASH) 
            {
                attacker.velX = -attacker.facingDir * 6;
            } 
            else 
            {
            // --- FINAL SMASH DAMAGE LOGIC ---
                if (attacker.currentAttack == Fighter.AttackType.FINAL_SMASH)
                {
                    victim.damage +=50;
                    victim.velY = -20.0f; 
                    victim.velX = attacker.facingDir * 15.0f;
                    victim.hitstun = 60; //Frozen for 60 frames, then regain drift
                    victim.isBeingHeld = false;
                } 
                else 
                {
                        // Standard Attack
                        victim.damage += (12 * attacker.chargeMultiplier);
                        victim.ledgeGrabbed = false;
                        victim.isHelpless = false;
                    
                    if (attacker.currentAttack == Fighter.AttackType.DOWN) 
                    {
                        victim.velY = 12.0f; // Spike
                    } 
                    else if (attacker.currentAttack == Fighter.AttackType.UP_SPECIAL) 
                    {
                        victim.velY = -15.0f; // Launcher
                        victim.velX = attacker.facingDir * 4;
                    } 
                    else 
                    {
                        victim.velX = attacker.facingDir * (5 + victim.damage/10);
                        victim.velY = -8.0f;
                    }
                }
                effects.add(new HitEffect((int)victim.x, (int)victim.y));
            }
            attacker.hitTargets.add(victim);
        }
}