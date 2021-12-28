package Opmodes.Config;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import Opmodes.BasicOpmode;
import State.Action.Action;
import Utils.OpmodeStatus;
@TeleOp
public class DtTester extends BasicOpmode {
    @Override
    public void setup() {
        OpmodeStatus.bindOnStart(() -> {
            hardware.getDrivetrainSystem().setPower(0, 0, 0, 0);
            if(gamepad1.a){
                hardware.getDrivetrainSystem().setPower(1, 0, 0, 0); //bl is tl,
            }
            if(gamepad1.b){
                hardware.getDrivetrainSystem().setPower(0, 1, 0, 0); //br is bl
            }
            if(gamepad1.x){
                hardware.getDrivetrainSystem().setPower(0, 0, 1, 0); //tl is tr
            }
            if(gamepad1.y){
                hardware.getDrivetrainSystem().setPower(0, 0, 0, 1); //tr is br
            }
        });
    }
}
