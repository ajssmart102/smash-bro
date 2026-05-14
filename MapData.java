import java.util.*;
import java.awt.Color;
import java.io.File;
import java.util.Scanner;

public class MapData 
{
    public String name;
    public List<Platform> platforms;
    public Color backgroundColor;

    public MapData(String name, Color bg) 
    {
        this.name = name;
        this.backgroundColor = bg;
        this.platforms = new ArrayList<>();
    }

    public static List<MapData> getAllMaps() 
    {
        List<MapData> maps = new ArrayList<>();
        MapData currentMap = null;

        File file = new File("maps.txt");
        
        // Debug: Print where the game is looking for the file
        System.out.println("Looking for maps.txt at: " + file.getAbsolutePath());

        try (Scanner scanner = new Scanner(file)) 
        {
            while (scanner.hasNextLine()) 
            {
                String line = scanner.nextLine().trim();

                // Skip comments or empty lines
                if (line.isEmpty() || line.startsWith("#")) continue;

                if (line.startsWith("MAP:")) 
                {
                    String[] parts = line.substring(4).split(",");
                    String name = parts[0].trim();
                    int r = Integer.parseInt(parts[1].trim());
                    int g = Integer.parseInt(parts[2].trim());
                    int b = Integer.parseInt(parts[3].trim());

                    currentMap = new MapData(name, new Color(r, g, b));
                    maps.add(currentMap);

                } 
                else if (line.startsWith("PLAT:") && currentMap != null) 
                    {
                    String[] p = line.substring(5).split(",");
                    Platform plat = null;
                    
                    // --- STATIC PLATFORMS (4 or 5 parameters) ---
                    if (p.length == 4 || p.length == 5) 
                    {
                        plat = new Platform(
                            Float.parseFloat(p[0].trim()), 
                            Float.parseFloat(p[1].trim()), 
                            (int)Float.parseFloat(p[2].trim()), 
                            (int)Float.parseFloat(p[3].trim())
                        );
                        // Check for main stage flag (the 5th parameter)
                        if (p.length == 5) 
                        {
                            plat.isMainStage = p[4].trim().equals("1");
                        }
                    } 
                    // --- MOVING PLATFORMS (8 or 9 parameters) ---
                    else if (p.length == 8 || p.length == 9) 
                    {
                        plat = new Platform(
                            Float.parseFloat(p[0].trim()), 
                            Float.parseFloat(p[1].trim()), 
                            (int)Float.parseFloat(p[2].trim()), 
                            (int)Float.parseFloat(p[3].trim()),
                            Float.parseFloat(p[4].trim()), 
                            Float.parseFloat(p[5].trim()),
                            (int)Float.parseFloat(p[6].trim()), 
                            (int)Float.parseFloat(p[7].trim())
                        );
                        // Check for main stage flag (the 9th parameter)
                        if (p.length == 9) 
                        {
                            plat.isMainStage = p[8].trim().equals("1");
                        }
                    }

                    if (plat != null) 
                    {
                        currentMap.platforms.add(plat);
                    }
                }
            }
        } catch (Exception e) 
        {
            System.err.println("Error reading maps.txt: " + e.getMessage());
            e.printStackTrace();
            
            // Emergency Fallback
            if (maps.isEmpty()) 
            {
                MapData fallback = new MapData("Fallback Stage", Color.DARK_GRAY);
                Platform mainFloor = new Platform(200, 500, 880, 30);
                mainFloor.isMainStage = true; // Make sure fallback is grabbable!
                fallback.platforms.add(mainFloor);
                maps.add(fallback);
            }
        }

        return maps;
    }
}