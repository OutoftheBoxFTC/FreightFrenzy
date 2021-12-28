package Hardware.HardwareSystems.FFSystems.Actions;

import Hardware.FFHardwareController;
import MathSystems.Angle;
import State.Action.Action;
import State.Action.ActionQueue;
import State.Action.InstantAction;
import State.Action.StandardActions.DelayAction;

public class BlueGoalActions {

    public static ActionQueue getBlueAlliance(FFHardwareController hardware){
        ActionQueue queue = new ActionQueue();
        queue.submitAction(new Action() {

            @Override
            public void update() {
                hardware.getTurretSystem().moveExtensionRaw(840);
                hardware.getTurretSystem().moveTurretRaw(Angle.degrees(-37.5));
                hardware.getTurretSystem().movePitchRaw(Angle.degrees(-10));
            }

            @Override
            public boolean shouldDeactivate() {
                return hardware.getTurretSystem().isExtensionAtPos();
            }
        });
        queue.submitAction(new Action() {
            @Override
            public void update() {
                hardware.getTurretSystem().moveExtensionRaw(840);
            }

            @Override
            public boolean shouldDeactivate() {
                return hardware.getTurretSystem().isExtensionAtPos();
            }
        });

        return queue;
    }

    public static ActionQueue getBlueAllianceReturn(FFHardwareController hardware){
        ActionQueue queue = new ActionQueue();
        queue.submitAction(new Action() {
            @Override
            public void update() {
                hardware.getTurretSystem().moveExtensionRaw(300);
                hardware.getTurretSystem().movePitchRaw(Angle.degrees(-6.5));
            }

            @Override
            public boolean shouldDeactivate() {
                return hardware.getTurretSystem().isExtensionAtPos();
            }
        });
        queue.submitAction(new DelayAction(50));
        queue.submitAction(new InstantAction() {
            @Override
            public void update() {
                hardware.getTurretSystem().moveExtensionRaw(125);
            }
        });
        queue.submitAction(new Action() {
            @Override
            public void update() {
                hardware.getTurretSystem().moveTurretRaw(Angle.degrees(0));
                hardware.getTurretSystem().moveExtensionRaw(125);
            }

            @Override
            public boolean shouldDeactivate() {
                return hardware.getTurretSystem().isTurretAtPos();
            }
        });
        return queue;
    }
}
