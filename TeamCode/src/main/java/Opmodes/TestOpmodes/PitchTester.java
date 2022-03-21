package Opmodes.TestOpmodes;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import MathSystems.Angle;
import Opmodes.BasicOpmode;
import State.Action.Action;
import Utils.OpmodeStatus;

@Config
public class PitchTester extends BasicOpmode {
    public static int EXTENSION_TARGET = 0;
    @Override
    public void setup() {
        OpmodeStatus.bindOnStart(() -> hardware.getTurretSystem().movePitchRaw(Angle.degrees(EXTENSION_TARGET)));
        OpmodeStatus.bindOnStart(new Action() {
            @Override
            public void update() {
                telemetry.addData("Stuff", hardware.getTurretSystem().getPitchMotorPos());
            }
        });
    }
}
