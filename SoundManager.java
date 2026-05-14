import javax.sound.sampled.*;
import java.io.File;

public class SoundManager 
{
    public static void play(String filename) 
    {
        new Thread(() -> 
        {
            try 
            {
                // Adjust path based on where you put your .wav files
                File file = new File("resources/sounds/" + filename);
                AudioInputStream stream = AudioSystem.getAudioInputStream(file);
                Clip clip = AudioSystem.getClip();
                clip.open(stream);
                clip.start();
            } 
            catch (Exception e) 
            {
                System.err.println("Sound error: " + filename + " - " + e.getMessage());
            }
        }).start();
    }
}