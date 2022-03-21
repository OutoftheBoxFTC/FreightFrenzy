package Opmodes.TestOpmodes;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

import Opmodes.BasicOpmode;
import State.Action.Action;
import State.Action.ActionController;
import Utils.OpmodeStatus;

@Config
public class ExtensionTester extends BasicOpmode {
    public static int EXTENSION_TARGET = 0;
    @Override
    public void setup() {
        OpmodeStatus.bindOnStart(() -> hardware.getTurretSystem().moveExtensionRaw(EXTENSION_TARGET, DistanceUnit.MM));
        OpmodeStatus.bindOnStart(new Action() {
            @Override
            public void update() {
                telemetry.addData("Stuff", hardware.getTurretSystem().getExtensionPosition());
            }
        });
    }
}
