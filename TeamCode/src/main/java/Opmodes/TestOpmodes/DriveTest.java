package Opmodes.TestOpmodes;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import MathSystems.Vector.Vector3;
import Opmodes.BasicOpmode;
import State.Action.Action;
import Utils.OpmodeStatus;

public class DriveTest extends BasicOpmode {
    @Override
    public void setup() {
        OpmodeStatus.bindOnStart(() -> {
            telemetry.addData("TL", hardware.getDrivetrainSystem().getTl().getMotor().getCurrentPosition());
            telemetry.addData("TR", hardware.getDrivetrainSystem().getTr().getMotor().getCurrentPosition());
            telemetry.addData("BL", hardware.getDrivetrainSystem().getBl().getMotor().getCurrentPosition());
            telemetry.addData("BR", hardware.getDrivetrainSystem().getBr().getMotor().getCurrentPosition());
        });
        OpmodeStatus.bindOnStart(() -> hardware.getDrivetrainSystem().setPower(new Vector3(-gamepad1.left_stick_x, gamepad1.left_stick_y, -gamepad1.right_stick_x)));
    }
}
