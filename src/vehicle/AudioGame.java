package vehicle;

import javax.sound.sampled.*;
import java.io.File;

public class AudioGame {

    public static void playSoundGear(String path) {
        try {
            File soundFile = new File("src/assets/vehicles/sound/Gear.wav");
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
        } catch (Exception e) {
            System.out.println("Error playing sound: " + e.getMessage());
        }
    }
}