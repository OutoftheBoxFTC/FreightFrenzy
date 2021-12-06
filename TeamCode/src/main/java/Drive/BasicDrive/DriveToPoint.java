package Drive.BasicDrive;

import Drive.FieldCentricDrive;
import Hardware.HardwareSystems.UGSystems.DrivetrainSystem;
import MathSystems.Angle;
import MathSystems.MathUtils;
import MathSystems.Position;
import MathSystems.Vector.Vector3;

public class DriveToPoint extends FieldCentricDrive {
    private final DrivetrainSystem drivetrainSystem;
    private final double speed, linTol;
    private final Angle rotTol;
    private final Position position, endpoint;

    public DriveToPoint(DrivetrainSystem drivetrainSystem, Position position, Position endpoint, double speed, double linTol, Angle rotTol){
        super(drivetrainSystem, position);
        this.drivetrainSystem = drivetrainSystem;
        this.position = position;
        this.endpoint = endpoint;
        this.speed = speed;
        this.linTol = linTol;
        this.rotTol = rotTol;
    }

    public DriveToPoint(DrivetrainSystem drivetrainSystem, Position position, Position endpoint, double speed){
        this(drivetrainSystem, position, endpoint, speed, 3, Angle.degrees(5));
    }

    public DriveToPoint(DrivetrainSystem drivetrainSystem, Position position, Position endpoint){
        this(drivetrainSystem, position, endpoint, 1, 3, Angle.degrees(5));
    }

    @Override
    public Vector3 getDriveVector() {
        Vector3 delta = endpoint.toVector3().subtract(position.toVector3());
        return new Vector3(delta.getVector2().normalize().scale(speed), MathUtils.sign(delta.getC()) * speed);
    }

    @Override
    public boolean shouldDeactivate() {
        return endpoint.getPos().distanceTo(position.getPos()) < linTol && MathUtils.getRotDist(position.getR(), endpoint.getR()).radians() < rotTol.radians();
    }
}
