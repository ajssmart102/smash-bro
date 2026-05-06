import java.awt.*;
import java.awt.geom.Rectangle2D;

public class Items 
{
    public float x, y, velX, velY;
    public int width = 30, height = 30;
    public boolean isHeld = false;
    public boolean isThrown = false;
    public Fighter owner = null;
    
    public Items(float x, float y)
    {
        this.x = x; this.y = y;
    }

    public void update(java.util.List<Platform> platforms) 
    {
        if (isHeld) return; // Fighter handles position when held

        // Gravity & Physics
        velY += 0.5f; // gravity
        x += velX;
        y += velY;

        // Simple Platform Collision
        for (Platform p : platforms) 
        {
            if (velY > 0 && x + width > p.x && x < p.x + p.width &&
                y + height >= p.y && y + height <= p.y + p.height + velY) 
            {
                y = p.y - height;
                velY *= -0.4f; // Bounce
                velX *= 0.8f;  // Friction
                if (Math.abs(velX) < 1) isThrown = false; // Safe to pick up
            }
        }
    }

    public Rectangle getBounds() { return new Rectangle((int)x, (int)y, width, height); }

    public void draw(Graphics2D g) 
    {
        g.setColor(isThrown ? Color.RED : Color.ORANGE);
        g.fillOval((int)x, (int)y, width, height);
    }
}