package Opmodes.TestOpmodes;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import Opmodes.BasicOpmode;
import State.Action.Action;
import Utils.OpmodeStatus;
@TeleOp
public class IntakeTester extends BasicOpmode {
    @Override
    public void setup() {
        OpmodeStatus.bindOnStart(() -> {
            if(gamepad1.right_bumper){
                hardware.getIntakeSystem().intake();
            }else if(gamepad1.left_bumper){
                hardware.getIntakeSystem().outtake();
            }
        });
    }
}
