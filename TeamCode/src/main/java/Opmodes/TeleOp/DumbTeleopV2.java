package Opmodes.TeleOp;

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
    @Override
    public void setup() {
        GamepadEx gamepad1Ex = new GamepadEx(gamepad1), gamepad2Ex = new GamepadEx(gamepad2);
        OpmodeStatus.bindOnStart(gamepad1Ex);
        OpmodeStatus.bindOnStart(gamepad2Ex);
        Vector3 dirVec = Vector3.ZERO();

        if(!OpmodeData.getInstance().isDataStale())
            hardware.getTurretSystem().tareExtensionMotor(OpmodeData.getInstance().getExtensionPos());

        OpmodeStatus.bindOnStart(() -> {
            dirVec.setA(gamepad1.left_stick_x);
            dirVec.setB(-gamepad1.left_stick_y);
            dirVec.setC(gamepad1.right_stick_x);
            hardware.getDrivetrainSystem().setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        });

        OpmodeStatus.bindOnStart(() -> hardware.getDrivetrainSystem().setPower(dirVec));

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
                if(gamepad1.x || gamepad1.dpad_left){
                    if(gamepad1.dpad_left){
                        moving = false;
                        inIntake = false;
                    }
                    if(!moving && !inIntake){
                        moving = true;
                        hardware.getTurretSystem().moveExtensionRaw(140);
                        hardware.getTurretSystem().moveTurretRaw(Angle.ZERO());
                        hardware.getTurretSystem().movePitchRaw(Angle.degrees(-6.5));
                        hardware.getTurretSystem().openArm();
                        ActionController.addAction(new Action() {
                            @Override
                            public void update() {
                                if(Math.abs(hardware.getTurretSystem().getTurretPosition().degrees()) < 5){
                                    hardware.getIntakeSystem().setPower(-1);
                                    ActionController.getInstance().terminateAction(this);
                                }
                            }
                        });
                        ActionController.addAction(new Action() {
                            @Override
                            public void update() {
                                if(Math.abs(hardware.getTurretSystem().getExtensionPosition()) < 160){
                                    hardware.getTurretSystem().getBucketServo().disableServo();
                                    //hardware.getTurretSystem().setBucketPosRaw(0.1);
                                    ActionController.getInstance().terminateAction(this);
                                    ActionQueue queue = new ActionQueue();
                                    queue.submitAction(new DelayAction(200));
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
                                if(Math.abs(hardware.getTurretSystem().getExtensionPosition()) < 5 || gamepad1.right_bumper){
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
                            }
                        });
                    }
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

                if((gamepad1Ex.right_trigger.pressed() || gamepad1Ex.left_trigger.pressed()) && !moving && inIntake){
                    moving = true;
                    hardware.getTurretSystem().moveExtensionRaw(110);
                    hardware.getIntakeSystem().setPower(1);
                    hardware.getTurretSystem().closeArm();
                    hardware.getTurretSystem().getBucketServo().disableServo();
                    intaking = false;
                    if(gamepad1Ex.right_trigger.pressed()) {
                        ActionController.addAction(new Action() {
                            @Override
                            public void update() {
                                if (Math.abs(hardware.getTurretSystem().getExtensionPosition()) > 90) {
                                    hardware.getTurretSystem().getBucketServo().enableServo();
                                    hardware.getTurretSystem().getBucketServo().setPosition(0.4);
                                    ActionQueue queue = new ActionQueue();
                                    queue.submitAction(new DelayAction(500));
                                    queue.submitAction(new InstantAction() {
                                        @Override
                                        public void update() {
                                            hardware.getTurretSystem().moveTurretRaw(Angle.degrees(-35));
                                            hardware.getIntakeSystem().setPower(0);
                                            hardware.getTurretSystem().moveExtensionRaw(250);
                                            hardware.getTurretSystem().setBucketPosRaw(0.9);
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

                if(gamepad1.b && !moving && !inIntake){
                    hardware.getTurretSystem().setBucketPosRaw(1);
                    hardware.getTurretSystem().moveExtensionRaw(510);
                    hardware.getTurretSystem().movePitchRaw(Angle.degrees(-14));
                }

                if(gamepad1.y && !moving && !inIntake){
                    hardware.getTurretSystem().setBucketPosRaw(1);
                    hardware.getTurretSystem().moveExtensionRaw(480);
                    hardware.getTurretSystem().movePitchRaw(Angle.degrees(-14));
                }

                if(gamepad1.a && !prevA && !moving && !inIntake){
                    ActionController.addAction(new DumpBucketAction(hardware, 1, 0.4));
                }

                prevA = gamepad1.a;
            }
        });

        gamepad2Ex.dpad.bindOnXChange(val -> {
            if(val == 0){
                hardware.getTurretSystem().setTurretPIDActive(false);
            }else {
                hardware.getTurretSystem().setTurretPIDActive(true);
                hardware.getTurretSystem().setTurretMotorPower(0.9 * val);
            }
        });

        gamepad2Ex.dpad.bindOnYChange(val -> {
            if(val == 0){
                hardware.getTurretSystem().setExPIDActive(false);
            }else {
                hardware.getTurretSystem().setExPIDActive(true);
                hardware.getTurretSystem().setExtensionMotorPower(0.9 * val);
            }
        });

        gamepad2Ex.right_bumper.bindOnPress(() -> hardware.getDuckSystem().setDuckPower(0.6));
        gamepad2Ex.right_bumper.bindOnRelease(() -> hardware.getDuckSystem().setDuckPower(0));

        gamepad2Ex.right_bumper.bindOnPress(() -> hardware.getDuckSystem().setDuckPower(0.2));
        gamepad2Ex.right_bumper.bindOnRelease(() -> hardware.getDuckSystem().setDuckPower(0));

        gamepad2Ex.right_trigger.bindOnValueChange(hardware.getDuckSystem()::setDuckPower);
    }
}
