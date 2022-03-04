package Opmodes.TestOpmodes;

import com.acmerobotics.dashboard.FtcDashboard;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import Hardware.HardwareSystems.FFSystems.ScoutSystem;
import Opmodes.BasicOpmode;
import State.Action.Action;
import Utils.OpmodeStatus;
import Utils.ProgramClock;

@TeleOp
public class PTZTester extends BasicOpmode {
    @Override
    public void setup() {
        hardware.getTurretSystem().disableScout();
        hardware.getTurretSystem().setScoutTarget(ScoutSystem.SCOUT_STATE.HOMING);
        hardware.getTurretSystem().getBucketServo().disableServo();
        OpmodeStatus.bindOnStart(new Action() {
            double pan = 0.5, tilt = 0.5, zoom = 0;
            @Override
            public void update() {
                hardware.getIntakeSystem().getCameraServo().setPosition(tilt);
                hardware.getIntakeSystem().getPanServo().setPosition(pan);

                pan += ProgramClock.getFrameTimeSeconds() * gamepad1.left_stick_x;
                tilt += ProgramClock.getFrameTimeSeconds() * gamepad1.left_stick_y;
                zoom += ProgramClock.getFrameTimeSeconds() * (gamepad1.dpad_up ? 1 : gamepad1.dpad_down ? -1 : 0);

                if(zoom > 0.45){
                    zoom = 0.45;
                }
                if(zoom < 0){
                    zoom = 0;
                }

                FtcDashboard.getInstance().getTelemetry().addData("Zoom", zoom);
                FtcDashboard.getInstance().getTelemetry().update();
                hardware.getIntakeSystem().setZoom(zoom);
            }
        });
    }
}
