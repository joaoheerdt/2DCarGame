package sound;

import javax.sound.sampled.*;
import java.io.File;

public class AudioManager {
    private Clip bgmClip;
    private FloatControl volumeControl;
    private int volumePercent = 80;

    public void playMusic(String path) {
        try {
            File file = new File(path);
            AudioInputStream ais = AudioSystem.getAudioInputStream(file);
            bgmClip = AudioSystem.getClip();
            bgmClip.open(ais);

            if (bgmClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                volumeControl = (FloatControl) bgmClip.getControl(FloatControl.Type.MASTER_GAIN);
                atualizarVolume();
            }

            bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
            bgmClip.start();
        } catch (Exception e) {
            System.err.println("Música de fundo não encontrada ou erro ao tocar: " + e.getMessage());
        }
    }

    public void setVolume(int volume) {
        this.volumePercent = Math.max(0, Math.min(100, volume));
        atualizarVolume();
    }

    private void atualizarVolume() {
        if (volumeControl != null) {
            if (volumePercent == 0) {
                volumeControl.setValue(volumeControl.getMinimum()); // Mudo total
            } else if (volumePercent >= 100) {
                volumeControl.setValue(0.0f); // Volume original do arquivo
            } else {
                // Reduz agressivamente até -40dB (que é quase inaudível)
                float atenuacao = -40.0f * (1.0f - (volumePercent / 100.0f));
                volumeControl.setValue(Math.max(volumeControl.getMinimum(), atenuacao));
            }
        }
    }
    }
