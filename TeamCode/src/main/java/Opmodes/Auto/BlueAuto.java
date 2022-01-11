package Opmodes.Auto;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.DcMotor;

import Drive.DriveSystem;
import Hardware.HardwareSystems.FFSystems.Actions.BlueGoalActions;
import Hardware.HardwareSystems.FFSystems.Actions.EnterIntakeAction;
import Hardware.HardwareSystems.FFSystems.Actions.LeaveIntakeAction;
import Hardware.HardwareSystems.FFSystems.Actions.MoveExtensionAction;
import Hardware.HardwareSystems.FFSystems.Actions.MovePitchAction;
import MathSystems.Angle;
import MathSystems.Position;
import MathSystems.Vector.Vector3;
import Odometry.FusionOdometer;
import Opmodes.BasicOpmode;
import State.Action.Action;
import State.Action.ActionController;
import State.Action.ActionQueue;
import State.Action.InstantAction;
import State.Action.StandardActions.DelayAction;
import Utils.OpmodeStatus;
import Utils.PathUtils.ContinousPathBuilder;
import Utils.PathUtils.Path;
import Utils.PathUtils.PathBuilder;

@Autonomous
@Config
public class BlueAuto extends BasicOpmode {
    public static LEVEL level = LEVEL.MED;

    Position position, velocity;

    @Override
    public void setup() {
        Path driveInFirst = new ContinousPathBuilder(new Position(6, -5, Angle.ZERO()))
                .lineTo(new Position(3, -5, Angle.ZERO()))
                .lineTo(new Position(3, 20, Angle.ZERO()))
                .build();

        Path enterWarehouse1 = new ContinousPathBuilder(driveInFirst.getEndpoint())
                .lineTo(new Position(3, 45, Angle.ZERO()))
                .build();

        Path intake1 = new ContinousPathBuilder(driveInFirst.getEndpoint())
                .lineTo(new Position(3, 50, Angle.ZERO()))
                .build();

        Path outtake1 = new ContinousPathBuilder(intake1.getEndpoint())
                .lineTo(new Position(3, 40, Angle.ZERO()))
                .build();

        Path score1 = new ContinousPathBuilder(outtake1.getEndpoint())
                .lineTo(new Position(3, -15, Angle.ZERO()))
                .build();

        position = Position.ZERO();
        velocity = Position.ZERO();

        ActionController.addAction(() -> telemetry.addData("Position", position));

        FusionOdometer odometer = new FusionOdometer(hardware.getOdometrySystem(), hardware.getDrivetrainSystem(), position, velocity);
        OpmodeStatus.bindOnStart(odometer);

        DriveSystem system = new DriveSystem(hardware.getDrivetrainSystem(), position);

        hardware.getDrivetrainSystem().setZeroPowerBehaviour(DcMotor.ZeroPowerBehavior.BRAKE);

        ActionQueue initQueue = new ActionQueue();
        initQueue.submitAction(new LeaveIntakeAction(hardware));
        initQueue.submitAction(new DelayAction(2000));
        initQueue.submitAction(new InstantAction() {
            @Override
            public void update() {
                hardware.getDrivetrainSystem().setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
                hardware.getTurretSystem().moveTurretRaw(Angle.degrees(-40));
                hardware.getTurretSystem().movePitchRaw(Angle.degrees(1));
            }
        });

        ActionController.addAction(initQueue);

        ActionQueue runQueue = new ActionQueue();
        runQueue.submitAction(system.gotoGvf(new Position(6, -2, Angle.ZERO())));
        runQueue.submitAction(new Action() {
            @Override
            public void update() {
                if(level == LEVEL.HIGH) {
                    MoveExtensionAction.P = -0.005;
                    hardware.getTurretSystem().movePitchRaw(Angle.degrees(-13));
                    hardware.getTurretSystem().moveExtensionRaw(510);
                    hardware.getTurretSystem().moveTurretRaw(Angle.degrees(-49));
                }
                if(level == LEVEL.MED){
                    MoveExtensionAction.P = -0.002;
                    hardware.getTurretSystem().movePitchRaw(Angle.degrees(4));
                    hardware.getTurretSystem().moveExtensionRaw(470);
                    hardware.getTurretSystem().moveTurretRaw(Angle.degrees(-50));
                }
                if(level == LEVEL.LOW){
                    MoveExtensionAction.P = -0.001;
                    hardware.getTurretSystem().movePitchRaw(Angle.degrees(18));
                    hardware.getTurretSystem().moveExtensionRaw(430);
                    hardware.getTurretSystem().moveTurretRaw(Angle.degrees(-50));
                }
            }

            @Override
            public boolean shouldDeactivate() {
                return hardware.getTurretSystem().isExtensionAtPos();
            }
        });
        runQueue.submitAction(new Action() {
            @Override
            public void update() {
            }

            @Override
            public boolean shouldDeactivate() {
                return hardware.getTurretSystem().isExtensionAtPos();
            }
        });
        runQueue.submitAction(new DelayAction(750));
        runQueue.submitAction(new InstantAction() {
            @Override
            public void update() {
                MoveExtensionAction.P = -0.0045;
                if(level == LEVEL.LOW) {
                    hardware.getTurretSystem().setBucketPosRaw(0.9);
                }else if(level == LEVEL.HIGH){
                    hardware.getTurretSystem().setBucketPosRaw(0.95);
                }
                else{
                    hardware.getTurretSystem().setBucketPosRaw(0.85);
                }
            }
        });

        runQueue.submitAction(new DelayAction(850));

        runQueue.submitAction(new InstantAction() {
            @Override
            public void update() {
                if(level == LEVEL.HIGH) {
                    hardware.getTurretSystem().setBucketPosRaw(0.45);
                }else{
                    hardware.getTurretSystem().setBucketPosRaw(0.45);
                }
            }
        });
        runQueue.submitAction(BlueGoalActions.getBlueAllianceReturnAuto(hardware));
        runQueue.submitAction(new Action() {
            @Override
            public void update() {
                hardware.getTurretSystem().moveTurretRaw(Angle.degrees(0));
            }

            @Override
            public boolean shouldDeactivate() {
                return hardware.getTurretSystem().isTurretAtPos();
            }
        });

        for(int i = 0; i < 2; i ++) {
            runQueue.submitAction(new DelayAction(500));

            runQueue.submitAction(new Action() {
                @Override
                public void update() {
                    hardware.getDrivetrainSystem().setPower(new Vector3(0, 0.8, 0));
                }

                @Override
                public boolean shouldDeactivate() {
                    return position.getY() > 30;
                }
            });

            runQueue.submitAction(new EnterIntakeAction(hardware));

            runQueue.submitAction(system.followGvf(enterWarehouse1));

            runQueue.submitAction(new Action() {
                @Override
                public void update() {

                }

                @Override
                public boolean shouldDeactivate() {
                    return hardware.getTurretSystem().getExtensionPosition() < 10;
                }
            });

            runQueue.submitAction(new DelayAction(300));

            runQueue.submitAction(new InstantAction() {
                @Override
                public void update() {
                    hardware.getIntakeSystem().setPower(1);
                }
            });

            runQueue.submitAction(system.followGvf(intake1));

            runQueue.submitAction(new DelayAction(500));

            runQueue.submitAction(new InstantAction() {
                @Override
                public void update() {
                    hardware.getIntakeSystem().setPower(-0.5);
                }
            });

            runQueue.submitAction(system.followGvf(outtake1));

            runQueue.submitAction(new InstantAction() {
                @Override
                public void update() {
                    hardware.getIntakeSystem().setPower(0.2);
                }
            });

            runQueue.submitAction(new DelayAction(250));

            runQueue.submitAction(new Action() {
                @Override
                public void update() {
                }

                @Override
                public boolean shouldDeactivate() {
                    return hardware.getIntakeSystem().getIntakeStop();
                }

                @Override
                public void onEnd() {
                    hardware.getIntakeSystem().setPower(0);
                }
            });


            runQueue.submitAction(new LeaveIntakeAction(hardware));

            runQueue.submitAction(new InstantAction() {
                @Override
                public void update() {
                    odometer.setUse2mForward(true);
                }
            });

            runQueue.submitAction(new DelayAction(1000));

            runQueue.submitAction(new InstantAction() {
                @Override
                public void update() {
                    odometer.setUse2mForward(false);
                }
            });

            runQueue.submitAction(system.followGvf(score1));

            runQueue.submitAction(BlueGoalActions.getBlueAlliance(hardware, -49, 36));

            runQueue.submitAction(new DelayAction(500));

            runQueue.submitAction(new Action() {
                @Override
                public void update() {

                }

                @Override
                public boolean shouldDeactivate() {
                    return hardware.getTurretSystem().getExtensionPosition() > 500;
                }
            });

            runQueue.submitAction(new DelayAction(250));

            runQueue.submitAction(new InstantAction() {
                @Override
                public void update() {
                    hardware.getTurretSystem().setBucketPosRaw(0.9);
                }
            });

            runQueue.submitAction(new DelayAction(500));

            runQueue.submitAction(new InstantAction() {
                @Override
                public void update() {
                    hardware.getTurretSystem().setBucketPosRaw(0.4);
                }
            });

            runQueue.submitAction(BlueGoalActions.getBlueAllianceReturn(hardware));

            runQueue.submitAction(new DelayAction(1000));
        }

        OpmodeStatus.bindOnStart(runQueue);
    }

    public enum LEVEL{
        HIGH,
        MED,
        LOW
    }
}
