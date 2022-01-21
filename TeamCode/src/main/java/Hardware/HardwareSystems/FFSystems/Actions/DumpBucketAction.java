package Hardware.HardwareSystems.FFSystems.Actions;

import Hardware.FFHardwareController;
import State.Action.Action;
import State.Action.ActionQueue;
import State.Action.InstantAction;
import State.Action.StandardActions.DelayAction;

public class DumpBucketAction extends ActionQueue {
    
    public DumpBucketAction(FFHardwareController hardware, double dumpPos, double returnPos){
        submitAction(new InstantAction() {
            @Override
            public void update() {
                hardware.getTurretSystem().openArm();
            }
        });
        submitAction(new DelayAction(400));
        submitAction(new InstantAction() {
            @Override
            public void update() {
                hardware.getTurretSystem().setBucketPosRaw(returnPos);
            }
        });
    }
}
