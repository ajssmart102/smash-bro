import java.awt.*;
import java.util.List;

public class ThrowableItem 
{
    public float x, y, velX, velY;
    public int width = 30, height = 30;
    
    // States
    public boolean isHeld = false;
    public boolean isFlying = false;
    public Fighter thrower = null; // Prevents the item from hitting the person who threw it
    
    public ThrowableItem(float x, float y) 
    {
        this.x = x;
        this.y = y;
    }

    public void update(List<Platform> platforms, List<Fighter> fighters) 
    {
        if (isHeld) return; // Fighter.java handles position if held

        // --- Physics ---
        velY += 0.5f; // Gravity
        x += velX;
        y += velY;

        // Apply friction/air resistance
        if (isFlying) 
        {
            velX *= 0.99f; 
        } 
        else 
        {
            velX *= 0.85f; // Faster friction when sliding on the ground
        }

        // --- Platform Collision ---
        for (Platform p : platforms) 
        {
            if (velY >= 0 && x + width > p.x && x < p.x + p.width && 
                y + height >= p.y && y + height <= p.y + p.height + velY + 2) 
            {
                
                y = p.y - height;
                velY = 0;
                velX *= 0.5f; // Bounce/friction hit
                isFlying = false; // Item is safely on the ground now
            }
        }

        // --- Hit Detection (Combat) ---
        if (isFlying) 
        {
            for (Fighter f : fighters) 
            {
                // Don't hit the person who just threw it, and don't hit if they are shielding
                if (f != thrower && getBounds().intersects(f.getBounds())) 
                {
                    if (!f.isShielding) 
                    {
                        // Apply damage and knockback to the victim
                        f.damage += 12; 
                        f.applyKnockback(velX * 0.8f, -6f);
                    } 
                    else 
                    {
                        // If they are shielding, the item just bounces off
                        velX = -velX * 0.5f;
                    }
                    
                    isFlying = false; // Item stops being "dangerous" after one hit
                    velX = 0;
                }
            }
        }
    }

    public Rectangle getBounds() 
    {
        return new Rectangle((int)x, (int)y, width, height);
    }

    public void draw(Graphics2D g) 
    {
        // Draw a simple orange crate or ball
        g.setColor(new Color(255, 140, 0)); // Orange
        g.fillRoundRect((int)x, (int)y, width, height, 10, 10);
        
        // Draw an outline
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect((int)x, (int)y, width, height, 10, 10);
        
        // Visual indicator if it's "active/dangerous"
        if (isFlying) 
        {
            g.setColor(new Color(255, 255, 255, 100));
            g.drawOval((int)x - 5, (int)y - 5, width + 10, height + 10);
        }
    }
}