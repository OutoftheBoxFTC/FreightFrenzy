package Odometry;

import Hardware.HardwareSystems.FFSystems.DrivetrainSystem;
import Hardware.HardwareSystems.FFSystems.OdometrySystem;
import MathSystems.Position;
import State.Action.Action;

public class FusionOdometer implements Action {
    private static final double INCH_PER_PULSE = ((96/25.4) * Math.PI) / (384.5);

    private OdometrySystem odoSystem;
    private DrivetrainSystem dtSystem;
    private Position position, velocity;

    private double lastPos = 0;

    public FusionOdometer(OdometrySystem odometrySystem, DrivetrainSystem drivetrainSystem, Position position, Position velocity){
        this.odoSystem = odometrySystem;
        this.dtSystem = drivetrainSystem;
        this.position = position;
        this.velocity = velocity;
    }

    @Override
    public void update() {
        this.position.setR(dtSystem.getImuAngle());
        this.position.setX(odoSystem.getLeftDist());
        double curr = (odoSystem.getFl() + odoSystem.getBl()/2.0);
        double dE = curr - lastPos;
        dE = dE * Math.cos(dtSystem.getImuAngle().radians());
        dE = dE * INCH_PER_PULSE;
        this.position.setY(this.position.getY() + dE);

        lastPos = curr;
    }
}