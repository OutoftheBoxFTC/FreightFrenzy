package Opmodes.Config;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import MathSystems.Position;
import Odometry.FusionOdometer;
import Opmodes.BasicOpmode;
import State.Action.Action;
import Utils.OpmodeStatus;
@TeleOp
public class OdometryTester extends BasicOpmode {
    @Override
    public void setup() {
        Position position = Position.ZERO();
        FusionOdometer odometer = new FusionOdometer(hardware.getOdometrySystem(), hardware.getDrivetrainSystem(), position, Position.ZERO());
        OpmodeStatus.bindOnStart(odometer);
        OpmodeStatus.bindOnStart(() -> {
            telemetry.addData("Fl", hardware.getOdometrySystem().getFl());
            telemetry.addData("Fr", hardware.getOdometrySystem().getFr());
            telemetry.addData("Bl", hardware.getOdometrySystem().getBl());
            telemetry.addData("Br", hardware.getOdometrySystem().getBr());

            telemetry.addData("Left", hardware.getOdometrySystem().getLeftDist());
            telemetry.addData("Right", hardware.getOdometrySystem().getRightDist());

            telemetry.addData("Forward", hardware.getOdometrySystem().getForwardDist());

            telemetry.addData("Odo", position);
        });
    }
}
