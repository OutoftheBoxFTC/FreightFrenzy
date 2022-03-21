package Opmodes.TestOpmodes;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import Opmodes.BasicOpmode;
import State.Action.Action;
import Utils.OpmodeStatus;

public class TurretGamepad extends BasicOpmode {
    @Override
    public void setup() {
        OpmodeStatus.bindOnStart(() -> {
            hardware.getTurretSystem().setTurretMotorPower(gamepad1.right_stick_x);
        });
    }
}
