public class CharacterStats {
    public String name;
    public float walkSpeed;
    public float jumpForce;
    public float gravity;
    public float weight;
    public float dm;
    // 1. Define the fields here
    public int width;
    public int height;

    // 2. Add 'int width' and 'int height' to the constructor parameters
    public CharacterStats(String name, float walkSpeed, float jumpForce, float gravity, float weight, float damageMultiplier, int width, int height) {
        this.name = name;
        this.walkSpeed = walkSpeed;
        this.jumpForce = jumpForce;
        this.gravity = gravity;
        this.weight = weight;
        this.dm = damageMultiplier;
        
        // 3. Assign them
        this.width = width;
        this.height = height;
    }
}