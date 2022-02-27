package Opmodes.TestOpmodes;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;

import Opmodes.BasicOpmode;
import State.Action.Action;
import Utils.OpmodeStatus;

@TeleOp
@Config
public class ExtensionPowerTester extends BasicOpmode {
    public static double POWER = 0;
    @Override
    public void setup() {
        OpmodeStatus.bindOnStart(() -> hardware.getTurretSystem().setExtensionMotorPower(POWER));
        OpmodeStatus.bindOnStart(new Action() {
            @Override
            public void update() {
                hardware.getTurretSystem().setBucketPreset();
                FtcDashboard.getInstance().getTelemetry().addData("Current", hardware.getTurretSystem().getExtensionMotor().getCurrent(CurrentUnit.MILLIAMPS));
                FtcDashboard.getInstance().getTelemetry().update();
            }
        });
    }
}
