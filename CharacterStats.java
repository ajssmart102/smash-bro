public class CharacterStats {
    public String name;
    public float walkSpeed;
    public float jumpForce;
    public float gravity;
    public float weight;
    public float damageMultiplier;

    public CharacterStats(String name, float walkSpeed, float jumpForce, float gravity, float weight, float damageMultiplier) {
        this.name = name;
        this.walkSpeed = walkSpeed;
        this.jumpForce = jumpForce;
        this.gravity = gravity;
        this.weight = weight;
        this.damageMultiplier = damageMultiplier;
    }
}
