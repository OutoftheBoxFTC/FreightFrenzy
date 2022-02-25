package Opmodes.Auto;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import Hardware.HardwareSystems.FFSystems.Actions.MoveScoutAction;
import Hardware.HardwareSystems.FFSystems.ScoutSystem;
import Hardware.Pipelines.LineFinderCamera;
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
public class RedAuto extends BasicOpmode {
    private double startPos = 0;

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

        LineFinderCamera lineCamera = new LineFinderCamera(hardwareMap, hardware);

        hardware.getTurretSystem().setScoutAlliance(ScoutSystem.SCOUT_ALLIANCE.RED);
        hardware.getTurretSystem().setScoutFieldTarget(ScoutSystem.SCOUT_TARGET.ALLIANCE_HIGH);

        ActionQueue initQueue = new ActionQueue();
        initQueue.submitAction(new Action() {
            @Override
            public void update() {

            }

            @Override
            public boolean shouldDeactivate() {
                return lineCamera.isOpened();
            }
        });
        initQueue.submitAction(new InstantAction() {
            @Override
            public void update() {
                hardware.getIntakeSystem().moveCameraDown();
            }
        });
        /**
        initQueue.submitAction(new MoveScoutAction(hardware.getTurretSystem(), ScoutSystem.SCOUT_STATE.PRELOAD_ANGLE));
         */
        initQueue.submitAction(new DelayAction(1000));
        initQueue.submitAction(new TimedAction(1000) {
            @Override
            public void update() {
                startPos = lineCamera.getPipeline().getRealY();
            }
        });
        initQueue.submitAction(new InstantAction() {
            @Override
            public void update() {
                hardware.getIntakeSystem().moveCameraInspection();
            }
        });

        ActionController.addAction(initQueue);

        TrajectorySequence intoWarehouse = drive.trajectorySequenceBuilder(new Pose2d(0, 0, 0))
                .forward(45)
                .build();

        TrajectorySequence back = drive.trajectorySequenceBuilder(intoWarehouse.end())
                .back(45)
                .build();

        ActionQueue queue = new ActionQueue();
        /**
        queue.submitAction(new MoveScoutAction(hardware.getTurretSystem(), ScoutSystem.SCOUT_STATE.SCORE));
         */
        queue.submitAction(new InstantAction() {
            @Override
            public void update() {
                hardware.getIntakeSystem().lock();
            }
        });
        queue.submitAction(new DelayAction(1000));
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
                hardware.getTurretSystem().closeArm();
            }
        });
        queue.submitAction(new MoveScoutAction(hardware.getTurretSystem(), ScoutSystem.SCOUT_STATE.HOME_IN_INTAKE));

        for(int i = 0; i < 4; i ++){
            queue.submitAction(new InstantAction() {
                @Override
                public void update() {
                    //hardware.getIntakeSystem().intake();
                }
            });
            queue.submitAction(new Action() {
                @Override
                public void update() {
                    double distance = 35 - drive.getPoseEstimate().getX();
                    double power = Math.sqrt(2 * (DriveConstants.MAX_ACCEL/3.0) * distance) / DriveConstants.MAX_VEL;
                    drive.setDrivePower(new Pose2d(Math.max(power, 1), -0.4, 0));
                    telemetry.addData("X", drive.getPoseEstimate().getX());
                }

                @Override
                public boolean shouldDeactivate() {
                    return drive.getPoseEstimate().getX() > 35;
                }
            });
            queue.submitAction(new InstantAction() {
                @Override
                public void update() {
                    hardware.getTurretSystem().closeArm();
                    //hardware.getIntakeSystem().outtake();
                    drive.setDrivePower(new Pose2d());
                }
            });
            queue.submitAction(new DelayAction(1000));
            queue.submitAction(new InstantAction() {
                @Override
                public void update() {

                    //hardware.getTurretSystem().setScoutTarget(ScoutSystem.SCOUT_STATE.PRELOAD_ANGLE);
                }
            });
            queue.submitAction(new Action() {
                @Override
                public void update() {
                    double distance = drive.getPoseEstimate().getX();
                    double power = Math.sqrt(2 * (DriveConstants.MAX_ACCEL/3.0) * distance) / DriveConstants.MAX_VEL;
                    drive.setDrivePower(new Pose2d(-Math.max(power, 1), -0.4, 0));
                    telemetry.addData("X", drive.getPoseEstimate().getX());
                    if(drive.getPoseEstimate().getX() < 10) {
                        hardware.getIntakeSystem().moveCameraDown();
                    }
                }

                @Override
                public boolean shouldDeactivate() {
                    return drive.getPoseEstimate().getX() < 0;
                }
            });            //queue.submitAction(new MoveScoutAction(hardware.getTurretSystem(), ScoutSystem.SCOUT_STATE.SCORE));
            queue.submitAction(new InstantAction() {
                @Override
                public void update() {
                    drive.setDrivePower(new Pose2d());
                }
            });
            queue.submitAction(new DelayAction(1000));
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
                    hardware.getTurretSystem().closeArm();
                }
            });
            queue.submitAction(new MoveScoutAction(hardware.getTurretSystem(), ScoutSystem.SCOUT_STATE.HOME_IN_INTAKE));
            queue.submitAction(new TimedAction(500) {
                @Override
                public void update() {
                    double position = startPos - lineCamera.getPipeline().getRealY();
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
        }



        OpmodeStatus.bindOnStart(queue);
    }
}
