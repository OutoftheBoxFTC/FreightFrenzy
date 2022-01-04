package Opmodes.TeleOp;

import com.acmerobotics.dashboard.FtcDashboard;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

import Hardware.HardwareSystems.FFSystems.Actions.BlueGoalActions;
import Hardware.HardwareSystems.FFSystems.Actions.EnterIntakeAction;
import Hardware.HardwareSystems.FFSystems.Actions.LeaveIntakeAction;
import MathSystems.Angle;
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
            hardware.getDrivetrainSystem().setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            if(gamepad1.right_trigger < 0.1){
                dirVec.set(dirVec.scale(0.8));
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
                //FtcDashboard.getInstance().getTelemetry().addData("Power", gamepad1.right_trigger);
                //FtcDashboard.getInstance().getTelemetry().update();
                telemetry.addData("Pos", hardware.getTurretSystem().getExtensionPosition());
                telemetry.addData("Pitch", hardware.getTurretSystem().getPitchPosition().degrees());
                if(gamepad2.a || true){
                    double yDist = 27.5;
                    double xDist = 45 - (hardware.getOdometrySystem().getLeftDist()+14);
                    telemetry.addData("2m", hardware.getOdometrySystem().getLeftDist());
                    telemetry.addData("Turret", -(90-Angle.radians(Math.atan2(yDist, xDist)).degrees()));
                    telemetry.addData("Turret Pos", hardware.getTurretSystem().getTurretPosition().degrees());
                    telemetry.addData("Pitch Pos", hardware.getTurretSystem().getPitchPosition().degrees());
                    telemetry.addData("Pitch Target", hardware.getTurretSystem().getPitchTarget().degrees());
                    telemetry.addData("IMU", hardware.getDrivetrainSystem().getImuAngle().degrees());
                    if(gamepad2.a) {
                        hardware.getTurretSystem().moveTurretRaw(Angle.degrees(-(90-Angle.radians(Math.atan2(yDist, xDist)).degrees())));
                    }
                }
                if(gamepad1.b && state != 0){
                    double yDist = 27.5;
                    double xDist = 45 - (hardware.getOdometrySystem().getLeftDist()+14);
                    double angle = -(90-Math.toDegrees(Math.atan2(yDist, xDist)));
                    double dist = Math.sqrt(yDist * yDist + xDist * xDist);
                    if(hardware.getOdometrySystem().getLeftDist() > 40){
                        angle = -37.5;
                        dist = 810;
                    }
                    extendAction = BlueGoalActions.getBlueAlliance(hardware, angle, dist+3);
                    ActionController.addAction(extendAction);
                    extending = true;
                    state = 0;
                }
                if(state == 0){
                    if(hardware.getTurretSystem().getExtensionPosition() > 250){
                        hardware.getTurretSystem().setBucketPosRaw(0.5);
                    }
                }
                if(gamepad1.a){
                    hardware.getTurretSystem().setBucketPosRaw(1);
                }
                if(gamepad1.y){
                    hardware.getTurretSystem().setBucketPosRaw(0.85);
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

        OpmodeStatus.bindOnStart(() -> hardware.getDuckSystem().setDuckPower(gamepad2.right_bumper ? 1 : gamepad2.left_bumper ? -1 : 0));
        //-6.5
    }
}
