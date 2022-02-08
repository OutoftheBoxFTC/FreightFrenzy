package Odometry;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.localization.Localizer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import Hardware.HardwareSystems.FFSystems.DrivetrainSystem;
import Hardware.HardwareSystems.FFSystems.OdometrySystem;
import MathSystems.Position;

public class FFFusionOdometer implements Localizer {
    private Pose2d pose2d;

    private static final double INCH_PER_PULSE = ((96/25.4) * Math.PI) / (384.5);

    private OdometrySystem odoSystem;
    private DrivetrainSystem dtSystem;

    private double lastPos = 0;
    private boolean use2mForward = false;

    public FFFusionOdometer(OdometrySystem odometrySystem, DrivetrainSystem drivetrainSystem){
        this.odoSystem = odometrySystem;
        this.dtSystem = drivetrainSystem;
        pose2d = new Pose2d(0, 0, 0);
    }

    @NotNull
    @Override
    public Pose2d getPoseEstimate() {
        return pose2d;
    }

    @Override
    public void setPoseEstimate(@NotNull Pose2d pose2d) {
        this.pose2d = pose2d;
    }

    @Nullable
    @Override
    public Pose2d getPoseVelocity() {
        return null;
    }

    @Override
    public void update() {
        double left = odoSystem.getLeftDist();
        double angle = dtSystem.getImuAngle().radians();

        double x = 0;
        double y = 0;

        if(left < 100) {

            left = Math.cos(angle) * left;

            x = left;
        }
        double curr = -(odoSystem.getFl() + odoSystem.getBl()/2.0);
        double dE = curr - lastPos;
        dE = dE * Math.cos(dtSystem.getImuAngle().radians());
        dE = dE * INCH_PER_PULSE;
        y = pose2d.getY() + dE;
        if(use2mForward) {
            if (odoSystem.getForwardDist() < 90 && odoSystem.getForwardDist() != 0) {
                double forw = odoSystem.getForwardDist();
                forw = Math.cos(angle) * forw;

                y = 58 - forw;
            }
        }
        lastPos = curr;

        pose2d = new Pose2d(x, y, angle);
    }
}
