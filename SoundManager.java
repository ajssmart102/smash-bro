import javax.sound.sampled.*;
import java.io.File;

public class SoundManager 
{
    // These match the folder and naming style you downloaded
    private static final String PATH = "resources/sounds/";
    private static final String PREFIX = "vc_menu_narration_";
    private static final String EXT = ".wav";

    /**
     * Use this for the announcer.
     * Example: SoundManager.announce("mario"); 
     * Becomes: resources/sounds/vc_menu_narration_mario.wav
     */
    public static void announce(String keyword) 
    {
        // Clean up the string (lowercase and remove spaces)
        String fileName = PREFIX + keyword.toLowerCase().trim().replace(" ", "") + EXT;
        play(fileName);
    }

    /**
     * Plays any specific file by name
     */
    public static void play(String fileName) 
    {
        new Thread(() -> 
        {
            try {
                File file = new File(PATH + fileName);
                if (!file.exists()) 
                {
                    System.err.println("File not found: " + file.getAbsolutePath());
                    return;
                }
                
                AudioInputStream stream = AudioSystem.getAudioInputStream(file);
                Clip clip = AudioSystem.getClip();
                clip.open(stream);
                clip.start();
                
                // Allow the clip to finish before the thread closes
                Thread.sleep(clip.getMicrosecondLength() / 1000);
            } 
            catch (Exception e) 
            {
                System.err.println("Error playing " + fileName + ": " + e.getMessage());
            }
        })
        .start();
    }
}