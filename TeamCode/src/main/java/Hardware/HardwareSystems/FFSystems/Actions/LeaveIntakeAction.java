package Hardware.HardwareSystems.FFSystems.Actions;

import Hardware.FFHardwareController;
import Hardware.HardwareSystems.HardwareSystem;
import State.Action.InstantAction;

public class LeaveIntakeAction extends InstantAction {
    private FFHardwareController hardware;

    public LeaveIntakeAction(FFHardwareController hardware){
        this.hardware = hardware;
    }

    @Override
    public void update() {
        hardware.getTurretSystem().setBucketPosRaw(0.45);
        hardware.getTurretSystem().moveExtensionRaw(105); //100
    }
}
