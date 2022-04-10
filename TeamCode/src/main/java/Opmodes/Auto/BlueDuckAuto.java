package Opmodes.Auto;

import com.acmerobotics.dashboard.FtcDashboard;
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
import MathSystems.Position;
import Opmodes.BasicOpmode;
import RoadRunner.drive.DriveConstants;
import RoadRunner.drive.SampleTankDrive;
import RoadRunner.trajectorysequence.TrajectorySequence;
import State.Action.Action;
import State.Action.ActionController;
import State.Action.ActionQueue;
import State.Action.InstantAction;
import State.Action.StandardActions.DelayAction;
import State.Action.StandardActions.TimedAction;
import Utils.OpmodeStatus;
import Utils.ProgramClock;

@Autonomous
@Config
public class BlueDuckAuto extends BasicOpmode {
    public static double PAN = 0.5;
    public static double TILT = 0.8;

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
                hardware.getTurretSystem().setScoutAlliance(ScoutSystem.SCOUT_ALLIANCE.RED);
                hardware.getTurretSystem().setScoutFieldTarget(ScoutSystem.SCOUT_TARGET.ALLIANCE_LOW);
                hardware.getIntakeSystem().transferFlipOut();
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
                //preload = PRELOAD_POSITION.HIGH;
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
                hardware.getIntakeSystem().getCapServo().setPosition(0.8);
                ScoutTargets.SCOUTTarget target = ScoutTargets.getTarget(ScoutSystem.SCOUT_ALLIANCE.BLUE, ScoutSystem.SCOUT_TARGET.ALLIANCE_HIGH);
                switch (preload){
                    case HIGH:
                        target = ScoutTargets.getTarget(ScoutSystem.SCOUT_ALLIANCE.BLUE, ScoutSystem.SCOUT_TARGET.ALLIANCE_HIGH);
                        hardware.getTurretSystem().setScoutFieldTarget(ScoutSystem.SCOUT_TARGET.ALLIANCE_HIGH_AUTO);
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
                hardware.getIntakeSystem().getCapServo().setPosition(0.9);
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
                hardware.getTurretSystem().setScoutTarget(ScoutSystem.SCOUT_STATE.HOME_IN_INTAKE);
                hardware.getTurretSystem().setScoutFieldTarget(ScoutSystem.SCOUT_TARGET.ALLIANCE_HIGH);
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
                return hardware.getTurretSystem().getExtensionRealDistance(DistanceUnit.INCH) < 5;
            }
        });
        queue.submitAction(new DelayAction(750));
        queue.submitAction(new InstantAction() {
            @Override
            public void update() {
                hardware.getTurretSystem().setScoutAlliance(ScoutSystem.SCOUT_ALLIANCE.BLUE);
            }
        });
        queue.submitAction(new Action() {
            @Override
            public void update() {
                double angleError = MathUtils.getRadRotDist(drive.getPoseEstimate().getHeading(), Math.toRadians(35));
                drive.setDrivePower(new Pose2d(0, 0, Math.signum(angleError)));
            }

            @Override
            public boolean shouldDeactivate() {
                double angleError = MathUtils.getRadRotDist(drive.getPoseEstimate().getHeading(), Math.toRadians(35));
                return Math.abs(Math.toDegrees(angleError)) < 5;
            }
        });
        queue.submitAction(new TimedAction(250) {
            @Override
            public void update() {
                drive.setDrivePower(new Pose2d());
            }
        });
        queue.submitAction(new Action() {
            @Override
            public void update() {
                drive.setDrivePower(new Pose2d(1, 0, 0));
            }

            @Override
            public boolean shouldDeactivate() {
                return drive.getPoseEstimate().getY() > 8;
            }
        });
        queue.submitAction(new TimedAction(250) {
            @Override
            public void update() {
                drive.setDrivePower(new Pose2d());
            }
        });
        queue.submitAction(new Action() {
            @Override
            public void update() {
                double angleError = MathUtils.getRadRotDist(drive.getPoseEstimate().getHeading(), Math.toRadians(-35));
                drive.setDrivePower(new Pose2d(0, 0, Math.signum(angleError)));
            }

            @Override
            public boolean shouldDeactivate() {
                double angleError = MathUtils.getRadRotDist(drive.getPoseEstimate().getHeading(), Math.toRadians(-35));
                return Math.abs(Math.toDegrees(angleError)) < 5;
            }
        });
        queue.submitAction(new TimedAction(250) {
            @Override
            public void update() {
                drive.setDrivePower(new Pose2d());
            }
        });
        queue.submitAction(new Action() {
            @Override
            public void update() {
                drive.setDrivePower(new Pose2d(0.4, 0, 0));
            }

            @Override
            public boolean shouldDeactivate() {
                return drive.getPoseEstimate().getY() < 9;
            }
        });
        queue.submitAction(new TimedAction(250) {
            @Override
            public void update() {
                drive.setDrivePower(new Pose2d());
            }
        });
        queue.submitAction(new InstantAction() {
            @Override
            public void update() {
                drive.setDrivePower(new Pose2d(0.25, 0, 0));
            }
        });
        queue.submitAction(new InstantAction() {
            @Override
            public void update() {
                hardware.getIntakeSystem().spinDuckBlueAuto();
            }
        });
        queue.submitAction(new DelayAction(5000));
        queue.submitAction(new InstantAction() {
            @Override
            public void update() {
                hardware.getIntakeSystem().setAuto(true);
                hardware.getIntakeSystem().setPower(1);
                drive.setPoseEstimate(new Pose2d(0, 0, drive.getPoseEstimate().getHeading()));
            }
        });
        queue.submitAction(new TimedAction(400) {
            @Override
            public void update() {
                drive.setDrivePower(new Pose2d(-0.4, 0, 0));
            }
        });

        queue.submitAction(new TimedAction(250) {
            @Override
            public void update() {
                drive.setDrivePower(new Pose2d());
            }
        });

        queue.submitAction(new Action() {
            @Override
            public void update() {
                double angleError = MathUtils.getRadRotDist(drive.getPoseEstimate().getHeading(), Math.toRadians(-110));
                drive.setDrivePower(new Pose2d(0, 0, Math.signum(angleError)));
            }

            @Override
            public boolean shouldDeactivate() {
                double angleError = MathUtils.getRadRotDist(drive.getPoseEstimate().getHeading(), Math.toRadians(-110));
                return Math.abs(Math.toDegrees(angleError)) < 5;
            }
        });

        queue.submitAction(new TimedAction(250) {
            @Override
            public void update() {
                drive.setDrivePower(new Pose2d());
            }
        });

        queue.submitAction(new TimedAction(800) {
            @Override
            public void update() {
                drive.setDrivePower(new Pose2d(-0.4, 0, 0));
            }
        });

        queue.submitAction(new TimedAction(250) {
            @Override
            public void update() {
                drive.setDrivePower(new Pose2d());
            }
        });

        queue.submitAction(new Action() {
            @Override
            public void update() {
                double angleError = MathUtils.getRadRotDist(drive.getPoseEstimate().getHeading(), Math.toRadians(-90));
                drive.setDrivePower(new Pose2d(0, 0, Math.signum(angleError)));
            }

            @Override
            public boolean shouldDeactivate() {
                double angleError = MathUtils.getRadRotDist(drive.getPoseEstimate().getHeading(), Math.toRadians(-90));
                return Math.abs(Math.toDegrees(angleError)) < 5;
            }
        });

        queue.submitAction(new InstantAction() {
            @Override
            public void update() {
                drive.setDrivePower(new Pose2d());
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
