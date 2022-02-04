package Opmodes.Config;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import Opmodes.BasicOpmode;
import State.Action.Action;
import State.Action.ActionController;
import Utils.OpmodeStatus;

@TeleOp
@Config
public class TurretJog extends BasicOpmode {
    public static double POWER = 0.5;
    public static double TIME = 100;
    @Override
    public void setup() {
        OpmodeStatus.bindOnStart(new Action() {
            long timer;

            @Override
            public void initialize() {
                timer = System.currentTimeMillis() + ((long)TIME);
                hardware.getTurretSystem().setTurretMotorPower(POWER);
                //hardware.getTurretSystem().moveTurretRaw(Angle.degrees(165));
            }

            @Override
            public void update() {
                if(System.currentTimeMillis() > timer){
                    hardware.getTurretSystem().setTurretMotorPower(0);
                }
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
                packet.put("Pos", hardware.getTurretSystem().getTurretPosition().degrees());
                packet.put("Vel", hardware.getTurretSystem().getTurretVel().degrees());
                telemetry.addData("Encoder", hardware.getTurretSystem().getTurretEncoderPos());
                FtcDashboard.getInstance().getTelemetry().addData("Pos", hardware.getTurretSystem().getTurretPosition().degrees());
                FtcDashboard.getInstance().getTelemetry().addData("Vel", hardware.getTurretSystem().getTurretVel().degrees());
                FtcDashboard.getInstance().getTelemetry().update();
            }

            @Override
            public boolean shouldDeactivate() {
                return false;
            }
        });
    }
}
