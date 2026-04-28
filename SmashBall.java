import java.awt.*;
import java.util.Random;

public class SmashBall {
    public float x, y, velX, velY;
    public int width = 40, height = 40;
    public int health = 100;
    public boolean isBroken = false;

    public SmashBall(int x, int y) {
        this.x = x;
        this.y = y;
        // Random starting velocity
        Random rand = new Random();
        this.velX = (rand.nextFloat() * 8) - 4; 
        this.velY = (rand.nextFloat() * 8) - 4;
    }

    public void update(int screenWidth, int screenHeight) {
        // Move the ball
        x += velX;
        y += velY;

        // Bounce off walls
        if (x <= 0 || x + width >= screenWidth) velX *= -1;
        if (y <= 0 || y + height >= screenHeight) velY *= -1;
    }

    public Rectangle getHitbox() {
        return new Rectangle((int)x, (int)y, width, height);
    }

    public void draw(Graphics2D g) {
        if (!isBroken) {
            g.setColor(Color.YELLOW);
            g.fillOval((int)x, (int)y, width, height);
            // Optional: Draw a "health" indicator on the ball
            g.setColor(Color.RED);
            g.fillRect((int)x, (int)y - 10, 40, 5);
            g.setColor(Color.GREEN);
            g.fillRect((int)x, (int)y - 10, (int)(40 * (health / 100.0)), 5);
        }
    }
}