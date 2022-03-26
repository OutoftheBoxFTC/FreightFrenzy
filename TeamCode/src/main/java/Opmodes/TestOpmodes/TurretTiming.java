package Opmodes.TestOpmodes;

import com.acmerobotics.dashboard.FtcDashboard;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.navigation.VoltageUnit;

import Hardware.HardwareSystems.FFSystems.Actions.MoveScoutAction;
import Hardware.HardwareSystems.FFSystems.ScoutSystem;
import Opmodes.BasicOpmode;
import State.Action.Action;
import State.Action.ActionController;
import State.Action.ActionQueue;
import State.Action.InstantAction;
import State.Action.StandardActions.DelayAction;
import Utils.OpmodeStatus;

@TeleOp
public class TurretTiming extends BasicOpmode {
    @Override
    public void setup() {
        hardware.getTurretSystem().setScoutFieldTarget(ScoutSystem.SCOUT_TARGET.ALLIANCE_HIGH);
        hardware.getTurretSystem().setScoutAlliance(ScoutSystem.SCOUT_ALLIANCE.RED);

        ActionQueue queue = new ActionQueue();
        queue.submitAction(new MoveScoutAction(hardware.getTurretSystem(), ScoutSystem.SCOUT_STATE.SCORE));
        queue.submitAction(new DelayAction(500));
        queue.submitAction(new MoveScoutAction(hardware.getTurretSystem(), ScoutSystem.SCOUT_STATE.HOME_IN_INTAKE));
        OpmodeStatus.bindOnStart(queue);

        ActionController.addAction(() -> {
            FtcDashboard.getInstance().getTelemetry().addData("State", hardware.getTurretSystem().getCurrentState());
            FtcDashboard.getInstance().getTelemetry().addData("Voltage", hardware.getControlHub().getModule().getInputVoltage(VoltageUnit.VOLTS));
            FtcDashboard.getInstance().getTelemetry().update();
        });
    }
}
