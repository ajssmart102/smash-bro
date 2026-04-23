import java.awt.*;

public class SmashBall {
    int x, y, size = 50;
    int health = 100;
    boolean isBroken = false;

    public SmashBall(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void update() {
        // Add movement logic here (e.g., bouncing)
        if (health <= 0) {
            isBroken = true;
        }
    }

    public void draw(Graphics g) {
        if (!isBroken) {
            g.setColor(Color.YELLOW);
            g.fillOval(x, y, size, size); // Draw the ball
        }
    }

    public Rectangle getHitbox() {
        return new Rectangle(x, y, size, size);
    }
}
