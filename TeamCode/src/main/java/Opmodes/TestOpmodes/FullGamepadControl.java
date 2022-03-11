package Opmodes.TestOpmodes;

import com.acmerobotics.dashboard.FtcDashboard;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import Hardware.HardwareSystems.FFSystems.ScoutSystem;
import Opmodes.BasicOpmode;
import State.Action.Action;
import Utils.OpmodeStatus;

@TeleOp
public class FullGamepadControl extends BasicOpmode {
    @Override
    public void setup() {
        hardware.getTurretSystem().disableScout();
        hardware.getTurretSystem().bypassSetState(ScoutSystem.SCOUT_STATE.HOMING);
        hardware.getTurretSystem().setBucketScore();
        OpmodeStatus.bindOnStart(() -> {
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
    }
}
