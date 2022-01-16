package Opmodes.Config;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import Opmodes.BasicOpmode;
import State.Action.Action;
import Utils.OpmodeStatus;
@TeleOp
public class ServoOffTester extends BasicOpmode {
    @Override
    public void setup() {
        OpmodeStatus.bindOnStart(new Action() {
            @Override
            public void update() {
                if(gamepad1.a){
                   hardware.getTurretSystem().getBucketServo().disableServo();
                }else{
                    hardware.getTurretSystem().getBucketServo().enableServo();
                }
                hardware.getTurretSystem().setBucketPosRaw(0.4);
            }
        });
    }
}
