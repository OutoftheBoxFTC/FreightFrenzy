package Opmodes.Auto;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.trajectory.MarkerCallback;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import Hardware.HardwareSystems.FFSystems.Actions.BlueGoalAutoActions;
import Odometry.FFFusionOdometer;
import Opmodes.BasicOpmode;
import RoadRunner.drive.SampleMecanumDrive;
import RoadRunner.trajectorysequence.TrajectorySequence;
import RoadRunner.trajectorysequence.TrajectorySequenceBuilder;
import State.Action.Action;
import State.Action.ActionController;
import State.Action.ActionQueue;
import State.Action.InstantAction;
import Utils.OpmodeData;
import Utils.OpmodeStatus;
import Vision.ApriltagDetector;
@Autonomous
public class BlueAutoV2 extends BasicOpmode {
    public static BlueAuto.LEVEL level = BlueAuto.LEVEL.MED;

    long started = 0;

    @Override
    public void setup() {
        ApriltagDetector detector = new ApriltagDetector(hardwareMap);
        ActionController.addAction(detector);

        hardware.getDrivetrainSystem().disable();

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

        ActionController.addAction(new Action() {
            @Override
            public void update() {
                telemetry.addData("Extension", hardware.getTurretSystem().getExtensionPosition());
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
        //drive.setLocalizer(new FFFusionOdometer(hardware.getOdometrySystem(), hardware.getDrivetrainSystem()));

        TrajectorySequenceBuilder builder = drive.trajectorySequenceBuilder(new Pose2d(0, 0, 0))
                .strafeRight(3);

        for(int i = 0; i < 4; i ++){
            builder.forward(19)
                    .addDisplacementMarker(() -> BlueGoalAutoActions.intoIntake(hardware).submit())
                    .forward(4)
                    .waitSeconds(0.5)
                    .forward(12 + (i * 1))
                    .addDisplacementMarker(() -> hardware.getIntakeSystem().getOuttakeAction(hardware).submit())
                    .back(7 + (i * 1))
                    .addDisplacementMarker(() -> BlueGoalAutoActions.preloadToHighGoal(hardware).submit())
                    .back(20 + (i * 1.5))
                    .addDisplacementMarker(() -> BlueGoalAutoActions.score(hardware).submit())
                    .back(4)
                    .waitSeconds(1);
        }

        builder.forward(25);

        TrajectorySequence goIn1 = builder.build();

        ActionQueue startQueue = new ActionQueue();
        startQueue.submitAction(BlueGoalAutoActions.score(hardware));
        startQueue.submitAction(new Action() {
            @Override
            public void initialize() {
                drive.followTrajectorySequenceAsync(goIn1);
            }

            @Override
            public void update() {
                drive.update();
            }
        });

        OpmodeStatus.bindOnStart(startQueue);
    }
}
