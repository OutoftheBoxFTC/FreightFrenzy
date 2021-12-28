package Opmodes.Config;

import com.acmerobotics.dashboard.FtcDashboard;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import MathSystems.Angle;
import Opmodes.BasicOpmode;
import State.Action.Action;
import State.Action.ActionQueue;
import State.Action.StandardActions.DelayAction;
import Utils.OpmodeStatus;
@TeleOp
public class TurnAndExtendTest extends BasicOpmode {
    @Override
    public void setup() {
        OpmodeStatus.bindOnStart(new Action() {
            @Override
            public void update() {
                FtcDashboard.getInstance().getTelemetry().addData("Slide Pos", hardware.getTurretSystem().getExtensionPosition());
                FtcDashboard.getInstance().getTelemetry().update();
            }
        });

        ActionQueue queue = new ActionQueue();
        queue.submitAction(new Action() {
            @Override
            public void update() {
                hardware.getTurretSystem().moveExtensionRaw(840);
                hardware.getTurretSystem().moveTurretRaw(Angle.degrees(-35));
            }

            @Override
            public boolean shouldDeactivate() {
                return hardware.getTurretSystem().isExtensionAtPos() && hardware.getTurretSystem().isTurretAtPos();
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

        queue.submitAction(new DelayAction(2000));

        queue.submitAction(new Action() {
            @Override
            public void update() {
                hardware.getTurretSystem().moveExtensionRaw(100);
            }

            @Override
            public boolean shouldDeactivate() {
                return hardware.getTurretSystem().isExtensionAtPos();
            }
        });
        queue.submitAction(new Action() {
            @Override
            public void update() {
                hardware.getTurretSystem().moveTurretRaw(Angle.degrees(0));
            }

            @Override
            public boolean shouldDeactivate() {
                return hardware.getTurretSystem().isTurretAtPos();
            }
        });
        queue.submitAction(new Action() {
            @Override
            public void update() {
                hardware.getTurretSystem().moveExtensionRaw(0);
            }

            @Override
            public boolean shouldDeactivate() {
                return hardware.getTurretSystem().isExtensionAtPos();
            }
        });
        queue.submitAction(new DelayAction(500));
        queue.submitAction(new Action() {
            @Override
            public void update() {
                hardware.getTurretSystem().moveExtensionRaw(0);
            }

            @Override
            public boolean shouldDeactivate() {
                return hardware.getTurretSystem().isExtensionAtPos();
            }
        });

        OpmodeStatus.bindOnStart(queue);
    }
}
