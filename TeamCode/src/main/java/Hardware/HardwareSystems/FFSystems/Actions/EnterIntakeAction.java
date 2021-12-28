package Hardware.HardwareSystems.FFSystems.Actions;

import Hardware.FFHardwareController;
import State.Action.InstantAction;

public class EnterIntakeAction extends InstantAction {
    private FFHardwareController hardware;

    public EnterIntakeAction(FFHardwareController hardware){
        this.hardware = hardware;
    }

    @Override
    public void update() {
        hardware.getTurretSystem().setBucketPosRaw(0.1);
        hardware.getTurretSystem().moveExtensionRaw(0);
    }
}
