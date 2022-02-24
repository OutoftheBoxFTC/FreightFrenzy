package Hardware.HardwareSystems.FFSystems.Actions;

import Hardware.HardwareSystems.FFSystems.ScoutSystem;
import State.Action.Action;

public class MoveScoutAction implements Action {
    private ScoutSystem.SCOUT_STATE target;
    private ScoutSystem system;

    public MoveScoutAction(ScoutSystem system, ScoutSystem.SCOUT_STATE target){
        this.system = system;
        this.target = target;
    }

    @Override
    public void update() {
        system.setScoutTarget(target);
    }

    @Override
    public boolean shouldDeactivate() {
        return system.getCurrentState() == target && system.isScoutIdle();
    }
}
