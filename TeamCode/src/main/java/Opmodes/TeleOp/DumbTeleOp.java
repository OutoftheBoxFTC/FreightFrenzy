package Opmodes.TeleOp;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

import Hardware.HardwareSystems.FFSystems.ScoutSystem;
import MathSystems.Vector.Vector3;
import Opmodes.BasicOpmode;
import State.Action.Action;
import State.Action.ActionController;
import State.Action.ActionQueue;
import State.Action.InstantAction;
import State.Action.StandardActions.DelayAction;
import State.Action.StandardActions.ServoProfileAction;
import Utils.GamepadEx.GamepadCallback;
import Utils.GamepadEx.GamepadEx;
import Utils.GamepadEx.GamepadValueCallback;
import Utils.OpmodeStatus;
import Utils.ProgramClock;


@Config
public abstract class DumbTeleOp extends BasicOpmode {
    public static ScoutSystem.SCOUT_TARGET target = ScoutSystem.SCOUT_TARGET.ALLIANCE_HIGH;

    public GamepadEx gamepad1Ex, gamepad2Ex;
    private boolean queuedRetract = false;

    private boolean capIdle;
    @Override
    public void setup() {
        gamepad1Ex = new GamepadEx(gamepad1);
        gamepad2Ex = new GamepadEx(gamepad2);

        OpmodeStatus.bindOnStart(gamepad1Ex);
        OpmodeStatus.bindOnStart(gamepad2Ex);

        startTeleop();

        hardware.getTurretSystem().setScoutFieldTarget(target);

        OpmodeStatus.bindOnStart(() -> {
            if(hardware.getIntakeSystem().itemInTransfer()){
                gamepad1.rumble(Gamepad.RUMBLE_DURATION_CONTINUOUS);
            }else{
                gamepad1.stopRumble();
            }

            if(gamepad1Ex.right_trigger.pressed()){
                hardware.getIntakeSystem().startTransfer();
            }
        });

        OpmodeStatus.bindOnStart(() -> {
            if(!gamepad2Ex.left_trigger.pressed() && !gamepad2Ex.right_trigger.pressed()) {
                if (gamepad1.right_bumper) {
                    if(!queuedRetract && hardware.getTurretSystem().getScoutTarget() == ScoutSystem.SCOUT_STATE.SCORE && hardware.getTurretSystem().isScoutIdle()){
                        queuedRetract = true;
                        ActionQueue queue = new ActionQueue();
                        queue.submitAction(new InstantAction() {
                            @Override
                            public void update() {
                                hardware.getTurretSystem().openArm();
                            }
                        });
                        queue.submitAction(new DelayAction(300));
                        queue.submitAction(new InstantAction() {
                            @Override
                            public void update() {
                                hardware.getTurretSystem().setScoutTarget(ScoutSystem.SCOUT_STATE.HOME_IN_INTAKE);
                                queuedRetract = false;
                            }
                        });
                        ActionController.addAction(queue);
                    } else {
                        hardware.getIntakeSystem().intake();
                    }
                } else if (gamepad1.left_bumper) {
                    hardware.getIntakeSystem().outtake();
                } else {
                    hardware.getIntakeSystem().idleIntake();
                }
            }
        });

        OpmodeStatus.bindOnStart(() -> telemetry.addData("Intake Distance", hardware.getIntakeSystem().getTransfer()));

        OpmodeStatus.bindOnStart(() -> {
            double speed = 1;
            if(hardware.getTurretSystem().getFieldTarget() == ScoutSystem.SCOUT_TARGET.PASSTHROUGH){
                if(gamepad1.left_stick_y > 0){
                    speed = 0.8;
                }
            }

            if(!capIdle){
                speed = 0.35;
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
                hardware.getTurretSystem().setScoutTarget(ScoutSystem.SCOUT_STATE.PRELOAD_ANGLE);
            }
            if(gamepad1Ex.left_trigger.pressed()){
                hardware.getTurretSystem().setScoutTarget(ScoutSystem.SCOUT_STATE.HOME_IN_INTAKE);
            }
        });

        OpmodeStatus.bindOnStart(() -> {
            FtcDashboard.getInstance().getTelemetry().addData("Current", hardware.getTurretSystem().getExtensionMotor().getCurrent(CurrentUnit.AMPS));
            FtcDashboard.getInstance().getTelemetry().addData("Bl", hardware.getDrivetrainSystem().getBl().getMotor().getCurrent(CurrentUnit.AMPS));
            FtcDashboard.getInstance().getTelemetry().addData("Br", hardware.getDrivetrainSystem().getBr().getMotor().getCurrent(CurrentUnit.AMPS));
            FtcDashboard.getInstance().getTelemetry().addData("Tl", hardware.getDrivetrainSystem().getTl().getMotor().getCurrent(CurrentUnit.AMPS));
            FtcDashboard.getInstance().getTelemetry().addData("Tr", hardware.getDrivetrainSystem().getTr().getMotor().getCurrent(CurrentUnit.AMPS));
            FtcDashboard.getInstance().getTelemetry().addData("ODO", hardware.getDrivetrainSystem().getOdometryPosition());
            FtcDashboard.getInstance().getTelemetry().update();
        });

        OpmodeStatus.bindOnStart(new Action() {
            @Override
            public void update() {
                if(gamepad1.dpad_right){
                    hardware.getTurretSystem().disableExtensionPID();
                    hardware.getTurretSystem().setExtensionMotorPower(1);
                }else{
                    hardware.getTurretSystem().enableExtensionPID();
                }
            }
        });

        gamepad1Ex.dpad_up.bindOnPress(() -> hardware.getTurretSystem().bypassSetState(ScoutSystem.SCOUT_STATE.PRELOAD_ANGLE));

        OpmodeStatus.bindOnStart(new Action() {
            boolean down = false, up = false;
            double lastPos = 0;
            @Override
            public void update() {
                double pos = 0.7;
                if(gamepad2Ex.dpad_down.toggled()){
                    pos = 0.29;
                    down = true;
                    up = false;
                    gamepad2Ex.dpad_up.overrideToggle(false);
                    gamepad2Ex.dpad_right.overrideToggle(false);
                }else if(down){
                    pos = 0.235;
                }

                if(gamepad2Ex.dpad_up.toggled()){
                    pos = 0.55;
                    up = true;
                    down = false;
                    gamepad2Ex.dpad_down.overrideToggle(false);
                    gamepad2Ex.dpad_right.overrideToggle(false);
                }else if(up){
                    pos = 0.45;
                }

                if(gamepad2Ex.dpad_right.toggled()){
                    gamepad2Ex.dpad_down.overrideToggle(false);
                    gamepad2Ex.dpad_up.overrideToggle(false);
                    down = false;
                    up = false;
                }

                FtcDashboard.getInstance().getTelemetry().addData("Pos", pos);

                if(lastPos != pos){
                    ActionController.addAction(new ServoProfileAction(hardware.getIntakeSystem().getCapServo(), 2, (pos == 0.55 || pos == 0.45) ? 0.25 : 1, pos));
                }
                if(pos == 0.7){
                    hardware.getIntakeSystem().getCapServo().setPosition(0.7);
                    capIdle = true;
                }else{
                    capIdle = false;
                }
                lastPos = pos;
            }
        });

        hardware.getTurretSystem().setExtensionPreload(6);
        gamepad2Ex.b.bindOnPress(() -> {
            hardware.getTurretSystem().setExtensionScoreOffset(0);
            hardware.getTurretSystem().setScoutFieldTarget(ScoutSystem.SCOUT_TARGET.ALLIANCE_HIGH);
            hardware.getTurretSystem().setExtensionPreload(6);
        });
        gamepad2Ex.x.bindOnPress(() -> {
            hardware.getTurretSystem().setExtensionScoreOffset(0);
            hardware.getTurretSystem().setScoutFieldTarget(ScoutSystem.SCOUT_TARGET.SHARED);
            hardware.getTurretSystem().setExtensionPreload(6);
        });
        gamepad2Ex.y.bindOnPress(() -> {
            hardware.getTurretSystem().setExtensionScoreOffset(0);
            hardware.getTurretSystem().setScoutFieldTarget(ScoutSystem.SCOUT_TARGET.PASSTHROUGH);
            hardware.getTurretSystem().setExtensionPreload(20);
        });
        gamepad2Ex.right_bumper.bindOnPress(() -> {
            hardware.getTurretSystem().setScoutFieldTarget(ScoutSystem.SCOUT_TARGET.CAP_GRAB);
            hardware.getTurretSystem().setScoutTarget(ScoutSystem.SCOUT_STATE.SCORE);
            hardware.getTurretSystem().setExtensionPreload(6);
            hardware.getTurretSystem().setExtensionScoreOffset(0);
        });
        gamepad2Ex.left_bumper.bindOnPress(() -> {
            hardware.getTurretSystem().setScoutFieldTarget(ScoutSystem.SCOUT_TARGET.PASSTHROUGH);
            hardware.getTurretSystem().setScoutTarget(ScoutSystem.SCOUT_STATE.SCORE);
            hardware.getTurretSystem().setExtensionPreload(6);
            hardware.getTurretSystem().setExtensionScoreOffset(0);
        });

        OpmodeStatus.bindOnStart(new Action() {
            @Override
            public void update() {
                if(hardware.getTurretSystem().getFieldTarget() == ScoutSystem.SCOUT_TARGET.CAP_GRAB ||
                    hardware.getTurretSystem().getFieldTarget() == ScoutSystem.SCOUT_TARGET.CAP_PLACE){
                    double pitch = -gamepad2.left_stick_y;
                    hardware.getTurretSystem().setPitchMotorPower(pitch);
                }
            }
        });
    }

    public abstract void startTeleop();
}
