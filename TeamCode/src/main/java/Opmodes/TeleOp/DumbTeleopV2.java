package Opmodes.TeleOp;

import com.acmerobotics.dashboard.FtcDashboard;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

import Hardware.HardwareSystems.FFSystems.Actions.DumpBucketAction;
import MathSystems.Angle;
import MathSystems.Vector.Vector3;
import Opmodes.BasicOpmode;
import State.Action.Action;
import State.Action.ActionController;
import State.Action.ActionQueue;
import State.Action.InstantAction;
import State.Action.StandardActions.DelayAction;
import Utils.GamepadEx.GamepadCallback;
import Utils.GamepadEx.GamepadEx;
import Utils.GamepadEx.GamepadValueCallback;
import Utils.OpmodeData;
import Utils.OpmodeStatus;
@TeleOp
public class DumbTeleopV2 extends BasicOpmode {
    boolean slowmode = false;
    boolean retractLocked = false;
    double offset = 0;
    @Override
    public void setup() {
        GamepadEx gamepad1Ex = new GamepadEx(gamepad1), gamepad2Ex = new GamepadEx(gamepad2);
        OpmodeStatus.bindOnStart(gamepad1Ex);
        OpmodeStatus.bindOnStart(gamepad2Ex);
        Vector3 dirVec = Vector3.ZERO();

        //ctionController.addAction(() -> slowmode = gamepad1Ex.dpad_down.toggled());

        if(!OpmodeData.getInstance().isDataStale())
            hardware.getTurretSystem().tareExtensionMotor(OpmodeData.getInstance().getExtensionPos());

        OpmodeStatus.bindOnStart(new Action() {
            @Override
            public void update() {
                FtcDashboard.getInstance().getTelemetry().addData("Turret Position", hardware.getTurretSystem().getTurretPosition().degrees());
                FtcDashboard.getInstance().getTelemetry().addData("Turret Target", hardware.getTurretSystem().getTurretTarget().degrees());
                FtcDashboard.getInstance().getTelemetry().addData("Turret Voltage", hardware.getTurretSystem().getTurretPotentiometer().getVoltage());
                FtcDashboard.getInstance().getTelemetry().update();
            }
        });

        OpmodeStatus.bindOnStart(() -> {
            dirVec.setA(gamepad1.left_stick_x);
            dirVec.setB(-gamepad1.left_stick_y);
            dirVec.setC(gamepad1.right_stick_x);
            hardware.getDrivetrainSystem().setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        });

        OpmodeStatus.bindOnStart(() -> hardware.getDrivetrainSystem().setPower(dirVec.scale(slowmode ? 0.7 : 1)));

        //INTAKE LOGIC
        OpmodeStatus.bindOnStart(new InstantAction() {
            @Override
            public void update() {
                hardware.getTurretSystem().getBucketServo().disableServo();
            }
        });
        OpmodeStatus.bindOnStart(new Action() {
            boolean moving = false, intaking = false;

            boolean inIntake = true, prevA = false;
            @Override
            public void update() {
                offset = gamepad2Ex.a.toggled() ? 3 : 0;
                if(gamepad1Ex.x.justPressed() || gamepad1Ex.dpad_left.justPressed()){
                    ActionQueue queue1 = new ActionQueue();
                    queue1.submitAction(new Action() {
                        @Override
                        public void update() {

                        }

                        @Override
                        public boolean shouldDeactivate() {
                            return !retractLocked;
                        }
                    });
                    queue1.submitAction(new InstantAction() {
                        @Override
                        public void update() {
                            hardware.getTurretSystem().setBucketPosRaw(0.4);
                            if(gamepad1.dpad_left){
                                moving = false;
                                inIntake = false;
                            }
                            if(!moving && !inIntake) {
                                moving = true;
                                hardware.getTurretSystem().moveExtensionRaw(70);
                                hardware.getTurretSystem().moveTurretRaw(Angle.degrees(0.01));
                                hardware.getTurretSystem().movePitchRaw(Angle.degrees(-6.5));
                                hardware.getTurretSystem().openArm();
                                hardware.getIntakeSystem().setPower(-1);
                                ActionController.addAction(new Action() {
                                    @Override
                                    public void update() {
                                        if (Math.abs(hardware.getTurretSystem().getExtensionPosition()) < 80 &&
                                                hardware.getTurretSystem().isTurretAtPos()) {
                                            //hardware.getTurretSystem().setBucketPosRaw(0.1);
                                            ActionController.getInstance().terminateAction(this);
                                            ActionQueue queue = new ActionQueue();
                                            queue.submitAction(new DelayAction(150));
                                            queue.submitAction(new Action() {
                                                @Override
                                                public void update() {

                                                }

                                                @Override
                                                public boolean shouldDeactivate() {
                                                    return hardware.getTurretSystem().isTurretAtPos();
                                                }
                                            });
                                            queue.submitAction(new InstantAction() {
                                                @Override
                                                public void update() {
                                                    hardware.getTurretSystem().getBucketServo().disableServo();
                                                }
                                            });
                                            queue.submitAction(new InstantAction() {
                                                @Override
                                                public void update() {
                                                    hardware.getTurretSystem().moveExtensionRaw(0);
                                                }
                                            });
                                            ActionController.addAction(queue);
                                        }
                                    }
                                });
                                ActionController.addAction(new Action() {
                                    @Override
                                    public void update() {
                                        if (Math.abs(hardware.getTurretSystem().getExtensionPosition()) < 5 || gamepad1.right_bumper) {
                                            ActionQueue queue = new ActionQueue();
                                            queue.submitAction(new Action() {
                                                long timer = 0;

                                                @Override
                                                public void initialize() {
                                                    timer = System.currentTimeMillis();
                                                }

                                                @Override
                                                public void update() {

                                                }

                                                @Override
                                                public boolean shouldDeactivate() {
                                                    return (System.currentTimeMillis() - timer) > 1000 || gamepad1.right_bumper;
                                                }
                                            });
                                            queue.submitAction(new InstantAction() {
                                                @Override
                                                public void update() {
                                                    hardware.getIntakeSystem().setPower(1);
                                                    hardware.getTurretSystem().setBucketPosRaw(0.1);
                                                    moving = false;
                                                    inIntake = true;
                                                    intaking = true;
                                                }
                                            });
                                            ActionController.addAction(queue);
                                            ActionController.getInstance().terminateAction(this);
                                        }
                                        if (gamepad1Ex.a.justPressed()) {
                                            hardware.getTurretSystem().moveExtensionRaw(250);
                                            hardware.getTurretSystem().setBucketPosRaw(0.4);
                                        }
                                        if (gamepad1Ex.a.released()) {
                                            hardware.getTurretSystem().moveExtensionRaw(0);
                                            ActionController.addAction(new Action() {
                                                @Override
                                                public void update() {
                                                    if (hardware.getTurretSystem().getExtensionPosition() < 70) {
                                                        hardware.getTurretSystem().getBucketServo().disableServo();
                                                        ActionController.getInstance().terminateAction(this);
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        }
                    });
                    ActionController.addAction(queue1);
                }

                if(inIntake && !moving) {
                    if(gamepad1Ex.right_bumper.justPressed()){
                        intaking = !intaking;
                    }
                    if (gamepad1.left_bumper) {
                        hardware.getIntakeSystem().setPower(-1);
                    } else {
                        hardware.getIntakeSystem().setPower(intaking ? 1 : 0);
                    }
                }

                if((gamepad1Ex.right_trigger.pressed() || gamepad1Ex.left_trigger.pressed() || hardware.getIntakeSystem().inIntake()) && !moving && inIntake){
                    moving = true;
                    ActionController.addAction(hardware.getIntakeSystem().getOuttakeAction(hardware));
                    intaking = false;
                    if(gamepad1Ex.right_trigger.pressed() || hardware.getIntakeSystem().inIntake()) {
                        ActionController.addAction(new Action() {
                            @Override
                            public void update() {
                                if (Math.abs(hardware.getTurretSystem().getExtensionPosition()) > 130) {
                                    hardware.getTurretSystem().getBucketServo().enableServo();
                                    hardware.getTurretSystem().getBucketServo().setPosition(0.4);
                                    hardware.getTurretSystem().moveExtensionRaw(350);
                                    ActionQueue queue = new ActionQueue();
                                    queue.submitAction(new DelayAction(100));
                                    queue.submitAction(new InstantAction() {
                                        @Override
                                        public void update() {
                                            hardware.getTurretSystem().moveTurretRaw(Angle.degrees(-35));
                                            hardware.getIntakeSystem().setPower(0);
                                            hardware.getTurretSystem().setBucketPosRaw(0.9);
                                            hardware.getTurretSystem().movePitchRaw(Angle.degrees(-(30 + offset)));
                                            moving = false;
                                        }
                                    });
                                    ActionController.addAction(queue);
                                    inIntake = false;
                                    ActionController.getInstance().terminateAction(this);
                                }
                            }
                        });
                    }
                }

                telemetry.addData("Extension Position", hardware.getTurretSystem().getExtensionPosition());
                telemetry.addData("Turret Position", hardware.getTurretSystem().getTurretPosition().degrees());
                telemetry.addData("Offset", offset);

                if(gamepad1.b && !moving && !inIntake){
                    hardware.getTurretSystem().setBucketPosRaw(1);
                    hardware.getTurretSystem().moveExtensionRaw(580);
                    hardware.getTurretSystem().movePitchRaw(Angle.degrees(-(30 + offset)));
                }

                if(gamepad1.y && !moving && !inIntake){
                    //hardware.getTurretSystem().setBucketPosRaw(1);
                    //hardware.getTurretSystem().moveExtensionRaw(480);
                    //hardware.getTurretSystem().movePitchRaw(Angle.degrees(-(17 + offset)));
                }

                if(gamepad1Ex.y.justPressed() && !moving && !inIntake){
                    ActionController.addAction(new DumpBucketAction(hardware, 1, 0.4));
                    retractLocked = true;
                    ActionQueue queue = new ActionQueue();
                    queue.submitAction(new DelayAction(600));
                    queue.submitAction(new InstantAction() {
                        @Override
                        public void update() {
                            retractLocked = false;
                        }
                    });
                    ActionController.addAction(queue);
                }

                prevA = gamepad1.a;
            }
        });

        gamepad2Ex.dpad.bindOnXChange(val -> {

        });

        ActionController.addAction(() -> {
            double val = gamepad1Ex.dpad.getX();
            if(val == 0){
                hardware.getTurretSystem().setTurretPIDActive(true);
            }else {
                hardware.getTurretSystem().setTurretPIDActive(false);
                hardware.getTurretSystem().moveTurretRaw(hardware.getTurretSystem().getTurretPosition());
                hardware.getTurretSystem().setTurretMotorPower(1 * val);
            }
        });

        ActionController.addAction(() -> {
            double val = gamepad1Ex.dpad.getY();
            if(val == 0){
                hardware.getTurretSystem().setExPIDActive(true);
            }else {
                hardware.getTurretSystem().setExPIDActive(false);
                hardware.getTurretSystem().moveExtensionRaw(hardware.getTurretSystem().getExtensionPosition());
                if(val < 0){
                    val = val * 0.6;
                }
                hardware.getTurretSystem().setExtensionMotorPower(-1 * val);
            }
        });

        gamepad2Ex.right_bumper.bindOnPress(() -> hardware.getDuckSystem().setDuckPower(0.8));
        gamepad2Ex.right_bumper.bindOnRelease(() -> hardware.getDuckSystem().setDuckPower(0));

        gamepad2Ex.left_bumper.bindOnPress(() -> hardware.getDuckSystem().setDuckPower(0.2));
        gamepad2Ex.left_bumper.bindOnRelease(() -> hardware.getDuckSystem().setDuckPower(0));

        gamepad2Ex.right_trigger.bindOnValueChange(hardware.getDuckSystem()::setDuckPower);
    }
}
