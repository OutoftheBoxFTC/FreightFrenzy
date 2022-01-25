package Opmodes.Config;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import Opmodes.BasicOpmode;
import State.Action.Action;
import Utils.OpmodeStatus;

@TeleOp
public class IntakeTesting extends BasicOpmode {
    @Override
    public void setup() {
        OpmodeStatus.bindOnStart(() -> {
            if(hardware.getIntakeSystem().inIntake()){
                hardware.getIntakeSystem().setPower(-1);
            }else{
                hardware.getIntakeSystem().setPower(1);
            }
        });
    }
}
