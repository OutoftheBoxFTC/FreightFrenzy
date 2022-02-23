package Opmodes.TeleOp;

import Hardware.HardwareSystems.FFSystems.ScoutSystem;
import Opmodes.BasicOpmode;
import State.Action.Action;
import Utils.OpmodeStatus;

public class DumbTeleOp extends BasicOpmode {
    @Override
    public void setup() {
        OpmodeStatus.bindOnStart(() -> {
            if(hardware.getIntakeSystem().getDistance() > 30 && hardware.getTurretSystem().getCurrentState() == ScoutSystem.SCOUT_STATE.HOME_IN_INTAKE) {
                if (gamepad1.right_bumper) {
                    hardware.getIntakeSystem().intake();
                } else if (gamepad1.left_bumper) {
                    hardware.getIntakeSystem().outtake();
                } else {
                    hardware.getIntakeSystem().idleIntake();
                }
            }
        });

        OpmodeStatus.bindOnStart(() -> {
            if(hardware.getIntakeSystem().getDistance() < 30 && hardware.getTurretSystem().getScoutTarget() == ScoutSystem.SCOUT_STATE.HOME_IN_INTAKE){
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
    }
}
