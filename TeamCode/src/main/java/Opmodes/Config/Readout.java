package Opmodes.Config;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import Opmodes.BasicOpmode;
import State.Action.Action;
import Utils.OpmodeStatus;

@TeleOp
public class Readout extends BasicOpmode {
    @Override
    public void setup() {
        OpmodeStatus.bindOnStart(new Action() {
            @Override
            public void update() {
                telemetry.addData("Pitch Pos", hardware.getTurretSystem().getPitchPosition().degrees());
                telemetry.addData("Turret Pos", hardware.getTurretSystem().getTurretPosition().degrees());
                telemetry.addData("Extension Pos", hardware.getTurretSystem().getExtensionPosition());

                telemetry.addData("Intake Bucket", hardware.getIntakeSystem().getDistance());
            }
        });
    }
}
