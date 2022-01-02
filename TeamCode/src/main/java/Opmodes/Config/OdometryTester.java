package Opmodes.Config;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import Opmodes.BasicOpmode;
import State.Action.Action;
import Utils.OpmodeStatus;
@TeleOp
public class OdometryTester extends BasicOpmode {
    @Override
    public void setup() {
        OpmodeStatus.bindOnStart(() -> {
            telemetry.addData("Fl", hardware.getOdometrySystem().getFl());
            telemetry.addData("Fr", hardware.getOdometrySystem().getFr());
            telemetry.addData("Bl", hardware.getOdometrySystem().getBl());
            telemetry.addData("Br", hardware.getOdometrySystem().getBr());

            telemetry.addData("Left", hardware.getOdometrySystem().getLeftDist());
            telemetry.addData("Right", hardware.getOdometrySystem().getRightDist());
        });
    }
}
