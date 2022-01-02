package Opmodes.Auto;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.DcMotor;

import Drive.DriveSystem;
import Hardware.HardwareSystems.FFSystems.Actions.BlueGoalActions;
import Hardware.HardwareSystems.FFSystems.Actions.LeaveIntakeAction;
import MathSystems.Angle;
import MathSystems.Position;
import Odometry.FusionOdometer;
import Opmodes.BasicOpmode;
import State.Action.Action;
import State.Action.ActionController;
import State.Action.ActionQueue;
import State.Action.InstantAction;
import State.Action.StandardActions.DelayAction;
import Utils.OpmodeStatus;
@Autonomous
@Config
public class BlueAuto extends BasicOpmode {
    public static LEVEL level = LEVEL.LOW;

    Position position, velocity;

    @Override
    public void setup() {
        position = Position.ZERO();
        velocity = Position.ZERO();

        ActionController.addAction(() -> telemetry.addData("Position", position));

        OpmodeStatus.bindOnStart(new FusionOdometer(hardware.getOdometrySystem(), hardware.getDrivetrainSystem(), position, velocity));

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
            }
        });

        ActionController.addAction(initQueue);

        ActionQueue runQueue = new ActionQueue();
        runQueue.submitAction(new Action() {
            @Override
            public void update() {
                if(level == LEVEL.HIGH) {
                    hardware.getTurretSystem().movePitchRaw(Angle.degrees(-13));
                    hardware.getTurretSystem().moveExtensionRaw(810);
                    hardware.getTurretSystem().moveTurretRaw(Angle.degrees(-48));
                }
                if(level == LEVEL.MED){
                    hardware.getTurretSystem().movePitchRaw(Angle.degrees(10));
                    hardware.getTurretSystem().moveExtensionRaw(780);
                    hardware.getTurretSystem().moveTurretRaw(Angle.degrees(-48));
                }
                if(level == LEVEL.LOW){
                    hardware.getTurretSystem().movePitchRaw(Angle.degrees(20));
                    hardware.getTurretSystem().moveExtensionRaw(780);
                    hardware.getTurretSystem().moveTurretRaw(Angle.degrees(-48));
                }
            }

            @Override
            public boolean shouldDeactivate() {
                return hardware.getTurretSystem().isExtensionAtPos();
            }
        });
        runQueue.submitAction(new DelayAction(1000));
        runQueue.submitAction(new InstantAction() {
            @Override
            public void update() {
                if(level == LEVEL.HIGH) {
                    hardware.getTurretSystem().setBucketPosRaw(0.85);
                }else{
                    hardware.getTurretSystem().setBucketPosRaw(0.85);
                }
            }
        });

        runQueue.submitAction(new DelayAction(850));

        runQueue.submitAction(new InstantAction() {
            @Override
            public void update() {
                if(level == LEVEL.HIGH) {
                    hardware.getTurretSystem().setBucketPosRaw(0.4);
                }else{
                    hardware.getTurretSystem().setBucketPosRaw(0.5);
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

        runQueue.submitAction(new DelayAction(1000));

        runQueue.submitAction(system.gotoGvf(new Position(10, 0, Angle.ZERO())));

        OpmodeStatus.bindOnStart(runQueue);
    }

    public enum LEVEL{
        HIGH,
        MED,
        LOW
    }
}
