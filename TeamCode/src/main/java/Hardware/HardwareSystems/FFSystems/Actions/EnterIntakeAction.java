package Hardware.HardwareSystems.FFSystems.Actions;

import Hardware.FFHardwareController;
import State.Action.ActionController;
import State.Action.ActionQueue;
import State.Action.InstantAction;
import State.Action.StandardActions.DelayAction;

public class EnterIntakeAction extends InstantAction {
    private FFHardwareController hardware;

    public EnterIntakeAction(FFHardwareController hardware){
        this.hardware = hardware;
    }

    @Override
    public void update() {
        hardware.getIntakeSystem().setPower(-1);
        hardware.getTurretSystem().getBucketServo().disableServo();
        ActionQueue queue = new ActionQueue();
        queue.submitAction(new DelayAction(75));
        queue.submitAction(new InstantAction() {
            @Override
            public void update() {
                hardware.getTurretSystem().moveExtensionRaw(0);
                hardware.getTurretSystem().openArm();
            }
        });
        ActionController.addAction(queue);
    }
}
