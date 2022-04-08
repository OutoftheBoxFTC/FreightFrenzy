package Opmodes.TestOpmodes;

import com.acmerobotics.dashboard.FtcDashboard;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import Opmodes.BasicOpmode;
import State.Action.Action;
import Utils.OpmodeStatus;

@TeleOp
public class FullReadout extends BasicOpmode {
    @Override
    public void setup() {
        hardware.getTurretSystem().disableScout();
        OpmodeStatus.bindOnStart(new Action() {
            @Override
            public void update() {
                Telemetry telemetry = FtcDashboard.getInstance().getTelemetry();
                telemetry.addData("Turret", hardware.getTurretSystem().getTurretPotAngle().degrees());
                telemetry.addData("Pitch", hardware.getTurretSystem().getPitchPot().getAngle().degrees());
                telemetry.addData("Extension", hardware.getTurretSystem().getExtensionPosition());
                telemetry.addData("odo", hardware.getDrivetrainSystem().getOdometryPosition());
                telemetry.addData("Bucket", Double.isNaN(hardware.getIntakeSystem().getBucketSensorDistance()) ? 0 : hardware.getIntakeSystem().getBucketSensorDistance());
                telemetry.addData("Transfer", hardware.getIntakeSystem().getTransfer());
                telemetry.addData("Intake Switch", hardware.getIntakeSystem().getTransferSwitch().getState() ? 1 : 0);
                telemetry.update();
            }
        });
    }
}