package Opmodes.TestOpmodes;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import Opmodes.BasicOpmode;
import State.Action.Action;
import State.Action.ActionController;
import Utils.OpmodeStatus;

@TeleOp
@Config
public class ExtensionTester extends BasicOpmode {
    public static int EXTENSION_TARGET = 0;
    @Override
    public void setup() {
        OpmodeStatus.bindOnStart(() -> hardware.getTurretSystem().moveExtensionRaw(EXTENSION_TARGET));
        OpmodeStatus.bindOnStart(new Action() {
            @Override
            public void update() {
                telemetry.addData("Stuff", hardware.getTurretSystem().getExtensionPosition());
            }
        });
    }
}