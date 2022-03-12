package Opmodes.Auto;

import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

import Hardware.HardwareSystems.FFSystems.Actions.MoveScoutAction;
import Hardware.HardwareSystems.FFSystems.Actions.ScoutTargets;
import Hardware.HardwareSystems.FFSystems.IntakeSystem;
import Hardware.HardwareSystems.FFSystems.ScoutSystem;
import MathSystems.Angle;
import MathSystems.MathUtils;
import Opmodes.BasicOpmode;
import RoadRunner.drive.DriveConstants;
import RoadRunner.drive.SampleMecanumDrive;
import RoadRunner.trajectorysequence.TrajectorySequence;
import State.Action.Action;
import State.Action.ActionController;
import State.Action.ActionQueue;
import State.Action.InstantAction;
import State.Action.StandardActions.DelayAction;
import State.Action.StandardActions.TimedAction;
import Utils.OpmodeStatus;

@Autonomous
@Config
public class RedAuto extends BasicOpmode {
    private double startPos = 0;
    public static PRELOAD_POSITION preload = PRELOAD_POSITION.HIGH;

    private long startTimer = 0;

    @Override
    public void setup() {
        SampleMecanumDrive drive = new SampleMecanumDrive(hardwareMap);

        OpmodeStatus.bindOnStart(new Action() {
            @Override
            public void update() {
                telemetry.addData("Pose", drive.getPoseEstimate().toString());
            }
        });

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
                hardware.getTurretSystem().setScoutAlliance(ScoutSystem.SCOUT_ALLIANCE.RED);
                hardware.getTurretSystem().setScoutFieldTarget(ScoutSystem.SCOUT_TARGET.ALLIANCE_LOW);
                hardware.getIntakeSystem().transferFlipOut();
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
                hardware.getTurretSystem().setExtensionPreload(0);
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
                hardware.getIntakeSystem().moveCameraRedTSE();
                hardware.getIntakeSystem().transferFlipIn();
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
                ScoutTargets.SCOUTTarget target = ScoutTargets.getTarget(ScoutSystem.SCOUT_ALLIANCE.RED, ScoutSystem.SCOUT_TARGET.ALLIANCE_HIGH);
                switch (preload){
                    case HIGH:
                        target = ScoutTargets.getTarget(ScoutSystem.SCOUT_ALLIANCE.RED, ScoutSystem.SCOUT_TARGET.ALLIANCE_HIGH);
                        hardware.getTurretSystem().setScoutFieldTarget(ScoutSystem.SCOUT_TARGET.ALLIANCE_HIGH);
                        break;
                    case MEDIUM:
                        target = ScoutTargets.getTarget(ScoutSystem.SCOUT_ALLIANCE.RED, ScoutSystem.SCOUT_TARGET.ALLIANCE_MID);
                        hardware.getTurretSystem().setScoutFieldTarget(ScoutSystem.SCOUT_TARGET.ALLIANCE_MID);
                        break;
                    case LOW:
                        target = ScoutTargets.getTarget(ScoutSystem.SCOUT_ALLIANCE.RED, ScoutSystem.SCOUT_TARGET.ALLIANCE_LOW);
                        hardware.getTurretSystem().setScoutFieldTarget(ScoutSystem.SCOUT_TARGET.ALLIANCE_LOW);
                        break;
                }
                //hardware.getTurretSystem().moveTurretRaw(target.turretAngle);
                //hardware.getTurretSystem().movePitchRaw(target.pitchAngle);
                //hardware.getTurretSystem().moveExtensionRaw(target.extension, DistanceUnit.INCH);
                //hardware.getTurretSystem().bypassSetState(ScoutSystem.SCOUT_STATE.SCORE);
                hardware.getTurretSystem().setExtensionPreload(20);
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
        queue.submitAction(new DelayAction(600));
        queue.submitAction(new InstantAction() {
            @Override
            public void update() {
                hardware.getTurretSystem().openArm();
            }
        });
        queue.submitAction(new DelayAction(300));
        queue.submitAction(new InstantAction() {
            @Override
            public void update() {
                hardware.getIntakeSystem().transferFlipOut();
                hardware.getTurretSystem().setScoutTarget(ScoutSystem.SCOUT_STATE.HOME_IN_INTAKE);
                hardware.getTurretSystem().setScoutFieldTarget(ScoutSystem.SCOUT_TARGET.ALLIANCE_HIGH);

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


        for(int i = 0; i < 5; i ++){
            int finalI = i;

            queue.submitAction(new Action() {
                @Override
                public void update() {
                    double distance = (25 + (finalI * 1)) - hardware.getDrivetrainSystem().getOdometryPosition();
                    double power = Math.sqrt(2 * (DriveConstants.MAX_ACCEL/3.0) * distance) / DriveConstants.MAX_VEL;
                    double headingPower = 0;
                    if(drive.getPoseEstimate().getHeading() > Math.toRadians(5)){
                        headingPower = -0.4 * Math.signum(drive.getPoseEstimate().getHeading());
                    }
                    drive.setDrivePower(new Pose2d(Math.max(1, Math.min(power, 0.8)), -0.3, headingPower));
                    if(finalI > 2 && distance < 5) {
                        drive.setDrivePower(new Pose2d(Math.max(1, Math.min(power, 0.8)), 0.2, headingPower));
                    }
                    if(distance < 20){
                        hardware.getIntakeSystem().intake();
                    }
                    telemetry.addData("X", hardware.getDrivetrainSystem().getOdometryPosition());
                    hardware.getIntakeSystem().intake();
                }

                @Override
                public boolean shouldDeactivate() {
                    return hardware.getDrivetrainSystem().getOdometryPosition() >= 24 + (finalI * 1) || hardware.getIntakeSystem().itemInTransfer();
                }
            });

            //queue.submitAction(new FollowIntakeTrajectoryAction(drive, intoWarehouse, hardware.getIntakeSystem()));
            queue.submitAction(new InstantAction() {
                @Override
                public void update() {
                    if(System.currentTimeMillis() - startTimer > 27500){
                        ActionController.getInstance().terminateAction(queue);
                    }
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
                    double distance = hardware.getDrivetrainSystem().getOdometryPosition();
                    double power = Math.sqrt(2 * (DriveConstants.MAX_ACCEL/3.0) * distance) / DriveConstants.MAX_VEL;
                    double headingPower = 0;
                    if(drive.getPoseEstimate().getHeading() > Math.toRadians(5)){
                        headingPower = -0.4 * Math.signum(drive.getPoseEstimate().getHeading());
                    }
                    drive.setDrivePower(new Pose2d(-Math.max(Math.min(power, 1), 0.8), -0.3, headingPower));
                    telemetry.addData("X", hardware.getDrivetrainSystem().getOdometryPosition());
                    if(hardware.getDrivetrainSystem().getOdometryPosition() < 18 || hardware.getIntakeSystem().itemInTransfer()) {
                        hardware.getIntakeSystem().moveCameraLine();
                        if(timer == 0){
                            timer = System.currentTimeMillis() + 500;
                        }
                        hardware.getIntakeSystem().startTransfer();
                    }
                }

                @Override
                public boolean shouldDeactivate() {
                    return hardware.getDrivetrainSystem().getOdometryPosition() < 6;
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
                    return hardware.getIntakeSystem().getCurrentState() == IntakeSystem.INTAKE_STATE.IDLE;
                }
            });
            queue.submitAction(new MoveScoutAction(hardware.getTurretSystem(), ScoutSystem.SCOUT_STATE.SCORE));
            queue.submitAction(new DelayAction(50));
            queue.submitAction(new InstantAction() {
                @Override
                public void update() {
                    hardware.getTurretSystem().openArm();
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
                    hardware.getDrivetrainSystem().setOdometryPosition(position);
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
            queue.submitAction(new DelayAction(200));
        }

        queue.submitAction(new Action() {
            @Override
            public void update() {
                double distance = (25) - hardware.getDrivetrainSystem().getOdometryPosition();
                double power = Math.sqrt(2 * (DriveConstants.MAX_ACCEL/3.0) * distance) / DriveConstants.MAX_VEL;
                drive.setDrivePower(new Pose2d(Math.max(1, Math.min(power, 0.8)), -0.3, 0));
                telemetry.addData("X", hardware.getDrivetrainSystem().getOdometryPosition());
                hardware.getIntakeSystem().intake();
            }

            @Override
            public boolean shouldDeactivate() {
                return hardware.getDrivetrainSystem().getOdometryPosition() >= 23 || hardware.getIntakeSystem().itemInTransfer();
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
