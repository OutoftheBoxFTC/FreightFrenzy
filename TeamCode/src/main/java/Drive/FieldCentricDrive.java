package Drive;

import Hardware.HardwareSystems.UGSystems.DrivetrainSystem;
import MathSystems.MathUtils;
import MathSystems.Position;
import MathSystems.Vector.Vector3;
import State.Action.Action;

public abstract class FieldCentricDrive implements Action {
    private final DrivetrainSystem drivetrainSystem;
    private final Position position;

    public FieldCentricDrive(DrivetrainSystem drivetrainSystem, Position position){
        this.drivetrainSystem = drivetrainSystem;
        this.position = position;
    }

    @Override
    public void update() {
        Vector3 direction = getDriveVector();
        double r = direction.getVector2().length();
        double theta = Math.atan2(direction.getB(), direction.getA()) - position.getR().radians();
        drivetrainSystem.setPower(new Vector3(MathUtils.toCartesian(r, theta), direction.getC()));
    }

    public abstract Vector3 getDriveVector();
}
