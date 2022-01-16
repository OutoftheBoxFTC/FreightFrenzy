package Opmodes.Config;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import Opmodes.BasicOpmode;
import State.Action.Action;
import Utils.OpmodeStatus;

@TeleOp
public class IntakeTesting extends BasicOpmode {
    @Override
    public void setup() {
        OpmodeStatus.bindOnStart(() -> hardware.getIntakeSystem().setPower(1));
    }
}
