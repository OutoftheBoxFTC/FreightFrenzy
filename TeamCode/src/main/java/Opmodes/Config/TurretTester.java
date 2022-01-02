package Opmodes.Config;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import java.nio.file.attribute.FileTime;

import MathSystems.Angle;
import Opmodes.BasicOpmode;
import State.Action.Action;
import State.Action.ActionController;
import Utils.OpmodeStatus;
@TeleOp
@Config
public class TurretTester extends BasicOpmode {
    public static double TARGET = 0.01;
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
                hardware.getTurretSystem().moveTurretRaw(Angle.degrees(TARGET));
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
                FtcDashboard.getInstance().getTelemetry().addData("Pos", hardware.getTurretSystem().getTurretPosition().degrees());
                FtcDashboard.getInstance().getTelemetry().addData("Vel", hardware.getTurretSystem().getTurretVel().degrees());
                FtcDashboard.getInstance().getTelemetry().addData("Gyro", hardware.getDrivetrainSystem().getImuAngle().degrees());
                FtcDashboard.getInstance().getTelemetry().update();
            }

            @Override
            public boolean shouldDeactivate() {
                return false;
            }
        });
    }
}
