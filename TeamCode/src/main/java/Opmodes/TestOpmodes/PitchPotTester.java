package Opmodes.TestOpmodes;

import com.acmerobotics.dashboard.FtcDashboard;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import Opmodes.BasicOpmode;
import State.Action.Action;
import State.Action.ActionQueue;
import State.Action.InstantAction;
import State.Action.StandardActions.DelayAction;
import Utils.OpmodeStatus;

@TeleOp
public class PitchPotTester extends BasicOpmode {
    double startVoltage = 0, factor = 0;
    @Override
    public void setup() {
        OpmodeStatus.bindOnStart(() -> {
            FtcDashboard.getInstance().getTelemetry().addData("Voltage", hardware.getTurretSystem().getPitchPot().getValue());
            FtcDashboard.getInstance().getTelemetry().addData("Factor", factor);
            FtcDashboard.getInstance().getTelemetry().addData("Start", startVoltage);
            FtcDashboard.getInstance().getTelemetry().addData("Pos", hardware.getTurretSystem().getPitchMotorPos());
            FtcDashboard.getInstance().getTelemetry().addData("Angle", hardware.getTurretSystem().getPitchMotorPos() / 18.189);
            FtcDashboard.getInstance().getTelemetry().update();
        });

        ActionQueue queue = new ActionQueue();
        queue.submitAction(new InstantAction() {
            @Override
            public void update() {
                startVoltage = hardware.getTurretSystem().getPitchPot().getValue();
                hardware.getTurretSystem().setPitchMotorPower(1);
            }
        });
        queue.submitAction(new DelayAction(800));
        queue.submitAction(new InstantAction() {
            @Override
            public void update() {
                hardware.getTurretSystem().setPitchMotorPower(0);
            }
        });
        queue.submitAction(new DelayAction(1000));
        queue.submitAction(new Action() {
            @Override
            public void update() {
                double val = hardware.getTurretSystem().getPitchPot().getValue() - startVoltage;
                double angle = hardware.getTurretSystem().getPitchMotorPos() / 18.189;
                factor = angle / val;
            }
        });

        OpmodeStatus.bindOnStart(queue);
    }
}
