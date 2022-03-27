package Odometry;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.localization.Localizer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import Hardware.HardwareSystems.FFSystems.DrivetrainSystem;

public class SinglePodOdometer implements Localizer {
    private DrivetrainSystem system;
    private double lastodopos = 0;

    private Pose2d pose = new Pose2d();

    public SinglePodOdometer(DrivetrainSystem system){
        this.system = system;
        this.lastodopos = system.getOdometryPosition();
    }

    @NotNull
    @Override
    public Pose2d getPoseEstimate() {
        return pose;
    }

    @Override
    public void setPoseEstimate(@NotNull Pose2d pose2d) {
        this.pose = pose2d;
    }

    @Nullable
    @Override
    public Pose2d getPoseVelocity() {
        return null;
    }

    @Override
    public void update() {
        double delta = system.getOdometryPosition() - lastodopos;
        double forw = Math.cos(system.getImuAngle().radians()) * delta;
        double strafe = Math.sin(system.getImuAngle().radians()) * delta;

        pose = pose.plus(new Pose2d(forw, strafe, 0));
        pose = new Pose2d(pose.getX(), pose.getY(), system.getImuAngle().radians());

        lastodopos = system.getOdometryPosition();
    }
}
