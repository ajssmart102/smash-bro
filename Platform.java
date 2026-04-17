import java.awt.*;

class Platform 
{
    public int x, y, width, height;
    public boolean isSoft;
    public Platform(int x, int y, int w, int h) 
    {
        this.x = x; this.y = y; this.width = w; this.height = h; this.isSoft = false;
    }
    public Platform(int x, int y, int width, int height, boolean isSoft) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.isSoft = isSoft; // This one lets us choose!
    }
    public void draw(Graphics2D g)
    {
        g.setColor(Color.DARK_GRAY);
        g.fillRect(x, y, width, height);
    }
}

class HitEffect {
    public int x, y, life = 10;
    public HitEffect(int x, int y) { this.x = x; this.y = y; }
    public void draw(Graphics2D g) 
    {
        g.setColor(Color.ORANGE);
        g.fillOval(x-20, y-20, 40, 40);
        life--;
    }
}
