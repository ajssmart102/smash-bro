import java.util.*;
import java.awt.Color;

public class MapData {
    public String name;
    public List<Platform> platforms;
    public Color backgroundColor;

    public MapData(String name, Color bg) {
        this.name = name;
        this.backgroundColor = bg;
        this.platforms = new ArrayList<>();
    }

    public static List<MapData> getAllMaps() {
        List<MapData> maps = new ArrayList<>();

        // Map 1: Battlefield Style
        MapData m1 = new MapData("Battlefield", new Color(30, 30, 50));
        m1.platforms.add(new Platform(200, 500, 880, 30)); // Main floor
        m1.platforms.add(new Platform(350, 350, 150, 20)); // Left plat
        m1.platforms.add(new Platform(780, 350, 150, 20)); // Right plat
        m1.platforms.add(new Platform(565, 220, 150, 20)); // Top plat
        maps.add(m1);

        // Map 2: Final Destination (Flat)
        MapData m2 = new MapData("The Void", new Color(20, 0, 20));
        m2.platforms.add(new Platform(240, 550, 800, 40));
        maps.add(m2);

        // Map 3: Dual Heights
        MapData m3 = new MapData("High Ground", new Color(50, 40, 30));
        m3.platforms.add(new Platform(100, 600, 400, 30));
        m3.platforms.add(new Platform(780, 600, 400, 30));
        m3.platforms.add(new Platform(440, 380, 400, 30));
        maps.add(m3);

        return maps;
    }
}