package assets.vehicles;

import java.awt.*;
import javax.sound.sampled.*;
import javax.swing.ImageIcon;
import java.io.File;

public abstract class Vehicle {
    protected int x, y, width, height, wheelSize;
    protected Image bodyImage, wheelImage;
    protected double speed, currentRpm, maxRpm, baseTorque, mass, speedMax;
    protected int currentGear, maxSpeed;
    protected double[] gearRatios;
    protected double wheelAngle;
    protected boolean isEngineOn;

    private Clip engineStartClip;
    private Clip engineIdleClip;
    private Clip engineDrivingClip;

    private float baseIdleSampleRate = 44100.0f;
    private float baseDrivingSampleRate = 44100.0f;

    protected String engineStartSoundPath;
    protected String engineStopSoundPath;
    protected String soundIdlePath;
    protected String soundDrivingPath;


    protected int volumeEfeitos = 80;

    public Vehicle(int x, int y, int width, int height, int wheelSize, String bodyPath, String wheelPath) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.wheelSize = wheelSize;
        this.bodyImage = new ImageIcon(bodyPath).getImage();
        this.wheelImage = new ImageIcon(wheelPath).getImage();
        this.wheelAngle = 0;
    }

    public void setVolumeEfeitos(int volume) {
        this.volumeEfeitos = Math.max(0, Math.min(100, volume));
        updateEngineSound();
    }

    public boolean isEngineOn() {
        return isEngineOn;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void toggleEngine() {
        if (this.isEngineOn) {
            if (engineStopSoundPath != null) {
                playSound(engineStopSoundPath);
            }
            stopEngineSounds();
            this.isEngineOn = false;
        } else {
            this.isEngineOn = true;
            if (engineStartSoundPath != null) {
                playStartSequence(engineStartSoundPath);
            } else {
                startEngineSounds();
            }
        }
    }

    private float calcularAtenuacaoDB() {
        if (this.volumeEfeitos >= 100) return 0.0f;
        if (this.volumeEfeitos <= 0) return -80.0f; // Mute
        return -40.0f * (1.0f - (this.volumeEfeitos / 100.0f));
    }

    protected void playSound(String path) {
        new Thread(() -> {
            try {
                File file = new File(path);
                AudioInputStream ais = AudioSystem.getAudioInputStream(file);
                Clip clip = AudioSystem.getClip();
                clip.open(ais);

                try {
                    FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                    float finalVolumeDB;

                    if (this.volumeEfeitos == 0) {
                        finalVolumeDB = gainControl.getMinimum();
                    } else {
                        // Aplica a atenuação ao volume máximo do som rápido (como a marcha)
                        finalVolumeDB = 6.0f + calcularAtenuacaoDB();
                    }

                    if (finalVolumeDB > gainControl.getMaximum()) finalVolumeDB = gainControl.getMaximum();
                    if (finalVolumeDB < gainControl.getMinimum()) finalVolumeDB = gainControl.getMinimum();
                    gainControl.setValue(finalVolumeDB);
                } catch (Exception e) {}

                clip.start();
            } catch (Exception e) {}
        }).start();
    }

    private void playStartSequence(String startPath) {
        new Thread(() -> {
            try {
                File file = new File(startPath);
                AudioInputStream ais = AudioSystem.getAudioInputStream(file);
                engineStartClip = AudioSystem.getClip();
                engineStartClip.open(ais);

                // Aplica o volume inicial
                setClipVolume(engineStartClip, 2.0f);

                engineStartClip.start();
                Thread.sleep(engineStartClip.getMicrosecondLength() / 1000);

                if (this.isEngineOn) {
                    startEngineSounds();
                }
            } catch (Exception e) {
                if (this.isEngineOn) {
                    startEngineSounds();
                }
            }
        }).start();
    }

    private void startEngineSounds() {
        new Thread(() -> {
            try {
                if (soundIdlePath != null) {
                    File file = new File(soundIdlePath);
                    AudioInputStream ais = AudioSystem.getAudioInputStream(file);
                    engineIdleClip = AudioSystem.getClip();
                    engineIdleClip.open(ais);
                    try {
                        FloatControl rateControl = (FloatControl) engineIdleClip.getControl(FloatControl.Type.SAMPLE_RATE);
                        baseIdleSampleRate = rateControl.getValue();
                    } catch (Exception e) {
                        baseIdleSampleRate = ais.getFormat().getSampleRate();
                    }
                }

                if (soundDrivingPath != null) {
                    File file = new File(soundDrivingPath);
                    AudioInputStream ais = AudioSystem.getAudioInputStream(file);
                    engineDrivingClip = AudioSystem.getClip();
                    engineDrivingClip.open(ais);
                    try {
                        FloatControl rateControl = (FloatControl) engineDrivingClip.getControl(FloatControl.Type.SAMPLE_RATE);
                        baseDrivingSampleRate = rateControl.getValue();
                    } catch (Exception e) {
                        baseDrivingSampleRate = ais.getFormat().getSampleRate();
                    }
                }

                if (engineIdleClip != null && this.isEngineOn) {
                    setClipVolume(engineIdleClip, 2.0f);
                    engineIdleClip.loop(Clip.LOOP_CONTINUOUSLY);
                    engineIdleClip.start();
                }
            } catch (Exception e) {}
        }).start();
    }

    public void stopEngineSounds() {
        if (engineStartClip != null && engineStartClip.isRunning()) {
            engineStartClip.stop();
            engineStartClip.close();
        }
        if (engineIdleClip != null && engineIdleClip.isRunning()) {
            engineIdleClip.stop();
            engineIdleClip.close();
        }
        if (engineDrivingClip != null && engineDrivingClip.isRunning()) {
            engineDrivingClip.stop();
            engineDrivingClip.close();
        }
    }

    private void setClipVolume(Clip clip, float baseVolumeDB) {
        if (clip == null || !clip.isOpen()) return;

        try {
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float finalVolumeDB;

            if (this.volumeEfeitos == 0) {
                finalVolumeDB = gainControl.getMinimum();
            } else {
                // Soma a atenuação agressiva à base do volume calculada pela física do carro
                finalVolumeDB = baseVolumeDB + calcularAtenuacaoDB();
            }

            if (finalVolumeDB > gainControl.getMaximum()) finalVolumeDB = gainControl.getMaximum();
            if (finalVolumeDB < gainControl.getMinimum()) finalVolumeDB = gainControl.getMinimum();
            gainControl.setValue(finalVolumeDB);
        } catch (Exception e) {}
    }

    private void applyPitch(Clip clip, float baseSampleRate, double rpm) {
        if (clip == null || !clip.isRunning()) return;
        try {
            FloatControl rateControl = (FloatControl) clip.getControl(FloatControl.Type.SAMPLE_RATE);
            float ratio = (float) (rpm / 1000.0);
            if (ratio < 0.5f) ratio = 0.5f;
            float newRate = baseSampleRate * ratio;
            if (newRate > rateControl.getMaximum()) newRate = rateControl.getMaximum();
            if (newRate < rateControl.getMinimum()) newRate = rateControl.getMinimum();
            rateControl.setValue(newRate);
        } catch (Exception e) {}
    }

    public void updateEngineSound() {
        if (!isEngineOn) return;

        boolean isMoving = this.speed > 0.1;

        if (isMoving) {
            if (engineIdleClip != null && engineIdleClip.isRunning()) {
                engineIdleClip.stop();
            }
            if (engineDrivingClip != null && !engineDrivingClip.isRunning()) {
                engineDrivingClip.setFramePosition(0);
                engineDrivingClip.loop(Clip.LOOP_CONTINUOUSLY);
                engineDrivingClip.start();
            }

            float volume = (float) (Math.log10(currentRpm / 1000.0) * 10.0) - 10.0f;
            setClipVolume(engineDrivingClip, volume);
            applyPitch(engineDrivingClip, baseDrivingSampleRate, currentRpm);

        } else {
            if (engineDrivingClip != null && engineDrivingClip.isRunning()) {
                engineDrivingClip.stop();
            }
            if (engineIdleClip != null && !engineIdleClip.isRunning()) {
                engineIdleClip.setFramePosition(0);
                engineIdleClip.loop(Clip.LOOP_CONTINUOUSLY);
                engineIdleClip.start();
            }

            setClipVolume(engineIdleClip, 2.0f);
            applyPitch(engineIdleClip, baseIdleSampleRate, currentRpm);
        }
    }

    public void changeGear(int newGear) {
        if (newGear >= 0 && newGear < gearRatios.length) {
            double oldRatio = (currentGear == 0) ? 1.0 : gearRatios[currentGear];
            double newRatio = (newGear == 0) ? 1.0 : gearRatios[newGear];
            this.currentRpm *= (newRatio / oldRatio);
            this.currentGear = newGear;
            playSound("src/assets/vehicles/fusca/sound/Gear.wav");
        }
    }

    public double getTorqueFactor() {
        if (currentRpm < 2500.00) return 0.5;
        if (currentRpm > (maxRpm - 500.00)) return 0.6;
        return 1.0;
    }

    public abstract void updatePhysics(boolean isAccelerating, boolean isBraking);

    public abstract void draw(Graphics2D g2d, Component component);

    public double getCurrentSpeed() { return this.speed; }
    public double getCurrentRpm() { return this.currentRpm; }
    public int getCurrentGear() { return this.currentGear; }
}