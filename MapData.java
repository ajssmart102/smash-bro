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

        // 1. Battlefield (Standard Tri-Plat)
        MapData m1 = new MapData("Battlefield", new Color(30, 30, 50));
        m1.platforms.add(new Platform(200, 500, 880, 30)); 
        m1.platforms.add(new Platform(350, 350, 150, 20)); 
        m1.platforms.add(new Platform(780, 350, 150, 20)); 
        m1.platforms.add(new Platform(565, 220, 150, 20)); 
        maps.add(m1);

        // 2. The Void (Final Destination Style)
        MapData m2 = new MapData("The Void", new Color(20, 0, 20));
        m2.platforms.add(new Platform(240, 550, 800, 40));
        maps.add(m2);

        // 3. Pokémon Stadium (Wide floor, two side platforms)
        MapData ps = new MapData("Pokémon Stadium", new Color(45, 65, 120));
        ps.platforms.add(new Platform(150, 520, 980, 40)); // Massive floor
        ps.platforms.add(new Platform(320, 370, 180, 15)); // Left flat plat
        ps.platforms.add(new Platform(780, 370, 180, 15)); // Right flat plat
        maps.add(ps);

        // 4. Smashville (Dynamic: One platform moving across the whole stage)
        MapData sv = new MapData("Smashville", new Color(100, 140, 80));
        sv.platforms.add(new Platform(250, 540, 780, 35)); // Main floor
        // Moves horizontally across the stage
        sv.platforms.add(new Platform(515, 360, 250, 20, 2.5f, 0, 280, 0)); 
        maps.add(sv);

        // 5. Yoshi's Story (Narrow with high, sloped-feel platforms)
        MapData ys = new MapData("Yoshi's Story", new Color(120, 80, 140));
        ys.platforms.add(new Platform(340, 530, 600, 30)); // Smaller main floor
        ys.platforms.add(new Platform(380, 380, 140, 15)); // Low left
        ys.platforms.add(new Platform(760, 380, 140, 15)); // Low right
        ys.platforms.add(new Platform(570, 240, 140, 15)); // High center
        maps.add(ys);

        // 6. Fountain of Dreams (Vertical movement)
        MapData fod = new MapData("Fountain of Dreams", new Color(70, 40, 90));
        fod.platforms.add(new Platform(240, 530, 800, 35)); 
        // These platforms move up and down at different speeds
        fod.platforms.add(new Platform(300, 400, 160, 18, 0, 1.5f, 0, 120));
        fod.platforms.add(new Platform(820, 400, 160, 18, 0, -1.5f, 0, 120));
        fod.platforms.add(new Platform(560, 260, 160, 18)); // Static top
        maps.add(fod);

        return maps;
    }
}