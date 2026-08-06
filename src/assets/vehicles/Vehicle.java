package assets.vehicles;

import java.awt.*;
import java.awt.geom.AffineTransform;
import javax.sound.sampled.*;
import javax.swing.ImageIcon;
import java.io.File;

public class Vehicle {
    public String name;
    protected int x, y, width, height, wheelSize;
    protected int frontWheelX, frontWheelY, rearWheelX, rearWheelY;
    protected Image bodyImage, wheelImage;
    protected double speed, currentRpm, maxRpm, baseTorque, mass, speedMax;
    protected int currentGear;
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
    protected String soundGearPath;

    protected int volumeEfeitos = 50;

    public Vehicle(String name, int width, int height, int wheelSize,
                   int frontWheelX, int frontWheelY, int rearWheelX, int rearWheelY,
                   double mass, double baseTorque, double maxRpm, double speedMax, double[] gearRatios,
                   String bodyPath, String wheelPath) {
        this.name = name;
        this.width = width;
        this.height = height;
        this.wheelSize = wheelSize;
        this.frontWheelX = frontWheelX;
        this.frontWheelY = frontWheelY;
        this.rearWheelX = rearWheelX;
        this.rearWheelY = rearWheelY;
        this.mass = mass;
        this.baseTorque = baseTorque;
        this.maxRpm = maxRpm;
        this.speedMax = speedMax;
        this.gearRatios = gearRatios;
        this.bodyImage = new ImageIcon(bodyPath).getImage();
        this.wheelImage = new ImageIcon(wheelPath).getImage();

        this.x = 50;
        this.y = 260;
        this.wheelAngle = 0;
        this.currentGear = 1;
        this.currentRpm = 0.0;
    }

    public void setAudioPaths(String start, String stop, String idle, String run, String gear) {
        this.engineStartSoundPath = start;
        this.engineStopSoundPath = stop;
        this.soundIdlePath = idle;
        this.soundDrivingPath = run;
        this.soundGearPath = gear;
    }

    public void setVolumeEfeitos(int volume) {
        this.volumeEfeitos = Math.max(0, Math.min(100, volume));
        updateEngineSound();
    }

    public int getWidth() {
        return this.width;
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
            if (engineStopSoundPath != null) playSound(engineStopSoundPath);
            stopEngineSounds();
            this.isEngineOn = false;
        } else {
            this.isEngineOn = true;
            if (engineStartSoundPath != null) playStartSequence(engineStartSoundPath);
            else startEngineSounds();
        }
    }

    private float calcularAtenuacaoDB() {
        if (this.volumeEfeitos >= 100) return 0.0f;
        if (this.volumeEfeitos <= 0) return -80.0f;
        return -40.0f * (1.0f - (this.volumeEfeitos / 100.0f));
    }

    protected void playSound(String path) {
        new Thread(() -> {
            try {
                File file = new File(path);
                if (!file.exists()) {
                    System.err.println("Erro: Arquivo de som não encontrado: " + file.getAbsolutePath());
                    return;
                }
                AudioInputStream ais = AudioSystem.getAudioInputStream(file);
                Clip clip = AudioSystem.getClip();
                clip.open(ais);

                try {
                    FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                    float finalVolumeDB = (this.volumeEfeitos == 0) ? gainControl.getMinimum() : 6.0f + calcularAtenuacaoDB();
                    if (finalVolumeDB > gainControl.getMaximum()) finalVolumeDB = gainControl.getMaximum();
                    if (finalVolumeDB < gainControl.getMinimum()) finalVolumeDB = gainControl.getMinimum();
                    gainControl.setValue(finalVolumeDB);
                } catch (Exception e) {
                }

                clip.start();
            } catch (Exception e) {
                System.err.println("Erro ao reproduzir o som: " + path);
                e.printStackTrace();
            }
        }).start();
    }

    private void playStartSequence(String startPath) {
        new Thread(() -> {
            try {
                File file = new File(startPath);
                AudioInputStream ais = AudioSystem.getAudioInputStream(file);
                engineStartClip = AudioSystem.getClip();
                engineStartClip.open(ais);

                setClipVolume(engineStartClip, 2.0f);
                engineStartClip.start();
                Thread.sleep(engineStartClip.getMicrosecondLength() / 1000);

                if (this.isEngineOn) startEngineSounds();
            } catch (Exception e) {
                if (this.isEngineOn) startEngineSounds();
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
            } catch (Exception e) {
            }
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
            float finalVolumeDB = (this.volumeEfeitos == 0) ? gainControl.getMinimum() : baseVolumeDB + calcularAtenuacaoDB();
            if (finalVolumeDB > gainControl.getMaximum()) finalVolumeDB = gainControl.getMaximum();
            if (finalVolumeDB < gainControl.getMinimum()) finalVolumeDB = gainControl.getMinimum();
            gainControl.setValue(finalVolumeDB);
        } catch (Exception e) {
        }
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
        } catch (Exception e) {
        }
    }

    public void updateEngineSound() {
        if (!isEngineOn) return;

        boolean isMoving = this.speed > 0.1;

        if (isMoving) {
            if (engineIdleClip != null && engineIdleClip.isRunning()) engineIdleClip.stop();
            if (engineDrivingClip != null && !engineDrivingClip.isRunning()) {
                engineDrivingClip.setFramePosition(0);
                engineDrivingClip.loop(Clip.LOOP_CONTINUOUSLY);
                engineDrivingClip.start();
            }
            float volume = (float) (Math.log10(currentRpm / 1000.0) * 10.0) - 10.0f;
            setClipVolume(engineDrivingClip, volume);
            applyPitch(engineDrivingClip, baseDrivingSampleRate, currentRpm);

        } else {
            if (engineDrivingClip != null && engineDrivingClip.isRunning()) engineDrivingClip.stop();
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

            if (soundGearPath != null && !soundGearPath.isEmpty()) {
                playSound(soundGearPath);
            }
        }
    }

    public double getTorqueFactor() {
        double faixaIdeal = maxRpm * 0.6;
        if (currentRpm < faixaIdeal) return 1.0;
        if (currentRpm > maxRpm) return 0.4;
        double progresso = (currentRpm - faixaIdeal) / (maxRpm - faixaIdeal);
        return 1.0 - (progresso * 0.6);
    }

    public void stallEngine() {
        this.isEngineOn = false;
        this.currentRpm = 0.0;
        this.speed = 0.0;

        stopEngineSounds();

        if (engineStopSoundPath != null && !engineStopSoundPath.isEmpty()) {
            playSound(engineStopSoundPath);
        }

        System.out.println("O motor apagou! Arrancada em " + currentGear + "ª marcha.");
    }

    public void updatePhysics(boolean isAccelerating, boolean isBraking) {
        if (!this.isEngineOn()) isAccelerating = false;

        if (this.isEngineOn() && isAccelerating && this.speed < 0.5 && this.currentGear > 1) {
            stallEngine();
            return;
        }

        if (currentGear == 0) {
            if (isAccelerating) this.currentRpm += 120.0;
            else if (isBraking) this.currentRpm -= 80.0;
            else this.currentRpm -= 40.0;

            double rpmMinimo = this.isEngineOn() ? 1000.0 : 0.0;
            if (currentRpm < rpmMinimo) this.currentRpm = rpmMinimo;
            if (currentRpm > maxRpm) this.currentRpm = maxRpm;

            this.speed -= 0.05;
            if (isBraking) this.speed -= 1.0;
            if (this.speed < 0) this.speed = 0;

        } else {
            double gearRatio = gearRatios[currentGear];

            if (isAccelerating) {
                double engineForce = (baseTorque * 30 * gearRatio) * getTorqueFactor();
                double dragForce = 0.42 * (this.speed * this.speed);
                double netForce = engineForce - dragForce;
                if (netForce < 0) netForce = 0;

                this.currentRpm += (netForce / mass);
            } else if (isBraking) {
                this.currentRpm -= 80;
            } else {
                double queda = this.isEngineOn() ? 25 : 50;
                this.currentRpm -= queda;
            }

            double rpmMinimo = this.isEngineOn() ? 1000.0 : 0.0;
            if (currentRpm < rpmMinimo) this.currentRpm = rpmMinimo;
            if (currentRpm > maxRpm) this.currentRpm = maxRpm;

            if (this.isEngineOn() && this.currentRpm <= 1000.0 && !isAccelerating) {
                this.speed = 0;
            } else {
                this.speed = (this.currentRpm / gearRatio) * 0.015;
            }
        }

        this.wheelAngle += this.speed * 0.04;
        this.updateEngineSound();
    }

    public void draw(Graphics2D g2d, Component component) {



        AffineTransform oldTransform = g2d.getTransform();

        // Desenhar roda traseira usando o offset dinâmico do XML
        g2d.translate(x + rearWheelX, y + rearWheelY);
        g2d.rotate(this.wheelAngle);
        g2d.drawImage(wheelImage, -wheelSize / 2, -wheelSize / 2, wheelSize, wheelSize, component);
        g2d.setTransform(oldTransform);

        // Desenhar roda dianteira usando o offset dinâmico do XML
        g2d.translate(x + frontWheelX, y + frontWheelY);
        g2d.rotate(this.wheelAngle);
        g2d.drawImage(wheelImage, -wheelSize / 2, -wheelSize / 2, wheelSize, wheelSize, component);
        g2d.setTransform(oldTransform);

        g2d.drawImage(bodyImage, x, y, width, height, component);


    }

    public double getCurrentSpeed() {
        return this.speed;
    }

    public double getCurrentRpm() {
        return this.currentRpm;
    }

    public int getCurrentGear() {
        return this.currentGear;
    }
}