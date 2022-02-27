package Opmodes.TestOpmodes;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import Opmodes.BasicOpmode;
import State.Action.Action;
import Utils.OpmodeStatus;

@TeleOp
public class FullGamepadControl extends BasicOpmode {
    @Override
    public void setup() {
        OpmodeStatus.bindOnStart(new Action() {
            @Override
            public void update() {
                hardware.getTurretSystem().setExtensionMotorPower(-gamepad1.left_stick_y);
                hardware.getTurretSystem().setTurretMotorPower(gamepad1.left_stick_x);
                hardware.getTurretSystem().setPitchMotorPower(-gamepad1.right_stick_y);
                hardware.getTurretSystem().getBucketServo().disableServo();

                telemetry.addData("Extension", hardware.getTurretSystem().getExtensionPosition());
                telemetry.addData("Pitch", hardware.getTurretSystem().getPitchMotorPos());
                telemetry.addData("Turret", hardware.getTurretSystem().getTurretEncoderPos());
            }
        });
    }
}
