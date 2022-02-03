package Opmodes.Config;

import com.acmerobotics.dashboard.FtcDashboard;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import Opmodes.BasicOpmode;
import State.Action.Action;
import State.Action.ActionController;
import State.Action.ActionQueue;
import State.Action.InstantAction;
import State.Action.StandardActions.DelayAction;
import Utils.OpmodeStatus;

@TeleOp
public class IntakeTesting extends BasicOpmode {
    @Override
    public void setup() {

        OpmodeStatus.bindOnStart(new Action() {
            @Override
            public void update() {
                FtcDashboard.getInstance().getTelemetry().addData("Current", hardware.getIntakeSystem().getIntakeCurrent());
                FtcDashboard.getInstance().getTelemetry().update();
            }
        });

        OpmodeStatus.bindOnStart(new Action() {
            @Override
            public void update() {
                hardware.getIntakeSystem().setPower(1);
                hardware.getTurretSystem().openArm();
                if(hardware.getIntakeSystem().getIntakeCurrent() > 0.5){
                    //hardware.getIntakeSystem().setPower(0.6);
                }
            }

            @Override
            public boolean shouldDeactivate() {
                return hardware.getIntakeSystem().inIntake();
            }

            @Override
            public void onEnd() {
                ActionQueue queue = new ActionQueue();
                queue.submitAction(new InstantAction() {
                    @Override
                    public void update() {
                        hardware.getIntakeSystem().setPower(-1);
                        hardware.getTurretSystem().closeArm();
                        ActionController.addAction(new Action() {
                            long timer = 0;

                            @Override
                            public void initialize() {
                                timer = System.currentTimeMillis() + 50;
                            }

                            @Override
                            public void update() {
                                if(System.currentTimeMillis() > timer){
                                    hardware.getTurretSystem().openArm();
                                    ActionController.getInstance().terminateAction(this);
                                }
                            }
                        });
                    }
                });
                queue.submitAction(new Action() {
                    @Override
                    public void update() {

                    }

                    @Override
                    public boolean shouldDeactivate() {
                        return hardware.getIntakeSystem().getIntakeCurrent() < 0.5;
                    }
                });
                queue.submitAction(new InstantAction() {
                    @Override
                    public void update() {
                        hardware.getIntakeSystem().setPower(1);
                        hardware.getTurretSystem().openArm();
                        hardware.getTurretSystem().setBucketPosRaw(0.4);
                        hardware.getTurretSystem().moveExtensionRaw(150);
                    }
                });
                queue.submitAction(new DelayAction(75));
                queue.submitAction(new InstantAction() {
                    @Override
                    public void update() {
                        hardware.getTurretSystem().closeArm();
                    }
                });
                ActionController.addAction(queue);
            }
        });
    }
}
