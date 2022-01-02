package Drive.AdvDrive.GVF;

import com.qualcomm.robotcore.util.RobotLog;

import Drive.DriveConstants;
import Hardware.HardwareSystems.UGSystems.DrivetrainSystem;
import MathSystems.Angle;
import MathSystems.MathUtils;
import MathSystems.Position;
import MathSystems.Vector.Vector2;
import MathSystems.Vector.Vector3;
import State.Action.Action;
import Utils.PathUtils.Markers.BasicPathMarker;
import Utils.PathUtils.Markers.PathMarkerBase;
import Utils.PathUtils.Path;
import Utils.PathUtils.PathUtil;
import Utils.PathUtils.Profiling.LinearProfile;

public class GVFDrive implements Action {
    private final DrivetrainSystem drivetrainSystem;
    private final double speed, linTol, kps;
    private final Angle rotTol;
    private final Position position;
    private final Path path;
    private double lastProject;

    public GVFDrive(DrivetrainSystem drivetrainSystem, Position position, Path path, double speed, double kps, double linTol, Angle rotTol){
        this.drivetrainSystem = drivetrainSystem;
        this.position = position;
        this.path = path;
        this.speed = speed;
        this.kps = kps;
        this.linTol = linTol;
        this.rotTol = rotTol;
        this.lastProject = 0;
    }

    public GVFDrive(DrivetrainSystem drivetrainSystem, Position position, Path path, double speed){
        this(drivetrainSystem, position, path, speed, 0, 2, Angle.degrees(5));
    }

    public GVFDrive(DrivetrainSystem drivetrainSystem, Position position, Path path, double speed, double kps){
        this(drivetrainSystem, position, path, speed, kps, 2, Angle.degrees(5));
    }

    public GVFDrive(DrivetrainSystem drivetrainSystem, Position position, Path path){
        this(drivetrainSystem, position, path, 1, 0.15, 2, Angle.degrees(5));
    }

    @Override
    public void update() {
        double val = PathUtil.projectPosNew(position, path);
        Position closestPoint = path.get(val);
        Vector2 posDelta = closestPoint.getPos().subtract(position.getPos());

        for(PathMarkerBase marker : path.getMarkers()){
            if(marker.pathPos <= val){
                marker.call();
            }
        }

        if(posDelta.length() > 3 || Double.isNaN(val)){
            //Double check the project was right because we are so far away from where we expect
            val = PathUtil.project(position, path);
            closestPoint = path.get(val);
            posDelta = closestPoint.getPos().subtract(position.getPos());
        }

        double pathDist = posDelta.length();
        double pathReturnVel = Math.sqrt(2 * DriveConstants.MAX_LIN_ACCEL * pathDist);

        Vector2 tang = path.deriv(val).getVector2().normalize();

        Vector2 norm = tang.rotate(Angle.degrees(-90)).normalize().scale(Math.min(pathReturnVel, DriveConstants.MAX_LIN_SPEED));

        double weight = MathUtils.clamp(posDelta.length() * kps, 0, 1);

        if(closestPoint.getPos().distanceTo(path.getEndpoint().getPos()) < linTol){
            weight = 1;
            norm = path.getEndpoint().getPos().subtract(position.getPos()).normalize().scale(DriveConstants.MAX_LIN_ACCEL);
        }

        Vector2 vel = norm.subtract(tang).scale(weight).add(tang);

        double r = vel.length();
            double theta = Math.atan2(vel.getB(), vel.getA()) - position.getR().radians();
        Vector2 rotatedDist = MathUtils.toCartesian(r, theta);

        Angle angDelta = MathUtils.getRotDist(position.getR(), closestPoint.getR());

        double xSpeed = rotatedDist.getA();

        xSpeed = xSpeed / DriveConstants.MAX_LIN_SPEED;

        double ySpeed = rotatedDist.getB();

        ySpeed = ySpeed / DriveConstants.MAX_LIN_SPEED;

        double rSpeed = Math.min(DriveConstants.MAX_ROT_SPEED, Math.sqrt(2 * Math.abs(angDelta.radians()) * DriveConstants.MAX_ROT_ACCEL));

        rSpeed = rSpeed / DriveConstants.MAX_ROT_SPEED;

        Vector2 linVel = new Vector2(xSpeed * GVFConstants.strafeGain * speed, ySpeed * GVFConstants.forwardGain * speed);

        drivetrainSystem.setPower(new Vector3(linVel, rSpeed * GVFConstants.rotationGain * speed * MathUtils.sign(-angDelta.radians())));

        lastProject = val;
    }

    @Override
    public boolean shouldDeactivate() {
        return path.getEndpoint().getPos().distanceTo(position.getPos()) < linTol && MathUtils.getRotDist(position.getR(), path.getEndpoint().getR()).radians() < rotTol.radians();
    }

    @Override
    public void onEnd() {
        for(PathMarkerBase marker : path.getMarkers()){
            if(marker.pathPos >= 1){
                marker.call();
            }
        }
    }
}
