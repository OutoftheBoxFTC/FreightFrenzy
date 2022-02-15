package Opmodes.TestOpmodes;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import MathSystems.Vector.Vector3;
import Opmodes.BasicOpmode;
import State.Action.Action;
import Utils.OpmodeStatus;
@TeleOp
public class DriveTest extends BasicOpmode {
    @Override
    public void setup() {
        OpmodeStatus.bindOnStart(() -> hardware.getDrivetrainSystem().setPower(new Vector3(-gamepad1.left_stick_x, gamepad1.left_stick_y, -gamepad1.right_stick_x)));
    }
}
