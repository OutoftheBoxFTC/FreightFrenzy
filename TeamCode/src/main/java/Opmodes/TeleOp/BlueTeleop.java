package Opmodes.TeleOp;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import Hardware.HardwareSystems.FFSystems.ScoutSystem;
import State.Action.Action;
import Utils.OpmodeStatus;

@TeleOp
public class BlueTeleop extends DumbTeleOp {
    @Override
    public void startTeleop() {
        hardware.getTurretSystem().setScoutAlliance(ScoutSystem.SCOUT_ALLIANCE.BLUE);

        OpmodeStatus.bindOnStart(new Action() {
            @Override
            public void update() {
                if(gamepad2Ex.right_trigger.pressed()){
                    //hardware.getIntakeSystem().spinDuckBlue();
                }else if(gamepad2Ex.left_trigger.pressed()){
                    //hardware.getIntakeSystem().setDuckPower(1);
                }else{
                   // hardware.getIntakeSystem().idleIntake();
                }
            }
        });
    }
}
