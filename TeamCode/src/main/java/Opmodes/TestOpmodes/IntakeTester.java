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
                hardware.getTurretSystem().openArm();
            }else if(gamepad1.left_bumper){
                hardware.getIntakeSystem().outtake();
                hardware.getTurretSystem().openArm();
            }else{
                hardware.getIntakeSystem().idleIntake();
                hardware.getTurretSystem().closeArm();
            }
        });

        OpmodeStatus.bindOnStart(() -> telemetry.addData("Intake Distance", hardware.getIntakeSystem().getDistance()));
    }
}
