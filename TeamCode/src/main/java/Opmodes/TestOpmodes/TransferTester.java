package Opmodes.TestOpmodes;

import com.acmerobotics.dashboard.FtcDashboard;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;

import Opmodes.BasicOpmode;
import State.Action.Action;
import State.Action.ActionController;
import State.Action.ActionQueue;
import State.Action.InstantAction;
import State.Action.StandardActions.DelayAction;
import Utils.OpmodeStatus;

public class TransferTester extends BasicOpmode {
    boolean transferring = false;
    @Override
    public void setup() {
        hardware.getIntakeSystem().transferFlipOut();
        OpmodeStatus.bindOnStart(() -> {
            if(!transferring) {
                hardware.getIntakeSystem().setPower(gamepad1.left_bumper ? -1 : gamepad1.right_bumper ? 1 : 0);
            }
            if((gamepad1.a || hardware.getIntakeSystem().getIntakeMotor().getMotor().getCurrent(CurrentUnit.AMPS) > 4.5)
                    && !transferring){
                transferring = true;
                ActionQueue queue = new ActionQueue();
                queue.submitAction(new InstantAction() {
                    @Override
                    public void update() {
                        hardware.getIntakeSystem().setPower(0.1);
                        hardware.getIntakeSystem().transferFlipIn();
                    }
                });
                queue.submitAction(new DelayAction(100));
                queue.submitAction(new InstantAction() {
                    @Override
                    public void update() {
                        hardware.getIntakeSystem().setPower(0);
                    }
                });
                queue.submitAction(new DelayAction(300));
                queue.submitAction(new Action() {
                    @Override
                    public void update() {
                        hardware.getIntakeSystem().setPower(-1);
                    }

                    @Override
                    public boolean shouldDeactivate() {
                        return hardware.getIntakeSystem().itemInIntake();
                    }
                });
                queue.submitAction(new InstantAction() {
                    @Override
                    public void update() {
                        hardware.getIntakeSystem().transferFlipOut();
                        hardware.getIntakeSystem().setPower(0);
                        transferring = false;
                    }
                });
                ActionController.addAction(queue);
            }
        });

        OpmodeStatus.bindOnStart(new Action() {
            @Override
            public void update() {
                FtcDashboard.getInstance().getTelemetry().update();
            }
        });
    }
}
