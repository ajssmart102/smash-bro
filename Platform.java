import java.awt.*;

public class Platform {
    // Changed to float for smoother sub-pixel movement
    public float x, y, width, height;
    public float velX = 0, velY = 0;
    
    // Movement boundaries
    private float startX, startY;
    private int rangeX = 0, rangeY = 0;

    /**
     * Standard Static Platform Constructor
     * Matches MapData: PLAT:x,y,w,h
     */
    public Platform(float x, float y, int w, int h) {
        this.x = x; 
        this.y = y; 
        this.width = (float)w; 
        this.height = (float)h;
        this.startX = x; 
        this.startY = y;
    }

    /**
     * Moving Platform Constructor
     * Matches MapData: PLAT:x,y,w,h,vx,vy,rx,ry
     */
    public Platform(float x, float y, int w, int h, float vx, float vy, int rx, int ry) {
        this(x, y, w, h);
        this.velX = vx;
        this.velY = vy;
        this.rangeX = rx;
        this.rangeY = ry;
    }

    public void update() {
        // 1. Apply movement
        x += velX;
        y += velY;

        // 2. Horizontal Boundary Logic (Prevents Vibrating)
        if (rangeX > 0) {
            float rightLimit = startX + rangeX;
            float leftLimit = startX - rangeX;

            if (x > rightLimit) {
                x = rightLimit;        // Snap to exactly the limit
                velX = -Math.abs(velX); // Force direction to be negative (Left)
            } else if (x < leftLimit) {
                x = leftLimit;         // Snap to exactly the limit
                velX = Math.abs(velX);  // Force direction to be positive (Right)
            }
        }

        // 3. Vertical Boundary Logic (Prevents Vibrating)
        if (rangeY > 0) {
            float bottomLimit = startY + rangeY;
            float topLimit = startY - rangeY;

            if (y > bottomLimit) {
                y = bottomLimit;       // Snap to exactly the limit
                velY = -Math.abs(velY); // Force direction to be negative (Up)
            } else if (y < topLimit) {
                y = topLimit;          // Snap to exactly the limit
                velY = Math.abs(velY);  // Force direction to be positive (Down)
            }
        }
    }

    public void draw(Graphics2D g) {
        // Draw Main Body (Dark Gray)
        g.setColor(new Color(50, 50, 55));
        g.fillRect((int)x, (int)y, (int)width, (int)height);
        
        // Lighter Top Edge (Highlight)
        g.setColor(new Color(150, 150, 170));
        g.fillRect((int)x, (int)y, (int)width, 4);

        // Black Border for clarity
        g.setColor(Color.BLACK);
        g.drawRect((int)x, (int)y, (int)width, (int)height);
    }
}

/**
 * HitEffect remains in the same file or package
 */
class HitEffect {
    public int x, y, life = 12;
    
    public HitEffect(int x, int y) { 
        this.x = x; 
        this.y = y; 
    }
    
    public void draw(Graphics2D g) {
        if (life <= 0) return;
        int size = life * 5;
        
        // Inner white flash
        g.setColor(new Color(255, 255, 255, 180));
        g.fillOval(x - size/2, y - size/2, size, size);
        
        // Outer orange ring
        g.setColor(new Color(255, 100, 0, 120));
        g.fillOval(x - (size + 10)/2, y - (size + 10)/2, size + 10, size + 10);
        
        life--;
    }
}