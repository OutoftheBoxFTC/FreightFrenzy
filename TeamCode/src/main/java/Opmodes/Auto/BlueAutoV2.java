package Opmodes.Auto;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.trajectory.MarkerCallback;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import Hardware.HardwareSystems.FFSystems.Actions.BlueGoalActions;
import Hardware.HardwareSystems.FFSystems.Actions.BlueGoalAutoActions;
import Hardware.HardwareSystems.FFSystems.Actions.MoveExtensionAction;
import MathSystems.Angle;
import MathSystems.Position;
import MathSystems.Vector.Vector3;
import Odometry.FFFusionOdometer;
import Opmodes.BasicOpmode;
import RoadRunner.drive.SampleMecanumDrive;
import RoadRunner.trajectorysequence.TrajectorySequence;
import RoadRunner.trajectorysequence.TrajectorySequenceBuilder;
import State.Action.Action;
import State.Action.ActionController;
import State.Action.ActionQueue;
import State.Action.InstantAction;
import State.Action.StandardActions.DelayAction;
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
                .strafeRight(4);

        for(int i = 0; i < 3; i ++){
            builder.forward(21)
                    .addDisplacementMarker(() -> BlueGoalAutoActions.intoIntake(hardware).submit())
                    .forward(4)
                    .waitSeconds(0.5)
                    .forward(12 + (i * 1.6))
                    .addDisplacementMarker(() -> hardware.getIntakeSystem().getOuttakeAction(hardware).submit())
                    .back(7 + (i * 1.8))
                    .addDisplacementMarker(() -> BlueGoalAutoActions.preloadToHighGoal(hardware).submit())
                    .back(20 + (i * 1.95))
                    .addDisplacementMarker(() -> BlueGoalAutoActions.score(hardware).submit())
                    .back(4)
                    .waitSeconds(1);
        }

        builder.forward(32);

        TrajectorySequence goIn1 = builder.build();

        ActionQueue startQueue = new ActionQueue();
        startQueue.submitAction(new Action() {
                    @Override
                    public void update() {
                        hardware.getTurretSystem().setBucketPosRaw(1);
                        if(level == BlueAuto.LEVEL.HIGH) {
                            MoveExtensionAction.P = -0.005;
                            hardware.getTurretSystem().movePitchRaw(Angle.degrees(-20));
                            hardware.getTurretSystem().moveExtensionRaw(650);
                            hardware.getTurretSystem().moveTurretRaw(Angle.degrees(-50));
                        }
                        if(level == BlueAuto.LEVEL.MED){
                            MoveExtensionAction.P = -0.002;
                            hardware.getTurretSystem().movePitchRaw(Angle.degrees(7));
                            hardware.getTurretSystem().moveExtensionRaw(650);
                            hardware.getTurretSystem().moveTurretRaw(Angle.degrees(-50));
                            hardware.getTurretSystem().setBucketPosRaw(0.9);
                        }
                        if(level == BlueAuto.LEVEL.LOW){
                            MoveExtensionAction.P = -0.001;
                            hardware.getTurretSystem().moveExtensionRaw(645);
                            hardware.getTurretSystem().moveTurretRaw(Angle.degrees(-38));
                            hardware.getTurretSystem().setBucketPosRaw(0.85);
                            ActionController.addAction(new Action() {
                                @Override
                                public void update() {
                                    if(hardware.getTurretSystem().getExtensionPosition() > 200){
                                        hardware.getTurretSystem().movePitchRaw(Angle.degrees(17));
                                    }
                                }

                                @Override
                                public boolean shouldDeactivate() {
                                    return hardware.getTurretSystem().getExtensionPosition() > 250;
                                }
                            });
                        }
                    }

                    @Override
                    public boolean shouldDeactivate() {
                        return hardware.getTurretSystem().isExtensionAtPos();
                    }
                });
        startQueue.submitAction(new Action() {
                    @Override
                    public void update() {
                    }

                    @Override
                    public boolean shouldDeactivate() {
                        return hardware.getTurretSystem().isExtensionAtPos();
                    }
                });
        startQueue.submitAction(new DelayAction(75));
        startQueue.submitAction(new InstantAction() {
                    @Override
                    public void update() {
                        MoveExtensionAction.P = -0.005;
                        hardware.getTurretSystem().openArm();
                    }
                });

        startQueue.submitAction(new DelayAction(200));

        startQueue.submitAction(new InstantAction() {
                    @Override
                    public void update() {
                        hardware.getTurretSystem().closeArm();
                        hardware.getTurretSystem().setBucketPosRaw(0.4);
                    }
                });
        startQueue.submitAction(new DelayAction(50));
        startQueue.submitAction(BlueGoalActions.getBlueAllianceReturnAuto(hardware));
        startQueue.submitAction(new Action() {
                    @Override
                    public void update() {
                        hardware.getIntakeSystem().setPower(-1);
                        hardware.getTurretSystem().moveTurretRaw(Angle.degrees(0));
                        hardware.getTurretSystem().openArm();
                    }

                    @Override
                    public boolean shouldDeactivate() {
                        return true;
                    }
                });
        startQueue.submitAction(new DelayAction(500));
        startQueue.submitAction(new Action() {
            @Override
            public void initialize() {
                drive.followTrajectorySequenceAsync(goIn1);
            }

            @Override
            public void update() {
                drive.update();
            }

            @Override
            public boolean shouldDeactivate() {
                return System.currentTimeMillis() - started > 28000;
            }
        });
        startQueue.submitAction(new InstantAction() {
            @Override
            public void update() {
                hardware.getDrivetrainSystem().setPower(Vector3.ZERO());
            }
        });
        startQueue.submitAction(BlueGoalAutoActions.intoIntake(hardware));

        OpmodeStatus.bindOnStart(startQueue);
    }
}
