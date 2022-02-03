package Opmodes.Config;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import Opmodes.BasicOpmode;
import State.Action.Action;
import Utils.OpmodeStatus;

@TeleOp
@Config
public class IntakeTester extends BasicOpmode {
    public static double POWER = 0.3;
    @Override
    public void setup() {
        OpmodeStatus.bindOnStart(new Action() {
            @Override
            public void update() {
                hardware.getIntakeSystem().setPowerNormalized(POWER);
            }
        });
    }
}
