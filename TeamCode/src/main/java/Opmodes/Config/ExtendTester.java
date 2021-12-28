package Opmodes.Config;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import MathSystems.Angle;
import Opmodes.BasicOpmode;
import State.Action.Action;
import State.Action.ActionController;
import Utils.OpmodeStatus;

@TeleOp
@Config
public class ExtendTester extends BasicOpmode {
    public static double TARGET = 0;
    @Override
    public void setup() {
        OpmodeStatus.bindOnStart(new Action() {
            long timer;

            @Override
            public void initialize() {
                //hardware.getTurretSystem().setTurretPowerRaw(POWER);
            }

            @Override
            public void update() {
                hardware.getTurretSystem().moveExtensionRaw(TARGET);
            }

            @Override
            public boolean shouldDeactivate() {
                return false;
            }
        });
        ActionController.addAction(new Action() {
            @Override
            public void update() {
                TelemetryPacket packet = new TelemetryPacket();
                packet.put("Pos", hardware.getTurretSystem().getExtensionPosition());
                FtcDashboard.getInstance().getTelemetry().addData("Pos", hardware.getTurretSystem().getExtensionPosition());
                FtcDashboard.getInstance().getTelemetry().update();
            }

            @Override
            public boolean shouldDeactivate() {
                return false;
            }
        });
    }
}
