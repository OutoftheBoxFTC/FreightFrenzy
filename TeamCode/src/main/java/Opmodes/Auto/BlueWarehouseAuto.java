package Opmodes.Auto;

import android.graphics.Path;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.trajectory.MarkerCallback;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

import Drive.BasicDrive.PurePursuit;
import Hardware.HardwareSystems.FFSystems.Actions.FollowTrajectoryAction;
import Hardware.HardwareSystems.FFSystems.Actions.MoveScoutAction;
import Hardware.HardwareSystems.FFSystems.Actions.ScoutTargets;
import Hardware.HardwareSystems.FFSystems.IntakeSystem;
import Hardware.HardwareSystems.FFSystems.ScoutSystem;
import MathSystems.Angle;
import MathSystems.MathUtils;
import MathSystems.Position;
import Odometry.SinglePodOdometer;
import Opmodes.Auto.AutoActions.FollowIntakeTrajectoryAction;
import Opmodes.BasicOpmode;
import RoadRunner.drive.DriveConstants;
import RoadRunner.drive.SampleMecanumDrive;
import RoadRunner.drive.SampleTankDrive;
import RoadRunner.trajectorysequence.TrajectorySequence;
import State.Action.Action;
import State.Action.ActionController;
import State.Action.ActionQueue;
import State.Action.InstantAction;
import State.Action.StandardActions.DelayAction;
import State.Action.StandardActions.TimedAction;
import Utils.OpmodeStatus;
import Utils.PathUtils.PathBuilder;

@Autonomous
@Config
public class BlueWarehouseAuto extends BasicOpmode {
    private double startPos = 0;
    public static PRELOAD_POSITION preload = PRELOAD_POSITION.HIGH;
    private Position position = Position.ZERO();

    private long startTimer = 0;

    @Override
    public void setup() {
        SampleTankDrive drive = new SampleTankDrive(hardwareMap);

        OpmodeStatus.bindOnStart(drive::update);

        OpmodeStatus.bindOnStart(new Action() {
            @Override
            public void update() {
                position.set(new Position(drive.getPoseEstimate().component1(), drive.getPoseEstimate().component2(), Angle.radians(drive.getPoseEstimate().component3())));
            }
        });

        OpmodeStatus.bindOnStart(new Action() {
            @Override
            public void update() {
                FtcDashboard.getInstance().getTelemetry().addData("Motor Power", hardware.getTurretSystem().getExtensionMotor().getPower());
                FtcDashboard.getInstance().getTelemetry().update();
            }
        });

        OpmodeStatus.bindOnStart(() -> telemetry.addData("Pose", drive.getPoseEstimate()));

        OpmodeStatus.bindOnStart(new InstantAction() {
            @Override
            public void update() {
                startTimer = System.currentTimeMillis();
            }
        });

        ActionQueue initQueue = new ActionQueue();
        initQueue.submitAction(new Action() {
            @Override
            public void update() {
                hardware.getTurretSystem().setAuto(true);
            }

            @Override
            public boolean shouldDeactivate() {
                return hardware.getIntakeSystem().getCamera().isOpened();
            }
        });
        initQueue.submitAction(new InstantAction() {
            @Override
            public void update() {
                hardware.getIntakeSystem().moveCameraLine();
                hardware.getTurretSystem().setScoutAlliance(ScoutSystem.SCOUT_ALLIANCE.BLUE);
                hardware.getTurretSystem().setScoutFieldTarget(ScoutSystem.SCOUT_TARGET.ALLIANCE_LOW);
                hardware.getIntakeSystem().setEnabled(false);
            }
        });
        initQueue.submitAction(new MoveScoutAction(hardware.getTurretSystem(), ScoutSystem.SCOUT_STATE.PRELOAD_ANGLE));
        initQueue.submitAction(new DelayAction(1000));
        initQueue.submitAction(new InstantAction() {
            @Override
            public void update() {
                //hardware.getTurretSystem().setBucketScore();
                //hardware.getTurretSystem().bypassSetState(ScoutSystem.SCOUT_STATE.HOMING);
                //hardware.getTurretSystem().moveTurretRaw(ScoutTargets.getTarget(ScoutSystem.SCOUT_ALLIANCE.BLUE, ScoutSystem.SCOUT_TARGET.ALLIANCE_MID).turretAngle);
                //hardware.getTurretSystem().movePitchRaw(ScoutTargets.getTarget(ScoutSystem.SCOUT_ALLIANCE.BLUE, ScoutSystem.SCOUT_TARGET.ALLIANCE_MID).pitchAngle);
                //hardware.getTurretSystem().setExtensionPreload(0);
            }
        });
        initQueue.submitAction(new TimedAction(1000) {
            @Override
            public void update() {
                startPos = hardware.getIntakeSystem().getCamera().getLinePipeline().getRealY();
            }
        });
        initQueue.submitAction(new InstantAction() {
            @Override
            public void update() {
                hardware.getIntakeSystem().moveCameraBlueTSE();
                hardware.getIntakeSystem().transferFlipIn();
                hardware.getIntakeSystem().setTransferAuto();
            }
        });

        ActionController.addAction(initQueue);

        ActionController.addAction(new Action() {
            @Override
            public void update() {
                switch ((int) hardware.getIntakeSystem().getCamera().getTSEPipeline().getPosition()){
                    case 1:
                        preload = PRELOAD_POSITION.LOW;
                        break;
                    case 2:
                        preload = PRELOAD_POSITION.MEDIUM;
                        break;
                    default:
                        preload = PRELOAD_POSITION.HIGH;
                        break;
                }
                //preload = PRELOAD_POSITION.LOW;
                telemetry.addData("Preload", preload);
            }

            @Override
            public boolean shouldDeactivate() {
                return isStarted();
            }
        });

        TrajectorySequence intoWarehouse = drive.trajectorySequenceBuilder(new Pose2d(0, 2, 0))
                .forward(18)
                .build();

        TrajectorySequence back = drive.trajectorySequenceBuilder(intoWarehouse.end())
                .addDisplacementMarker(0.4, () -> {
                    hardware.getIntakeSystem().startTransfer();
                    hardware.getIntakeSystem().moveCameraLine();
                })
                .back(20)
                .build();

        ActionQueue queue = new ActionQueue();
        /**
        queue.submitAction(new MoveScoutAction(hardware.getTurretSystem(), ScoutSystem.SCOUT_STATE.SCORE));
         */
        queue.submitAction(new InstantAction() {
            @Override
            public void update() {
                hardware.getTurretSystem().setAuto(false);
                hardware.getIntakeSystem().setEnabled(true);
                hardware.getIntakeSystem().getCapServo().setPosition(0.7);
                ScoutTargets.SCOUTTarget target = ScoutTargets.getTarget(ScoutSystem.SCOUT_ALLIANCE.BLUE, ScoutSystem.SCOUT_TARGET.ALLIANCE_HIGH);
                switch (preload){
                    case HIGH:
                        target = ScoutTargets.getTarget(ScoutSystem.SCOUT_ALLIANCE.BLUE, ScoutSystem.SCOUT_TARGET.ALLIANCE_HIGH);
                        hardware.getTurretSystem().setScoutFieldTarget(ScoutSystem.SCOUT_TARGET.ALLIANCE_HIGH);
                        break;
                    case MEDIUM:
                        target = ScoutTargets.getTarget(ScoutSystem.SCOUT_ALLIANCE.BLUE, ScoutSystem.SCOUT_TARGET.ALLIANCE_MID);
                        hardware.getTurretSystem().setScoutFieldTarget(ScoutSystem.SCOUT_TARGET.ALLIANCE_MID);
                        break;
                    case LOW:
                        target = ScoutTargets.getTarget(ScoutSystem.SCOUT_ALLIANCE.BLUE, ScoutSystem.SCOUT_TARGET.ALLIANCE_LOW);
                        hardware.getTurretSystem().setScoutFieldTarget(ScoutSystem.SCOUT_TARGET.ALLIANCE_LOW);
                        break;
                }
                //hardware.getTurretSystem().moveTurretRaw(target.turretAngle);
                //hardware.getTurretSystem().movePitchRaw(target.pitchAngle);
                //hardware.getTurretSystem().moveExtensionRaw(target.extension, DistanceUnit.INCH);
                //hardware.getTurretSystem().bypassSetState(ScoutSystem.SCOUT_STATE.SCORE);
                hardware.getTurretSystem().setExtensionPreload(8);
            }
        });
        queue.submitAction(new DelayAction(25));
        queue.submitAction(new InstantAction() {
            @Override
            public void update() {
                hardware.getIntakeSystem().getCapServo().setPosition(0.7);
                hardware.getTurretSystem().setScoutTarget(ScoutSystem.SCOUT_STATE.SCORE);
            }
        });
        queue.submitAction(new Action() {
            @Override
            public void update() {

            }

            @Override
            public boolean shouldDeactivate() {
                return hardware.getTurretSystem().getCurrentState() == ScoutSystem.SCOUT_STATE.SCORE && hardware.getTurretSystem().isScoutIdle();
            }
        });
        queue.submitAction(new InstantAction() {
            @Override
            public void update() {
                //hardware.getIntakeSystem().lock();
            }
        });
        queue.submitAction(new DelayAction(300));
        queue.submitAction(new InstantAction() {
            @Override
            public void update() {
                hardware.getTurretSystem().kickArm();

                //hardware.getTurretSystem().setScoutFieldTarget(ScoutSystem.SCOUT_TARGET.ALLIANCE_HIGH);
            }
        });
        queue.submitAction(new DelayAction(300));
        queue.submitAction(new InstantAction() {
            @Override
            public void update() {
                hardware.getIntakeSystem().transferFlipOut();
                hardware.getIntakeSystem().disableAuto();
                hardware.getTurretSystem().setScoutTarget(ScoutSystem.SCOUT_STATE.HOME_IN_INTAKE);
                hardware.getTurretSystem().setScoutFieldTarget(ScoutSystem.SCOUT_TARGET.ALLIANCE_HIGH_AUTO);
                hardware.getTurretSystem().setExtensionPreload(6);
            }
        });
        queue.submitAction(new Action() {
            @Override
            public void update() {
                hardware.getTurretSystem().setAuto(false);
            }

            @Override
            public boolean shouldDeactivate() {
                return hardware.getTurretSystem().getExtensionRealDistance(DistanceUnit.INCH) < 20;
            }
        });
        queue.submitAction(new DelayAction(750));


        for(int i = 0; i < 4; i ++){
            int finalI = i;

            queue.submitAction(new Action() {
                @Override
                public void update() {
                    double distance = (18 + (finalI * 1)) - drive.getPoseEstimate().getX();
                    double power = Math.sqrt(2 * (DriveConstants.MAX_ACCEL/3.0) * distance) / DriveConstants.MAX_VEL;
                    double headingPower = 0;
                    if(drive.getPoseEstimate().getHeading() > Math.toRadians(5)){
                        headingPower = -0.4 * Math.signum(drive.getPoseEstimate().getHeading());
                    }
                    drive.setDrivePower(new Pose2d(Math.max(0.6, Math.min(power, 0.5)), 0, 0.3));
                    if(finalI > 1 && distance < 20) {
                        drive.setDrivePower(new Pose2d(Math.max(0.6, Math.min(power, 0.5)), 0, -0.35));
                    }
                    if(distance < 20){
                        hardware.getIntakeSystem().intake();
                    }
                    telemetry.addData("X", drive.getPoseEstimate().getX());
                    hardware.getIntakeSystem().intake();
                }

                @Override
                public boolean shouldDeactivate() {
                    return drive.getPoseEstimate().getX() >= 16 + (finalI * 1) || hardware.getIntakeSystem().itemInTransfer();
                }
            });


            //queue.submitAction(new FollowIntakeTrajectoryAction(drive, intoWarehouse, hardware.getIntakeSystem()));
            queue.submitAction(new InstantAction() {
                @Override
                public void update() {
                    if(System.currentTimeMillis() - startTimer > 27500){
                        ActionController.getInstance().terminateAction(queue);
                    }
                    hardware.getIntakeSystem().startTransfer();
                }
            });

            queue.submitAction(new InstantAction() {
                @Override
                public void update() {
                    drive.setDrivePower(new Pose2d());
                }
            });

            //queue.submitAction(new DelayAction(200));
            /**
            queue.submitAction(new InstantAction() {
                @Override
                public void update() {
                    hardware.getTurretSystem().setScoutTarget(ScoutSystem.SCOUT_STATE.PRELOAD_ANGLE);
                }
            });
            */

            queue.submitAction(new Action() {
                long timer = Long.MAX_VALUE;
                @Override
                public void update() {
                    double distance = drive.getPoseEstimate().getX();
                    double power = Math.sqrt(2 * (DriveConstants.MAX_ACCEL/3.0) * distance) / DriveConstants.MAX_VEL;
                    double headingPower = 0;
                    if(drive.getPoseEstimate().getHeading() > Math.toRadians(5)){
                        headingPower = -0.4 * Math.signum(drive.getPoseEstimate().getHeading());
                    }
                    double headingError = MathUtils.getRadRotDist(drive.getPoseEstimate().getHeading(), 0);
                    drive.setDrivePower(new Pose2d(-Math.max(Math.min(power, 1), 0.8), 0, -0.3));

                    telemetry.addData("X", drive.getPoseEstimate().getX());
                    if(drive.getPoseEstimate().getX() < 23 || hardware.getIntakeSystem().itemInTransfer()) {
                        hardware.getIntakeSystem().moveCameraLine();
                        if(timer == 0){
                            timer = System.currentTimeMillis() + 500;
                        }
                    }
                }

                @Override
                public boolean shouldDeactivate() {
                    return drive.getPoseEstimate().getX() < 3;
                }
            });

            //queue.submitAction(new FollowTrajectoryAction(drive, back));
            queue.submitAction(new InstantAction() {
                @Override
                public void update() {
                    drive.setDrivePower(new Pose2d());
                }
            });
            queue.submitAction(new Action() {
                @Override
                public void update() {

                }

                @Override
                public boolean shouldDeactivate() {
                    return hardware.getIntakeSystem().getCurrentState() == IntakeSystem.INTAKE_STATE.IDLE || hardware.getTurretSystem().getScoutTarget() == ScoutSystem.SCOUT_STATE.SCORE;
                }
            });
            queue.submitAction(new InstantAction() {
                @Override
                public void update() {
                    hardware.getIntakeSystem().setPower(0.01);
                }
            });
            queue.submitAction(new MoveScoutAction(hardware.getTurretSystem(), ScoutSystem.SCOUT_STATE.SCORE));
            queue.submitAction(new InstantAction() {
                @Override
                public void update() {
                    hardware.getTurretSystem().kickArm();
                }
            });
            queue.submitAction(new DelayAction(100));
            queue.submitAction(new InstantAction() {
                @Override
                public void update() {
                    hardware.getTurretSystem().setScoutTarget(ScoutSystem.SCOUT_STATE.HOME_IN_INTAKE);
                }
            });
            queue.submitAction(new Action() {
                @Override
                public void update() {
                    double position = startPos - hardware.getIntakeSystem().getCamera().getLinePipeline().getRealY();
                    //drive.setPoseEstimate(new Pose2d(position, 0, 0));
                }

                @Override
                public boolean shouldDeactivate() {
                    return hardware.getTurretSystem().getExtensionRealDistance(DistanceUnit.INCH) < 40;
                }
            });
            queue.submitAction(new InstantAction() {
                @Override
                public void update() {
                    hardware.getIntakeSystem().moveCameraInspection();
                    hardware.getIntakeSystem().outtake();
                }
            });
            //queue.submitAction(new DelayAction(200));
        }

        queue.submitAction(new InstantAction() {
            @Override
            public void update() {
                hardware.getIntakeSystem().setPower(0);
            }
        });

        queue.submitAction(new Action() {
            @Override
            public void update() {
                double distance = (25) - drive.getPoseEstimate().getX();
                double power = Math.sqrt(2 * (DriveConstants.MAX_ACCEL/3.0) * distance) / DriveConstants.MAX_VEL;
                drive.setDrivePower(new Pose2d(Math.max(1, Math.min(power, 0.8)), 0, 0.3));
                telemetry.addData("X", drive.getPoseEstimate().getX());
                hardware.getIntakeSystem().intake();
            }

            @Override
            public boolean shouldDeactivate() {
                return drive.getPoseEstimate().getX() >= 23 || hardware.getIntakeSystem().itemInTransfer();
            }
        });

        queue.submitAction(new InstantAction() {
            @Override
            public void update() {
                drive.setDrivePower(new Pose2d());
            }
        });

        OpmodeStatus.bindOnStart(() -> {
            if(Math.abs(MathUtils.getRotDist(hardware.getDrivetrainSystem().getImuAngle(), Angle.degrees(0)).degrees()) > 40){
                ActionController.getInstance().terminateAction(queue);
                ActionController.addAction(() -> {
                    drive.setDrivePower(new Pose2d());
                    hardware.getTurretSystem().setScoutTarget(ScoutSystem.SCOUT_STATE.HOME_IN_INTAKE);
                });
            }
        });

        OpmodeStatus.bindOnStart(queue);
    }

    public enum PRELOAD_POSITION{
        HIGH,
        MEDIUM,
        LOW
    }
}
