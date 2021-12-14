package Opmodes.Config;

import com.acmerobotics.dashboard.FtcDashboard;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;

import Opmodes.BasicOpmode;
import State.Action.Action;
import Utils.GamepadEx.GamepadEx;
import Utils.GamepadEx.GamepadValueCallback;
import Utils.OpmodeStatus;
@TeleOp
public class ExtensionTest extends BasicOpmode {
    @Override
    public void setup() {
        OpmodeStatus.bindOnStart(new Action() {
            @Override
            public void update() {
                telemetry.addData("Pos", hardware.getTurretSystem().getExtensionPosition());
                hardware.getTurretSystem().setExtensionMotorPower(gamepad1.right_stick_y);
                FtcDashboard.getInstance().getTelemetry().addData("Max", 9.2);
                FtcDashboard.getInstance().getTelemetry().addData("Current", hardware.getTurretSystem().getExtensionMotor().getMotor().getCurrent(CurrentUnit.AMPS));
                FtcDashboard.getInstance().getTelemetry().update();
            }

            @Override
            public boolean shouldDeactivate() {
                return false;
            }
        });
    }
}
