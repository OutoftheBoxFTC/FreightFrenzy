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
import Utils.GamepadEx.GamepadEx;
import Utils.OpmodeStatus;
@TeleOp
public class DumbTeleopV2 extends BasicOpmode {
    @Override
    public void setup() {
        GamepadEx gamepad1Ex = new GamepadEx(gamepad1);
        OpmodeStatus.bindOnStart(gamepad1Ex);
        Vector3 dirVec = Vector3.ZERO();

        OpmodeStatus.bindOnStart(() -> {
            dirVec.setA(gamepad1.left_stick_x);
            dirVec.setB(-gamepad1.left_stick_y);
            dirVec.setC(gamepad1.right_stick_x);
            hardware.getDrivetrainSystem().setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            if(gamepad1.right_trigger < 0.1){
                dirVec.set(dirVec.scale(0.8));
            }
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
                if(gamepad1.x){
                    if(!moving && !inIntake){
                        moving = true;
                        hardware.getTurretSystem().moveExtensionRaw(140);
                        hardware.getTurretSystem().moveTurretRaw(Angle.ZERO());
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
                                    queue.submitAction(new DelayAction(500));
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
                                if(Math.abs(hardware.getTurretSystem().getExtensionPosition()) < 5){
                                    ActionQueue queue = new ActionQueue();
                                    queue.submitAction(new DelayAction(1000));
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

                if(gamepad1.right_trigger > 0.1 && !moving && inIntake){
                    moving = true;
                    hardware.getTurretSystem().moveExtensionRaw(110);
                    hardware.getIntakeSystem().setPower(1);
                    hardware.getTurretSystem().getBucketServo().disableServo();
                    intaking = false;
                    ActionController.addAction(new Action() {
                        @Override
                        public void update() {
                            if(Math.abs(hardware.getTurretSystem().getExtensionPosition()) > 90){
                                hardware.getTurretSystem().getBucketServo().enableServo();
                                hardware.getTurretSystem().getBucketServo().setPosition(0.4);
                                ActionQueue queue = new ActionQueue();
                                queue.submitAction(new DelayAction(500));
                                queue.submitAction(new InstantAction() {
                                    @Override
                                    public void update() {
                                        hardware.getTurretSystem().moveTurretRaw(Angle.degrees(-35));
                                        hardware.getIntakeSystem().setPower(0);
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

                telemetry.addData("Test", hardware.getTurretSystem().getExtensionPosition());

                if(gamepad1.b && !moving && !inIntake){
                    hardware.getTurretSystem().getBucketServo().setPosition(0.45);
                    hardware.getTurretSystem().moveExtensionRaw(510);
                }

                if(gamepad1.a && !prevA && !moving && !inIntake){
                    ActionController.addAction(new DumpBucketAction(hardware, 1, 0.4));
                }

                prevA = gamepad1.a;
            }
        });
    }
}
