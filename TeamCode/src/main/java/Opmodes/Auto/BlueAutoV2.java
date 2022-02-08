package Opmodes.Auto;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;

import Hardware.HardwareSystems.FFSystems.Actions.BlueGoalAutoActions;
import Odometry.FFFusionOdometer;
import Opmodes.BasicOpmode;
import RoadRunner.drive.SampleMecanumDrive;
import State.Action.Action;
import State.Action.ActionController;
import State.Action.ActionQueue;
import State.Action.InstantAction;
import Utils.OpmodeData;
import Utils.OpmodeStatus;
import Vision.ApriltagDetector;

public class BlueAutoV2 extends BasicOpmode {
    public static BlueAuto.LEVEL level = BlueAuto.LEVEL.MED;

    long started = 0;

    @Override
    public void setup() {
        ApriltagDetector detector = new ApriltagDetector(hardwareMap);
        ActionController.addAction(detector);

        ActionController.addAction(() -> OpmodeData.getInstance().setExtensionPos(hardware.getTurretSystem().getExtensionPosition()));

        ActionController.addAction(() -> {
            telemetry.addData("Detection", detector.getPosition());
            FtcDashboard.getInstance().getTelemetry().addData("Left", hardware.getOdometrySystem().getLeftDist());
            FtcDashboard.getInstance().getTelemetry().addData("Forward", hardware.getOdometrySystem().getRightDist());
            FtcDashboard.getInstance().getTelemetry().update();
            if(!isStarted()) {
                switch (detector.getPosition()) {
                    case LEFT:
                        level = BlueAuto.LEVEL.LOW;
                        break;
                    case CENTRE:
                        level = BlueAuto.LEVEL.MED;
                        break;
                    case RIGHT:
                        level = BlueAuto.LEVEL.HIGH;
                        break;
                }
            }
        });

        OpmodeStatus.bindOnStart(new InstantAction() {
            @Override
            public void update() {
                started = System.currentTimeMillis();
                detector.shutdown();
            }
        });

        ActionController.addAction(BlueGoalAutoActions.initQueue(hardware));

        SampleMecanumDrive drive = new SampleMecanumDrive(hardwareMap);
        drive.setLocalizer(new FFFusionOdometer(hardware.getOdometrySystem(), hardware.getDrivetrainSystem()));

        Trajectory traj = drive.trajectoryBuilder(new Pose2d(9.7, 63.5, 0))
                .forward(32)
                .forward(5)
                .addDisplacementMarker(() -> hardware.getIntakeSystem().getOuttakeAction(hardware).submit())
                .back(5)
                .addDisplacementMarker(() -> BlueGoalAutoActions.preloadToHighGoal(hardware))
                .back(32)
                .addDisplacementMarker(() -> BlueGoalAutoActions.score(hardware))
                .build();

        ActionQueue startQueue = new ActionQueue();
        startQueue.submitAction(BlueGoalAutoActions.score(hardware));
        startQueue.submitAction(new Action() {
            @Override
            public void initialize() {
                drive.followTrajectoryAsync(traj);
            }

            @Override
            public void update() {
                drive.update();
            }
        });
    }
}
