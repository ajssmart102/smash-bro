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

        // 3. Pokémon Stadium 2 (Wide floor, platforms move slightly side-to-side)
        MapData ps2 = new MapData("Pokémon Stadium 2", new Color(40, 60, 100));
        ps2.platforms.add(new Platform(150, 520, 980, 40)); 
        // Side platforms move slowly left/right
        ps2.platforms.add(new Platform(320, 380, 180, 15, 0.8f, 0, 40, 0)); 
        ps2.platforms.add(new Platform(780, 380, 180, 15, -0.8f, 0, 40, 0)); 
        maps.add(ps2);

        // 4. Smashville (The moving platform center-piece)
        MapData sv = new MapData("Smashville", new Color(100, 130, 80));
        sv.platforms.add(new Platform(250, 540, 780, 35)); 
        // Platform travels a long distance across the stage
        sv.platforms.add(new Platform(515, 370, 250, 20, 2.5f, 0, 250, 0)); 
        maps.add(sv);

        // 5. Hyrule Castle (Static tiers)
        MapData hyrule = new MapData("Hyrule Castle", new Color(40, 40, 40));
        hyrule.platforms.add(new Platform(100, 580, 1080, 40)); 
        hyrule.platforms.add(new Platform(250, 420, 250, 20));  
        hyrule.platforms.add(new Platform(780, 420, 250, 20));  
        hyrule.platforms.add(new Platform(540, 300, 200, 20));  
        hyrule.platforms.add(new Platform(100, 320, 150, 20));  
        maps.add(hyrule);

        // 6. Fountain of Dreams (Vertical Moving Platforms)
        MapData fod = new MapData("Fountain of Dreams", new Color(80, 40, 90));
        fod.platforms.add(new Platform(250, 530, 780, 35));
        // These platforms move up and down at different intervals
        fod.platforms.add(new Platform(300, 420, 160, 20, 0, 1.2f, 0, 100));
        fod.platforms.add(new Platform(820, 420, 160, 20, 0, -1.2f, 0, 100));
        fod.platforms.add(new Platform(560, 250, 160, 20)); // Static top plat
        maps.add(fod);

        return maps;
    }
}