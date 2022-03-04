package Opmodes.TeleOp;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import Hardware.HardwareSystems.FFSystems.ScoutSystem;

@TeleOp
public class RedTeleop extends DumbTeleOp {
    @Override
    public void startTeleop() {
        hardware.getTurretSystem().setScoutAlliance(ScoutSystem.SCOUT_ALLIANCE.RED);
    }
}
