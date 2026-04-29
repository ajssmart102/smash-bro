import java.awt.*;

class Platform {
    // Changed to float for smoother sub-pixel movement
    public float x, y, width, height;
    public float velX = 0, velY = 0;
    
    // Movement boundaries
    private float startX, startY;
    private int rangeX = 0, rangeY = 0;

    // Standard Static Platform Constructor
    public Platform(int x, int y, int w, int h) {
        this.x = x; this.y = y; this.width = w; this.height = h;
        this.startX = x; this.startY = y;
    }

    // NEW: Moving Platform Constructor
    // vx/vy: velocity, rx/ry: how many pixels to move from the start before turning back
    public Platform(int x, int y, int w, int h, float vx, float vy, int rx, int ry) {
        this(x, y, w, h);
        this.velX = vx;
        this.velY = vy;
        this.rangeX = rx;
        this.rangeY = ry;
    }

    public void update() {
        x += velX;
        y += velY;

        // If a range is set, check if we need to reverse direction
        if (rangeX > 0 && Math.abs(x - startX) > rangeX) {
            velX *= -1;
            // Snap to boundary to prevent getting stuck
            x = (velX > 0) ? startX - rangeX : startX + rangeX;
        }
        if (rangeY > 0 && Math.abs(y - startY) > rangeY) {
            velY *= -1;
            y = (velY > 0) ? startY - rangeY : startY + rangeY;
        }
    }

    public void draw(Graphics2D g) {
        g.setColor(Color.DARK_GRAY);
        g.fillRect((int)x, (int)y, (int)width, (int)height);
        
        // Optional: Add a lighter top edge to make platforms pop
        g.setColor(Color.GRAY);
        g.fillRect((int)x, (int)y, (int)width, 4);
    }
}

class HitEffect {
    public int x, y, life = 10;
    public HitEffect(int x, int y) { this.x = x; this.y = y; }
    public void draw(Graphics2D g) {
        // Flash effect: size decreases with life
        int size = life * 4;
        g.setColor(new Color(255, 165, 0, 150)); // Semi-transparent orange
        g.fillOval(x - size/2, y - size/2, size, size);
        life--;
    }
}