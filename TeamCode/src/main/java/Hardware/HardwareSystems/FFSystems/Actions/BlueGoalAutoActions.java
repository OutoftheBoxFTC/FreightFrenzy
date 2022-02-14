package Hardware.HardwareSystems.FFSystems.Actions;

import com.qualcomm.robotcore.hardware.DcMotor;

import org.checkerframework.checker.units.qual.A;

import Hardware.FFHardwareController;
import MathSystems.Angle;
import State.Action.Action;
import State.Action.ActionController;
import State.Action.ActionQueue;
import State.Action.InstantAction;
import State.Action.StandardActions.DelayAction;

public class BlueGoalAutoActions {
    public static ActionQueue preloadToHighGoal(FFHardwareController hardware){
        ActionQueue queue = new ActionQueue();

        queue.submitAction(new Action() {
            @Override
            public void update() {

            }

            @Override
            public boolean shouldDeactivate() {
                return hardware.getTurretSystem().isExtensionAtPos() && hardware.getTurretSystem().getExtensionTarget() == 150;
            }
        });

        queue.submitAction(new InstantAction() {
            @Override
            public void update() {
                hardware.getTurretSystem().moveTurretRaw(Angle.degrees(-38));
                hardware.getTurretSystem().moveExtensionRaw(400);
                hardware.getTurretSystem().movePitchRaw(Angle.degrees(-40));
            }
        });

        queue.submitAction(new DelayAction(250));

        queue.submitAction(new InstantAction() {
            @Override
            public void update() {
                hardware.getTurretSystem().setBucketPosRaw(0.9);
            }
        });

        return queue;
    }

    public static ActionQueue score(FFHardwareController hardware){
        ActionQueue queue = new ActionQueue();

        queue.submitAction(new Action() {
            @Override
            public void update() {
                hardware.getTurretSystem().moveTurretRaw(Angle.degrees(-40));
                hardware.getTurretSystem().movePitchRaw(Angle.degrees(-20));
            }

            @Override
            public boolean shouldDeactivate() {
                return hardware.getTurretSystem().isTurretAtPos();
            }
        });

        queue.submitAction(new InstantAction() {
            @Override
            public void update() {
                hardware.getTurretSystem().moveExtensionRaw(650);
                hardware.getTurretSystem().setBucketPosRaw(0.9);
            }
        });

        queue.submitAction(new Action() {
            @Override
            public void update() {

            }

            @Override
            public boolean shouldDeactivate() {
                return hardware.getTurretSystem().isExtensionAtPos();
            }
        });

        queue.submitAction(new InstantAction() {
            @Override
            public void update() {
                hardware.getTurretSystem().openArm();
            }
        });

        queue.submitAction(new DelayAction(75));

        queue.submitAction(new InstantAction() {
            @Override
            public void update() {
                hardware.getTurretSystem().setBucketPosRaw(0.4);
            }
        });

        queue.submitAction(new DelayAction(100));

        queue.submitAction(new InstantAction() {
            @Override
            public void update() {
                hardware.getTurretSystem().moveExtensionRaw(130);
                hardware.getTurretSystem().moveTurretRaw(Angle.ZERO());
                hardware.getTurretSystem().movePitchRaw(Angle.degrees(-6.5));
                hardware.getIntakeSystem().setPower(-1);
            }
        });

        queue.submitAction(new Action() {
            @Override
            public void update() {

            }

            @Override
            public boolean shouldDeactivate() {
                return hardware.getTurretSystem().isExtensionAtPos() && hardware.getTurretSystem().isTurretAtPos();
            }
        });

        return queue;
    }

    public static ActionQueue intoIntake(FFHardwareController hardware){
        ActionQueue queue = new ActionQueue();

        queue.submitAction(new InstantAction() {
            @Override
            public void update() {
                hardware.getTurretSystem().getBucketServo().disableServo();
            }
        });

        queue.submitAction(new DelayAction(50));

        queue.submitAction(new InstantAction() {
            @Override
            public void update() {
                hardware.getTurretSystem().moveExtensionRaw(0);
            }
        });

        queue.submitAction(new Action() {
            @Override
            public void update() {

            }

            @Override
            public boolean shouldDeactivate() {
                return hardware.getTurretSystem().isExtensionAtPos();
            }
        });

        queue.submitAction(new DelayAction(100));

        queue.submitAction(new InstantAction() {
            @Override
            public void update() {
                hardware.getTurretSystem().setBucketPosRaw(0.1);
            }
        });

        queue.submitAction(new DelayAction(300));

        queue.submitAction(new InstantAction() {
            @Override
            public void update() {
                ActionController.addAction(new Action() {
                    boolean deactive = false;
                    long timer = 0;
                    @Override
                    public void initialize() {
                        hardware.getIntakeSystem().setPower(1);
                        timer = System.currentTimeMillis() + 1000;
                    }

                    @Override
                    public void update() {
                        if(hardware.getIntakeSystem().inIntake()){
                            hardware.getIntakeSystem().setPower(-0.5);
                            hardware.getTurretSystem().closeArm();
                            deactive = true;
                        }
                    }

                    @Override
                    public boolean shouldDeactivate() {
                        return deactive && System.currentTimeMillis() > timer;
                    }
                });
            }
        });
        return queue;
    }

    public static ActionQueue initQueue(FFHardwareController hardware){
        ActionQueue initQueue = new ActionQueue();
        initQueue.submitAction(new InstantAction() {
            @Override
            public void update() {
                hardware.getTurretSystem().closeArm();
            }
        });
        initQueue.submitAction(new LeaveIntakeAction(hardware));
        initQueue.submitAction(new DelayAction(1000));
        initQueue.submitAction(new Action() {
            @Override
            public void update() {
                hardware.getTurretSystem().moveExtensionRaw(250);
                hardware.getTurretSystem().closeArm();
            }

            @Override
            public boolean shouldDeactivate() {
                return hardware.getTurretSystem().isExtensionAtPos();
            }
        });
        initQueue.submitAction(new InstantAction() {
            @Override
            public void update() {
                hardware.getTurretSystem().setBucketPosRaw(1);
            }
        });
        initQueue.submitAction(new DelayAction(500));
        initQueue.submitAction(new InstantAction() {
            @Override
            public void update() {
                hardware.getTurretSystem().moveExtensionRaw(0);
            }
        });
        initQueue.submitAction(new DelayAction(2000));
        initQueue.submitAction(new InstantAction() {
            @Override
            public void update() {
                hardware.getDrivetrainSystem().setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
                hardware.getTurretSystem().moveTurretRaw(Angle.degrees(-40));
                hardware.getTurretSystem().movePitchRaw(Angle.degrees(0));
            }
        });
        return initQueue;
    }
}
