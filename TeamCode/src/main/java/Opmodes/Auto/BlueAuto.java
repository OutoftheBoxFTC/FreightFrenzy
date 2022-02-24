package Opmodes.Auto;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.roadrunner.geometry.Pose2d;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;

import Hardware.HardwareSystems.FFSystems.Actions.FollowTrajectoryAction;
import Hardware.HardwareSystems.FFSystems.Actions.MoveScoutAction;
import Hardware.HardwareSystems.FFSystems.ScoutSystem;
import Hardware.Pipelines.LineFinderCamera;
import Hardware.Pipelines.LineFinderPipeline;
import MathSystems.Vector.Vector3;
import Opmodes.Auto.AutoActions.FollowIntakeTrajectoryAction;
import Opmodes.BasicOpmode;
import RoadRunner.drive.SampleMecanumDrive;
import RoadRunner.trajectorysequence.TrajectorySequence;
import RoadRunner.trajectorysequence.sequencesegment.TrajectorySegment;
import State.Action.Action;
import State.Action.ActionController;
import State.Action.ActionQueue;
import State.Action.InstantAction;
import State.Action.StandardActions.DelayAction;
import State.Action.StandardActions.TimedAction;
import Utils.OpmodeStatus;

public class BlueAuto extends BasicOpmode {
    private double startPos = 0;

    @Override
    public void setup() {
        SampleMecanumDrive drive = new SampleMecanumDrive(hardwareMap);

        LineFinderCamera lineCamera = new LineFinderCamera(hardwareMap, hardware);

        hardware.getTurretSystem().setScoutAlliance(ScoutSystem.SCOUT_ALLIANCE.BLUE);
        hardware.getTurretSystem().setScoutFieldTarget(ScoutSystem.SCOUT_TARGET.ALLIANCE_HIGH);

        ActionQueue initQueue = new ActionQueue();
        initQueue.submitAction(new InstantAction() {
            @Override
            public void update() {
                hardware.getIntakeSystem().moveCameraDown();
            }
        });
        initQueue.submitAction(new MoveScoutAction(hardware.getTurretSystem(), ScoutSystem.SCOUT_STATE.PRELOAD_ANGLE));
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
        queue.submitAction(new MoveScoutAction(hardware.getTurretSystem(), ScoutSystem.SCOUT_STATE.SCORE));
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

        for(int i = 0; i < 1; i ++){
            queue.submitAction(new InstantAction() {
                @Override
                public void update() {
                    hardware.getIntakeSystem().intake();
                }
            });
            queue.submitAction(new FollowIntakeTrajectoryAction(drive, intoWarehouse, hardware.getIntakeSystem()));
            queue.submitAction(new InstantAction() {
                @Override
                public void update() {
                    hardware.getTurretSystem().closeArm();
                    hardware.getIntakeSystem().outtake();
                    drive.setDrivePower(new Pose2d());
                }
            });
            queue.submitAction(new DelayAction(250));
            queue.submitAction(new InstantAction() {
                @Override
                public void update() {
                    hardware.getTurretSystem().setScoutTarget(ScoutSystem.SCOUT_STATE.PRELOAD_ANGLE);
                }
            });
            queue.submitAction(new FollowTrajectoryAction(drive, back));
            queue.submitAction(new MoveScoutAction(hardware.getTurretSystem(), ScoutSystem.SCOUT_STATE.SCORE));
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
                    hardware.getIntakeSystem().moveCameraDown();
                }
            });
            queue.submitAction(new MoveScoutAction(hardware.getTurretSystem(), ScoutSystem.SCOUT_STATE.HOME_IN_INTAKE));
            queue.submitAction(new InstantAction() {
                @Override
                public void update() {
                    double position = lineCamera.getPipeline().getRealY() - startPos;
                    Pose2d estimate = drive.getPoseEstimate();
                    drive.setPoseEstimate(new Pose2d(position, estimate.getY(), estimate.getHeading()));
                    hardware.getIntakeSystem().moveCameraInspection();
                }
            });
        }



        OpmodeStatus.bindOnStart(queue);
    }
}
