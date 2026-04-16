import java.awt.*;

class Platform {
    public int x, y, width, height;
    public Platform(int x, int y, int w, int h) {
        this.x = x; this.y = y; this.width = w; this.height = h;
    }
    public void draw(Graphics2D g) {
        g.setColor(Color.DARK_GRAY);
        g.fillRect(x, y, width, height);
    }
}

class HitEffect {
    public int x, y, life = 10;
    public HitEffect(int x, int y) { this.x = x; this.y = y; }
    public void draw(Graphics2D g) {
        g.setColor(Color.ORANGE);
        g.fillOval(x-20, y-20, 40, 40);
        life--;
    }
}
