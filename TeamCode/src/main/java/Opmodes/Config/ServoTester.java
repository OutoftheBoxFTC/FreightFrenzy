package Opmodes.Config;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import Opmodes.BasicOpmode;
import Utils.OpmodeStatus;

@TeleOp
@Config
public class ServoTester extends BasicOpmode {
    public static double POS = 0.5;
    @Override
    public void setup() {
        OpmodeStatus.bindOnStart(() -> hardware.getTurretSystem().openArm());
    }
}
