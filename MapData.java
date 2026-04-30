import java.util.*;
import java.awt.Color;
import java.io.File;
import java.util.Scanner;

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
        MapData currentMap = null;

        File file = new File("maps.txt");
        
        // Debug: Print where the game is looking for the file
        System.out.println("Looking for maps.txt at: " + file.getAbsolutePath());

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();

                // Skip comments or empty lines
                if (line.isEmpty() || line.startsWith("#")) continue;

                if (line.startsWith("MAP:")) {
                    String[] parts = line.substring(4).split(",");
                    String name = parts[0].trim();
                    int r = Integer.parseInt(parts[1].trim());
                    int g = Integer.parseInt(parts[2].trim());
                    int b = Integer.parseInt(parts[3].trim());

                    currentMap = new MapData(name, new Color(r, g, b));
                    maps.add(currentMap);

                } else if (line.startsWith("PLAT:") && currentMap != null) {
                    String[] p = line.substring(5).split(",");
                    
                    // Convert all parts to trimmed strings to avoid NumberFormatErrors
                    if (p.length == 4) {
                        currentMap.platforms.add(new Platform(
                            Float.parseFloat(p[0].trim()), 
                            Float.parseFloat(p[1].trim()), 
                            Integer.parseInt(p[2].trim()), 
                            Integer.parseInt(p[3].trim())
                        ));
                    } else if (p.length == 8) {
                        currentMap.platforms.add(new Platform(
                            Float.parseFloat(p[0].trim()), 
                            Float.parseFloat(p[1].trim()), 
                            Integer.parseInt(p[2].trim()), 
                            Integer.parseInt(p[3].trim()),
                            Float.parseFloat(p[4].trim()), 
                            Float.parseFloat(p[5].trim()),
                            Integer.parseInt(p[6].trim()), 
                            Integer.parseInt(p[7].trim())
                        ));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading maps.txt: " + e.getMessage());
            e.printStackTrace();
            
            // Emergency Fallback so the game still runs
            if (maps.isEmpty()) {
                MapData fallback = new MapData("Fallback Stage", Color.DARK_GRAY);
                fallback.platforms.add(new Platform(200, 500, 880, 30));
                maps.add(fallback);
            }
        }

        return maps;
    }
}