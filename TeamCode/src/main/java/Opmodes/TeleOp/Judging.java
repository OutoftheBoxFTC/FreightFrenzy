package Opmodes.TeleOp;

import com.acmerobotics.dashboard.FtcDashboard;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import Hardware.HardwareSystems.FFSystems.Actions.MoveScoutAction;
import Hardware.HardwareSystems.FFSystems.ScoutSystem;
import Opmodes.BasicOpmode;
import State.Action.ActionController;
import State.Action.ActionQueue;
import State.Action.InstantAction;
import State.Action.StandardActions.DelayAction;
import Utils.GamepadEx.GamepadCallback;
import Utils.GamepadEx.GamepadEx;
import Utils.OpmodeStatus;

@TeleOp
public class Judging extends BasicOpmode {

    @Override
    public void setup() {
        GamepadEx gamepad1Ex = new GamepadEx(gamepad1);
        ActionController.addAction(gamepad1Ex);

        gamepad1Ex.b.bindOnPress(new GamepadCallback() {
            @Override
            public void call() {
                ActionQueue queue = new ActionQueue();
                hardware.getTurretSystem().setScoutAlliance(ScoutSystem.SCOUT_ALLIANCE.BLUE);
                hardware.getTurretSystem().setScoutFieldTarget(ScoutSystem.SCOUT_TARGET.ALLIANCE_HIGH);
                queue.submitAction(new InstantAction() {
                    @Override
                    public void update() {
                        hardware.getIntakeSystem().moveCameraBlueTSE();
                    }
                });
                queue.submitAction(new MoveScoutAction(hardware.getTurretSystem(), ScoutSystem.SCOUT_STATE.SCORE));
                queue.submitAction(new MoveScoutAction(hardware.getTurretSystem(), ScoutSystem.SCOUT_STATE.HOME_IN_INTAKE));
                queue.submitAction(new InstantAction() {
                    @Override
                    public void update() {
                        hardware.getIntakeSystem().moveCameraRedTSE();
                        hardware.getTurretSystem().setScoutFieldTarget(ScoutSystem.SCOUT_TARGET.PASSTHROUGH);
                    }
                });
                queue.submitAction(new MoveScoutAction(hardware.getTurretSystem(), ScoutSystem.SCOUT_STATE.SCORE));
                queue.submitAction(new MoveScoutAction(hardware.getTurretSystem(), ScoutSystem.SCOUT_STATE.HOME_IN_INTAKE));

                ActionController.addAction(queue);
            }
        });

        gamepad1Ex.x.bindOnPress(() -> {
            ActionQueue queue = new ActionQueue();
            queue.submitAction(new InstantAction() {
                @Override
                public void update() {
                    hardware.getIntakeSystem().setPower(1);
                }
            });
            queue.submitAction(new DelayAction(500));
            queue.submitAction(new InstantAction() {
                @Override
                public void update() {
                    hardware.getIntakeSystem().setPower(0);
                }
            });

            ActionController.addAction(queue);
        });

        gamepad1Ex.y.bindOnPress(() -> {
            hardware.getTurretSystem().disableScout();
            hardware.getTurretSystem().bypassSetState(ScoutSystem.SCOUT_STATE.HOMING);
            hardware.getTurretSystem().setBucketScore();
            ActionController.addAction(() -> {
                hardware.getTurretSystem().setExtensionMotorPower(-gamepad1.right_stick_x);
                hardware.getTurretSystem().setTurretMotorPower(gamepad1.left_stick_x);
                hardware.getTurretSystem().setPitchMotorPower(-gamepad1.left_stick_y);

                if(gamepad1.a){
                    hardware.getTurretSystem().setArmPos(1);
                }
                if(gamepad1.b){
                    hardware.getTurretSystem().closeArm();
                }
                if(gamepad1.x){
                    hardware.getTurretSystem().setBucketScore();
                }
                if(gamepad1.y){
                    hardware.getTurretSystem().setBucketPreset();
                }

                if(gamepad1.right_bumper){
                    hardware.getIntakeSystem().setPower(0.001);
                }
                if(gamepad1.left_bumper){
                    hardware.getIntakeSystem().setPower(0);
                }

                telemetry.addData("Extension", hardware.getTurretSystem().getExtensionPosition());
                telemetry.addData("Pitch", hardware.getTurretSystem().getPitchMotorPos());
                telemetry.addData("Turret", hardware.getTurretSystem().getTurretEncoderPos());
                telemetry.addData("Turret Angle", hardware.getTurretSystem().getTurretPotAngle().degrees());
                telemetry.addData("Pitch Angle", hardware.getTurretSystem().getPitchPot().getAngle().degrees());
                FtcDashboard.getInstance().getTelemetry().addData("Pitch Angle", hardware.getTurretSystem().getPitchPot().getAngle().degrees());
                FtcDashboard.getInstance().getTelemetry().addData("Turret Angle", hardware.getTurretSystem().getTurretPotAngle().degrees());
                FtcDashboard.getInstance().getTelemetry().update();
            });
        });
    }
}
