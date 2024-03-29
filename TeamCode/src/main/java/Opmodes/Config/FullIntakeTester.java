package Opmodes.Config;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import Hardware.HardwareSystems.FFSystems.Actions.EnterIntakeAction;
import Hardware.HardwareSystems.FFSystems.Actions.LeaveIntakeAction;
import Opmodes.BasicOpmode;
import State.Action.Action;
import State.Action.ActionController;
import Utils.OpmodeStatus;

@TeleOp
@Config
public class FullIntakeTester extends BasicOpmode {
    public static double POWER = 0.3;
    boolean inIntake = true;
    @Override
    public void setup() {
        OpmodeStatus.bindOnStart(new Action() {
            boolean prev = false, started = false;
            long timer = System.currentTimeMillis();
            @Override
            public void update() {
                if(gamepad1.right_trigger < 0.2){
                    if(timer < System.currentTimeMillis()) {
                        if (!started) {
                            hardware.getIntakeSystem().setPower(POWER);
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
                                hardware.getIntakeSystem().setPower(POWER);
                            }
                        }
                    }else{
                        hardware.getIntakeSystem().setPower(0);
                    }
                }else if (gamepad1.right_trigger > 0.2){
                    if(!inIntake){
                        ActionController.addAction(new EnterIntakeAction(hardware));
                        inIntake = true;
                        hardware.getIntakeSystem().setPower(0);
                    }
                    if(hardware.getTurretSystem().isExtensionAtPos() && hardware.getTurretSystem().getExtensionPosition() < 10) {
                        started = false;
                        timer = System.currentTimeMillis() + 50;
                        hardware.getIntakeSystem().setPower(gamepad1.right_trigger);
                    }
                }
                prev = hardware.getIntakeSystem().getIntakeStop();
            }
        });
        OpmodeStatus.bindOnStart(() -> {
            FtcDashboard.getInstance().getTelemetry().addData("Power", gamepad1.right_trigger);
            FtcDashboard.getInstance().getTelemetry().update();
        });
    }
}
