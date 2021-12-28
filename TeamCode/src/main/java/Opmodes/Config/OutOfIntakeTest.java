package Opmodes.Config;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import Opmodes.BasicOpmode;
import State.Action.Action;
import State.Action.ActionController;
import State.Action.ActionQueue;
import State.Action.InstantAction;
import State.Action.StandardActions.DelayAction;
import Utils.OpmodeStatus;
@TeleOp
public class OutOfIntakeTest extends BasicOpmode {
    @Override
    public void setup() {
        ActionController.addAction(new Action() {
            @Override
            public void update() {
                hardware.getTurretSystem().setBucketPosRaw(0.1);
            }

            @Override
            public boolean shouldDeactivate() {
                return isStarted();
            }
        });
        ActionQueue queue = new ActionQueue();
        queue.submitAction(new InstantAction(){
            @Override
            public void update() {
                hardware.getTurretSystem().setBucketPosRaw(0.4);
                hardware.getTurretSystem().moveExtensionRaw(125);
            }
        });
        queue.submitAction(new DelayAction(2000));
        queue.submitAction(new InstantAction(){
            @Override
            public void update() {
                hardware.getTurretSystem().setBucketPosRaw(0.1);
                hardware.getTurretSystem().moveExtensionRaw(0);
            }
        });
        OpmodeStatus.bindOnStart(queue);
    }
}
