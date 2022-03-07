package Opmodes.Auto;

import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import Hardware.HardwareSystems.FFSystems.Actions.MoveScoutAction;
import Hardware.HardwareSystems.FFSystems.Actions.ScoutTargets;
import Hardware.HardwareSystems.FFSystems.ScoutSystem;
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

        OpmodeStatus.bindOnStart(drive::updatePoseEstimate);
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
            }
        });
        initQueue.submitAction(new MoveScoutAction(hardware.getTurretSystem(), ScoutSystem.SCOUT_STATE.PRELOAD_ANGLE));
        initQueue.submitAction(new DelayAction(1000));
        initQueue.submitAction(new InstantAction() {
            @Override
            public void update() {
                hardware.getTurretSystem().setBucketScore();
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

        TrajectorySequence intoWarehouse = drive.trajectorySequenceBuilder(new Pose2d(0, 0, 0))
                .forward(20)
                .build();

        TrajectorySequence back = drive.trajectorySequenceBuilder(intoWarehouse.end())
                .addDisplacementMarker(0.8, () -> {
                    hardware.getTurretSystem().setScoutTarget(ScoutSystem.SCOUT_STATE.SCORE);
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
                hardware.getIntakeSystem().lock();
            }
        });
        queue.submitAction(new DelayAction(600));
        queue.submitAction(new InstantAction() {
            @Override
            public void update() {
                hardware.getTurretSystem().openArm();
                hardware.getTurretSystem().setScoutFieldTarget(ScoutSystem.SCOUT_TARGET.ALLIANCE_HIGH);
            }
        });
        queue.submitAction(new DelayAction(300));
        queue.submitAction(new MoveScoutAction(hardware.getTurretSystem(), ScoutSystem.SCOUT_STATE.HOME_IN_INTAKE));
        queue.submitAction(new DelayAction(750));


        for(int i = 0; i < 5; i ++){
            queue.submitAction(new InstantAction() {
                @Override
                public void update() {
                    hardware.getIntakeSystem().intake();
                }
            });
            int finalI = i;

            queue.submitAction(new Action() {
                @Override
                public void update() {
                    double distance = (14 + (finalI * 1)) - drive.getPoseEstimate().getX();
                    double power = Math.sqrt(2 * (DriveConstants.MAX_ACCEL/3.0) * distance) / DriveConstants.MAX_VEL;
                    drive.setDrivePower(new Pose2d(Math.max(0.75, Math.min(power, 1)), -0.2, 0));
                    telemetry.addData("X", drive.getPoseEstimate().getX());
                    hardware.getIntakeSystem().intake();
                }

                @Override
                public boolean shouldDeactivate() {
                    return drive.getPoseEstimate().getX() >= 16 + (finalI * 1) || hardware.getIntakeSystem().itemInIntake();
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
            queue.submitAction(new DelayAction(500));
            /**
            queue.submitAction(new InstantAction() {
                @Override
                public void update() {
                    hardware.getTurretSystem().setScoutTarget(ScoutSystem.SCOUT_STATE.PRELOAD_ANGLE);
                }
            });
            */
            queue.submitAction(new Action() {
                @Override
                public void update() {
                    double distance = drive.getPoseEstimate().getX();
                    double power = Math.sqrt(2 * (DriveConstants.MAX_ACCEL/3.0) * distance) / DriveConstants.MAX_VEL;
                    double headingPower = 0;
                    if(drive.getPoseEstimate().getHeading() > Math.toRadians(5)){
                        headingPower = -0.4 * Math.signum(drive.getPoseEstimate().getHeading());
                    }
                    drive.setDrivePower(new Pose2d(-Math.max(Math.min(power, 1), 0.8), -0.2));
                    telemetry.addData("X", drive.getPoseEstimate().getX());
                    if(drive.getPoseEstimate().getX() < 10) {
                        hardware.getIntakeSystem().moveCameraLine();
                    }
                    if(hardware.getIntakeSystem().itemInIntake()){
                        hardware.getTurretSystem().setScoutTarget(ScoutSystem.SCOUT_STATE.SCORE);
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
            queue.submitAction(new MoveScoutAction(hardware.getTurretSystem(), ScoutSystem.SCOUT_STATE.SCORE));
            queue.submitAction(new DelayAction(500));
            queue.submitAction(new InstantAction() {
                @Override
                public void update() {
                    hardware.getTurretSystem().openArm();
                }
            });
            queue.submitAction(new DelayAction(200));
            queue.submitAction(new InstantAction() {
                @Override
                public void update() {
                    hardware.getTurretSystem().setScoutTarget(ScoutSystem.SCOUT_STATE.HOME_IN_INTAKE);
                }
            });
            queue.submitAction(new Action() {
                @Override
                public void update() {

                }

                @Override
                public boolean shouldDeactivate() {
                    return hardware.getTurretSystem().getCurrentState() == ScoutSystem.SCOUT_STATE.HOME_IN_INTAKE;
                }
            });
            queue.submitAction(new TimedAction(250) {
                @Override
                public void update() {
                    double position = startPos - hardware.getIntakeSystem().getCamera().getLinePipeline().getRealY();
                    Pose2d estimate = drive.getPoseEstimate();
                    drive.setPoseEstimate(new Pose2d(position, estimate.getY(), estimate.getHeading()));
                }
            });
            queue.submitAction(new InstantAction() {
                @Override
                public void update() {
                    hardware.getIntakeSystem().moveCameraInspection();
                }
            });
            queue.submitAction(new MoveScoutAction(hardware.getTurretSystem(), ScoutSystem.SCOUT_STATE.HOME_IN_INTAKE));
        }



        OpmodeStatus.bindOnStart(queue);
    }

    public enum PRELOAD_POSITION{
        HIGH,
        MEDIUM,
        LOW
    }
}
