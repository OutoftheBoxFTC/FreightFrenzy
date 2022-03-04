package Opmodes.TeleOp;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;

import Hardware.HardwareSystems.FFSystems.ScoutSystem;
import MathSystems.Vector.Vector3;
import Opmodes.BasicOpmode;
import State.Action.Action;
import Utils.OpmodeStatus;

@TeleOp
@Config
public class DumbTeleOp extends BasicOpmode {
    public static ScoutSystem.SCOUT_ALLIANCE alliance = ScoutSystem.SCOUT_ALLIANCE.RED;
    public static ScoutSystem.SCOUT_TARGET target = ScoutSystem.SCOUT_TARGET.ALLIANCE_HIGH;
    @Override
    public void setup() {
        OpmodeStatus.bindOnStart(() -> {
            hardware.getTurretSystem().setScoutAlliance(alliance);
            hardware.getTurretSystem().setScoutFieldTarget(target);
        });
        OpmodeStatus.bindOnStart(() -> {
            if(hardware.getTurretSystem().getCurrentState() == ScoutSystem.SCOUT_STATE.HOME_IN_INTAKE) {
                if (gamepad1.right_bumper) {
                    hardware.getIntakeSystem().intake();
                } else if (gamepad1.left_bumper) {
                    hardware.getIntakeSystem().outtake();
                }
                if(hardware.getIntakeSystem().itemInIntake()){
                    hardware.getIntakeSystem().idleIntake();
                    hardware.getTurretSystem().closeArm();
                    hardware.getTurretSystem().setScoutTarget(ScoutSystem.SCOUT_STATE.PRELOAD_ANGLE);
                }
            }
        });

        OpmodeStatus.bindOnStart(() -> telemetry.addData("Intake Distance", hardware.getIntakeSystem().getDistance()));

        OpmodeStatus.bindOnStart(() -> hardware.getDrivetrainSystem().setPower(new Vector3(gamepad1.left_stick_x, -gamepad1.left_stick_y, gamepad1.right_stick_x)));

        OpmodeStatus.bindOnStart(() -> {
            if(gamepad1.a){
                hardware.getIntakeSystem().idleIntake();
                hardware.getTurretSystem().setScoutTarget(ScoutSystem.SCOUT_STATE.PRELOAD_ANGLE);
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

        OpmodeStatus.bindOnStart(new Action() {
            @Override
            public void update() {
                FtcDashboard.getInstance().getTelemetry().addData("Current", hardware.getTurretSystem().getExtensionMotor().getCurrent(CurrentUnit.AMPS));
                FtcDashboard.getInstance().getTelemetry().addData("Bl", hardware.getDrivetrainSystem().getBl().getMotor().getCurrent(CurrentUnit.AMPS));
                FtcDashboard.getInstance().getTelemetry().addData("Br", hardware.getDrivetrainSystem().getBr().getMotor().getCurrent(CurrentUnit.AMPS));
                FtcDashboard.getInstance().getTelemetry().addData("Tl", hardware.getDrivetrainSystem().getTl().getMotor().getCurrent(CurrentUnit.AMPS));
                FtcDashboard.getInstance().getTelemetry().addData("Tr", hardware.getDrivetrainSystem().getTr().getMotor().getCurrent(CurrentUnit.AMPS));
                FtcDashboard.getInstance().getTelemetry().update();
            }
        });
    }
}
