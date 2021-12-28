package Opmodes.TeleOp;

import com.acmerobotics.dashboard.FtcDashboard;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import Hardware.HardwareSystems.FFSystems.Actions.BlueGoalActions;
import Hardware.HardwareSystems.FFSystems.Actions.EnterIntakeAction;
import Hardware.HardwareSystems.FFSystems.Actions.LeaveIntakeAction;
import MathSystems.MathUtils;
import MathSystems.Vector.Vector3;
import Opmodes.BasicOpmode;
import State.Action.Action;
import State.Action.ActionController;
import State.Action.ActionQueue;
import State.Action.InstantAction;
import Utils.GamepadEx.GamepadEx;
import Utils.OpmodeStatus;
@TeleOp
public class DumbTeleop extends BasicOpmode {
    boolean inIntake = true;
    boolean extending = false;
    boolean prevLeft = false;
    double angle = 0;
    @Override
    public void setup() {
        GamepadEx gamepad1Ex = new GamepadEx(gamepad1);
        Vector3 dirVec = Vector3.ZERO();

        OpmodeStatus.bindOnStart(() -> {
            dirVec.setA(gamepad1.left_stick_x);
            dirVec.setB(-gamepad1.left_stick_y);
            dirVec.setC(gamepad1.right_stick_x);
            if(gamepad1.left_trigger > 0.2 && !prevLeft){
                angle = hardware.getDrivetrainSystem().getImuAngle().degrees();
            }
            if(gamepad1.left_trigger > 0.2){
                double err = angle - hardware.getDrivetrainSystem().getImuAngle().degrees();
                if(Math.abs(err) > 2.5) {
                    dirVec.scale(0.75);
                    dirVec.setC(MathUtils.sign(err) * -0.3);
                }
            }
            prevLeft = gamepad1.left_trigger > 0.2;
        });

        OpmodeStatus.bindOnStart(() -> hardware.getDrivetrainSystem().setPower(dirVec));

        OpmodeStatus.bindOnStart(new Action() {
            boolean prev = false, started = false;
            long timer = System.currentTimeMillis();
            @Override
            public void update() {
                if(extending){
                    return;
                }
                if(!(gamepad1.right_bumper || gamepad1.left_bumper)){
                    if(timer < System.currentTimeMillis()) {
                        if (!started) {
                            hardware.getIntakeSystem().setPower(0.3);
                            if (!hardware.getIntakeSystem().getIntakeStop()) {
                                started = true;
                            }
                        } else {
                            if (!prev && hardware.getIntakeSystem().getIntakeStop()) {
                                hardware.getIntakeSystem().setPower(0);
                                if(inIntake){
                                    ActionController.addAction(new LeaveIntakeAction(hardware));
                                    inIntake = false;
                                }
                            } else if (!hardware.getIntakeSystem().getIntakeStop()) {
                                hardware.getIntakeSystem().setPower(0.3);
                            }
                        }
                    }else{
                        hardware.getIntakeSystem().setPower(0);
                    }
                }else{
                    if(!inIntake){
                        ActionController.addAction(new EnterIntakeAction(hardware));
                        inIntake = true;
                        hardware.getIntakeSystem().setPower(0);
                    }
                    if(hardware.getTurretSystem().isExtensionAtPos() && hardware.getTurretSystem().getExtensionPosition() < 30) {
                        started = false;
                        timer = System.currentTimeMillis() + 50;
                        hardware.getIntakeSystem().setPower(gamepad1.right_bumper ? 1 : gamepad1.left_bumper ? -1 : 0);
                    }
                }
                prev = hardware.getIntakeSystem().getIntakeStop();
            }
        });
        OpmodeStatus.bindOnStart(new Action() {
            int state = -1;
            ActionQueue extendAction = null;
            @Override
            public void update() {
                //hardware.getTurretSystem().setPitchMotorPower(-6.5);
                FtcDashboard.getInstance().getTelemetry().addData("Power", gamepad1.right_trigger);
                FtcDashboard.getInstance().getTelemetry().update();
                telemetry.addData("Pos", hardware.getTurretSystem().getExtensionPosition());
                telemetry.addData("Turret", hardware.getTurretSystem().getTurretPosition().degrees());
                if(gamepad1.b && state != 0){
                    extendAction = BlueGoalActions.getBlueAlliance(hardware);
                    ActionController.addAction(extendAction);
                    extending = true;
                    state = 0;
                }
                if(gamepad1.a){
                    hardware.getTurretSystem().setBucketPosRaw(1);
                }
                if(gamepad1.x && state != 1){
                    hardware.getTurretSystem().setBucketPosRaw(0.4);
                    if(extendAction != null) {
                        ActionController.getInstance().terminateAction(extendAction);
                    }
                    ActionQueue queue = BlueGoalActions.getBlueAllianceReturn(hardware);
                    queue.submitAction(new InstantAction() {
                        @Override
                        public void update() {
                            extending = false;
                        }
                    });
                    ActionController.addAction(queue);
                    state = 1;
                }
            }
        });
        //-6.5
    }
}
