package assets.vehicles.fusca;

import assets.vehicles.Vehicle;

import java.awt.*;
import java.awt.geom.AffineTransform;

public class Fusca extends Vehicle {

    public Fusca() {
        super(50, 280, 300, 150, 67, "src/assets/vehicles/fusca/FuscaVermelho.png", "src/assets/vehicles/fusca/RodaFusca.png");
        this.maxRpm = 5000.0;
        this.baseTorque = 150.0;
        this.mass = 800.0;
        this.gearRatios = new double[]{1.0, 3.8, 2.0, 1.2, 0.9};
        this.currentGear = 1;
        this.currentRpm = 0.0;
        this.speedMax = 110;

        this.engineStartSoundPath = "src/assets/vehicles/fusca/sound/fusca_start1600.wav";
        this.soundIdlePath = "src/assets/vehicles/fusca/sound/fusca_idle1600.wav";
        this.soundDrivingPath = "src/assets/vehicles/fusca/sound/fusca_run.wav";
        this.engineStopSoundPath = "src/assets/vehicles/fusca/sound/fusca_stop.wav";
    }

    @Override
    public double getTorqueFactor() {
        if (currentRpm < 3000) return 1.0;
        if (currentRpm > 5000) return 0.4;

        double progresso = (currentRpm - 3000) / 2000.0;
        return 1.0 - (progresso * 0.6);
    }

    @Override
    public void updatePhysics(boolean isAccelerating, boolean isBraking) {
        if (!this.isEngineOn()) {
            isAccelerating = false;
        }

        if (currentGear == 0) {
            if (isAccelerating) {
                this.currentRpm += 120.0;
            } else if (isBraking) {
                this.currentRpm -= 80.0;
            } else {
                this.currentRpm -= 40.0;
            }

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

    @Override
    public void draw(Graphics2D g2d, Component component) {
        g2d.drawImage(bodyImage, x, y, width, height, component);

        int backWheelX = x + 40;
        int frontWheelX = x + 200;
        int wheelY = y + 80;

        AffineTransform oldTransform = g2d.getTransform();

        g2d.translate(backWheelX + wheelSize / 2.0, wheelY + wheelSize / 2.0);
        g2d.rotate(this.wheelAngle);
        g2d.drawImage(wheelImage, -wheelSize / 2, -wheelSize / 2, wheelSize, wheelSize, component);
        g2d.setTransform(oldTransform);

        g2d.translate(frontWheelX + wheelSize / 2.0, wheelY + wheelSize / 2.0);
        g2d.rotate(this.wheelAngle);
        g2d.drawImage(wheelImage, -wheelSize / 2, -wheelSize / 2, wheelSize, wheelSize, component);
        g2d.setTransform(oldTransform);
    }
}