package Opmodes.TestOpmodes;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import Hardware.HardwareSystems.FFSystems.ScoutSystem;
import Opmodes.BasicOpmode;
import State.Action.Action;
import State.Action.ActionQueue;
import State.Action.InstantAction;
import State.Action.StandardActions.DelayAction;
import Utils.OpmodeStatus;

@Config
public class StagingTesting extends BasicOpmode {
    public static int EXTENSION_TARGET = 0;
    @Override
    public void setup() {
        ActionQueue queue = new ActionQueue();
        queue.submitAction(new InstantAction() {
            @Override
            public void update() {
                hardware.getTurretSystem().setScoutAlliance(ScoutSystem.SCOUT_ALLIANCE.BLUE);
                hardware.getTurretSystem().setScoutFieldTarget(ScoutSystem.SCOUT_TARGET.ALLIANCE_HIGH);
                hardware.getTurretSystem().setScoutTarget(ScoutSystem.SCOUT_STATE.PRELOAD_ANGLE);
            }
        });
        queue.submitAction(new DelayAction(5000));
        queue.submitAction(new InstantAction() {
            @Override
            public void update() {
                hardware.getTurretSystem().setScoutTarget(ScoutSystem.SCOUT_STATE.HOME_IN_INTAKE);
            }
        });
        OpmodeStatus.bindOnStart(queue);
    }
}
