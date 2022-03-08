package Opmodes.TeleOp;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

import Hardware.HardwareSystems.FFSystems.ScoutSystem;
import MathSystems.Vector.Vector3;
import Opmodes.BasicOpmode;
import State.Action.Action;
import Utils.GamepadEx.GamepadCallback;
import Utils.GamepadEx.GamepadEx;
import Utils.GamepadEx.GamepadValueCallback;
import Utils.OpmodeStatus;
import Utils.ProgramClock;


@Config
public abstract class DumbTeleOp extends BasicOpmode {
    public static ScoutSystem.SCOUT_TARGET target = ScoutSystem.SCOUT_TARGET.ALLIANCE_HIGH;

    public GamepadEx gamepad1Ex, gamepad2Ex;
    @Override
    public void setup() {
        gamepad1Ex = new GamepadEx(gamepad1);
        gamepad2Ex = new GamepadEx(gamepad2);

        OpmodeStatus.bindOnStart(gamepad1Ex);
        OpmodeStatus.bindOnStart(gamepad2Ex);

        startTeleop();

        hardware.getTurretSystem().setScoutFieldTarget(target);

        OpmodeStatus.bindOnStart(new Action() {
            @Override
            public void update() {
                if(hardware.getIntakeSystem().itemInIntake()){
                    gamepad1.rumble(Gamepad.RUMBLE_DURATION_CONTINUOUS);
                }else{
                    gamepad1.stopRumble();
                }
            }
        });

        OpmodeStatus.bindOnStart(() -> {
            if(!gamepad2Ex.left_trigger.pressed() && !gamepad2Ex.right_trigger.pressed()) {
                if (gamepad1.right_bumper) {
                    hardware.getIntakeSystem().intake();
                } else if (gamepad1.left_bumper) {
                    hardware.getIntakeSystem().outtake();
                } else {
                    hardware.getIntakeSystem().idleIntake();
                }
            }
        });

        OpmodeStatus.bindOnStart(() -> telemetry.addData("Intake Distance", hardware.getIntakeSystem().getDistance()));

        OpmodeStatus.bindOnStart(() -> {
            double speed = 1;
            if(hardware.getTurretSystem().getFieldTarget() == ScoutSystem.SCOUT_TARGET.PASSTHROUGH){
                if(gamepad1.left_stick_y > 0){
                    speed = 0.8;
                }
            }
            hardware.getDrivetrainSystem().setPower(new Vector3(gamepad1.left_stick_x, -gamepad1.left_stick_y, gamepad1.right_stick_x).scale(speed));
        });

        OpmodeStatus.bindOnStart(() -> {
            if(gamepad1.a){
                hardware.getTurretSystem().setScoutTarget(ScoutSystem.SCOUT_STATE.TRANSFER);
            }
            if(gamepad1.x){
                hardware.getTurretSystem().setScoutTarget(ScoutSystem.SCOUT_STATE.SCORE);
            }
            if(gamepad1.y){
                hardware.getTurretSystem().openArm();
            }
            if(gamepad1.b){
                hardware.getTurretSystem().setScoutTarget(ScoutSystem.SCOUT_STATE.HOME_IN_INTAKE);
            }
        });

        OpmodeStatus.bindOnStart(() -> {
            FtcDashboard.getInstance().getTelemetry().addData("Current", hardware.getTurretSystem().getExtensionMotor().getCurrent(CurrentUnit.AMPS));
            FtcDashboard.getInstance().getTelemetry().addData("Bl", hardware.getDrivetrainSystem().getBl().getMotor().getCurrent(CurrentUnit.AMPS));
            FtcDashboard.getInstance().getTelemetry().addData("Br", hardware.getDrivetrainSystem().getBr().getMotor().getCurrent(CurrentUnit.AMPS));
            FtcDashboard.getInstance().getTelemetry().addData("Tl", hardware.getDrivetrainSystem().getTl().getMotor().getCurrent(CurrentUnit.AMPS));
            FtcDashboard.getInstance().getTelemetry().addData("Tr", hardware.getDrivetrainSystem().getTr().getMotor().getCurrent(CurrentUnit.AMPS));
            FtcDashboard.getInstance().getTelemetry().update();
        });
        OpmodeStatus.bindOnStart(new Action() {
            double extendVal = 0, turretVal = 0;
            boolean change = false;
            @Override
            public void update() {
                if (gamepad2.dpad_up) {
                    hardware.getTurretSystem().moveExtensionScoreOffset(ProgramClock.getFrameTimeSeconds() * 10);
                } else if (gamepad2.dpad_down) {
                    hardware.getTurretSystem().moveExtensionScoreOffset(ProgramClock.getFrameTimeSeconds() * -10);
                }
                if (gamepad2.dpad_left) {
                    hardware.getTurretSystem().disableTurretPID();
                    hardware.getTurretSystem().setTurretMotorPower(0.5);
                    hardware.getTurretSystem().setTurretOffset(hardware.getTurretSystem().getTurretPosition().degrees() - turretVal);
                } else if (gamepad2.dpad_right) {
                    hardware.getTurretSystem().disableTurretPID();
                    hardware.getTurretSystem().setTurretMotorPower(-0.5);
                    hardware.getTurretSystem().setTurretOffset(hardware.getTurretSystem().getTurretPosition().degrees() - turretVal);
                }else{
                    hardware.getTurretSystem().enableTurretPID();
                    turretVal = hardware.getTurretSystem().getTurretPosition().degrees();
                }
            }
        });

        gamepad1Ex.dpad_up.bindOnPress(() -> hardware.getTurretSystem().bypassSetState(ScoutSystem.SCOUT_STATE.PRELOAD_ANGLE));

        gamepad2Ex.b.bindOnPress(() -> {
            hardware.getTurretSystem().setExtensionScoreOffset(0);
            hardware.getTurretSystem().setScoutFieldTarget(ScoutSystem.SCOUT_TARGET.ALLIANCE_HIGH);
            hardware.getTurretSystem().setExtensionPreload(12.5);
        });
        gamepad2Ex.x.bindOnPress(() -> {
            hardware.getTurretSystem().setExtensionScoreOffset(0);
            hardware.getTurretSystem().setScoutFieldTarget(ScoutSystem.SCOUT_TARGET.SHARED);
            hardware.getTurretSystem().setExtensionPreload(12.5);
        });
        gamepad2Ex.y.bindOnPress(() -> {
            hardware.getTurretSystem().setScoutFieldTarget(ScoutSystem.SCOUT_TARGET.PASSTHROUGH);
            hardware.getTurretSystem().setExtensionPreload(20);
        });
    }

    public abstract void startTeleop();
}
