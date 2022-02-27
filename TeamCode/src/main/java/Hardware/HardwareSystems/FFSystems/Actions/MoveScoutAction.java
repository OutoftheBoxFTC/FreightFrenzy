package Hardware.HardwareSystems.FFSystems.Actions;

import Hardware.HardwareSystems.FFSystems.ScoutSystem;
import State.Action.Action;

public class MoveScoutAction implements Action {
    private ScoutSystem.SCOUT_STATE target;
    private ScoutSystem system;
    private int numFrames = 0;

    public MoveScoutAction(ScoutSystem system, ScoutSystem.SCOUT_STATE target){
        this.system = system;
        this.target = target;
    }

    @Override
    public void initialize() {
        system.setScoutTarget(target);
    }

    @Override
    public void update() {
    }

    @Override
    public boolean shouldDeactivate() {
        if(system.getCurrentState() == target && system.isScoutIdle()){
            numFrames ++;
        }
        return numFrames > 3;
    }
}
