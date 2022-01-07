package Hardware.HardwareSystems.FFSystems.Actions;

import Hardware.FFHardwareController;
import MathSystems.Angle;
import State.Action.Action;
import State.Action.ActionQueue;
import State.Action.InstantAction;
import State.Action.StandardActions.DelayAction;

public class BlueGoalActions {

    public static ActionQueue getBlueAlliance(FFHardwareController hardware, double angle, double distance){
        ActionQueue queue = new ActionQueue();
        queue.submitAction(new Action() {

            @Override
            public void initialize() {
                hardware.getTurretSystem().moveExtensionRaw(distance * 14.9817342);/// 0.0494500688
                hardware.getTurretSystem().moveTurretRaw(Angle.degrees(angle));//-37.5
                hardware.getTurretSystem().movePitchRaw(Angle.degrees(-19));
            }

            @Override
            public void update() {

            }

            @Override
            public boolean shouldDeactivate() {
                return hardware.getTurretSystem().isExtensionAtPos();
            }
        });
        queue.submitAction(new Action() {
            @Override
            public void initialize() {
                hardware.getTurretSystem().moveExtensionRaw(distance* 14.9817342);
            }

            @Override
            public void update() {
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
            public void initialize() {
                hardware.getTurretSystem().moveExtensionRaw(105);
            } //300

            @Override
            public void update() {

            }

            @Override
            public boolean shouldDeactivate() {
                return hardware.getTurretSystem().isExtensionAtPos();
            }
        });
        queue.submitAction(new DelayAction(50));
        queue.submitAction(new InstantAction() {
            @Override
            public void initialize() {
                hardware.getTurretSystem().movePitchRaw(Angle.degrees(-6.5)); //-6.18
                hardware.getTurretSystem().moveExtensionRaw(105); //105
            }

            @Override
            public void update() {

            }
        });
        queue.submitAction(new Action() {
            @Override
            public void initialize() {
                hardware.getTurretSystem().moveTurretRaw(Angle.degrees(0));
                hardware.getTurretSystem().moveExtensionRaw(105);
            }

            @Override
            public void update() {

            }

            @Override
            public boolean shouldDeactivate() {
                return true;
            }
        });
        return queue;
    }

    public static ActionQueue getBlueAllianceReturnAuto(FFHardwareController hardware){
        ActionQueue queue = new ActionQueue();
        queue.submitAction(new Action() {
            @Override
            public void initialize() {
                hardware.getTurretSystem().moveExtensionRaw(250);
                hardware.getTurretSystem().movePitchRaw(Angle.degrees(-5)); //-6.5
            }

            @Override
            public void update() {

            }

            @Override
            public boolean shouldDeactivate() {
                return hardware.getTurretSystem().isExtensionAtPos();
            }
        });
        queue.submitAction(new DelayAction(50));
        queue.submitAction(new InstantAction() {
            @Override
            public void initialize() {
                hardware.getTurretSystem().moveExtensionRaw(105);
            }

            @Override
            public void update() {

            }
        });
        queue.submitAction(new Action() {
            @Override
            public void initialize() {
                hardware.getTurretSystem().moveTurretRaw(Angle.degrees(0));
                hardware.getTurretSystem().moveExtensionRaw(105);
            }

            @Override
            public void update() {

            }

            @Override
            public boolean shouldDeactivate() {
                return true;
            }
        });
        return queue;
    }
}
