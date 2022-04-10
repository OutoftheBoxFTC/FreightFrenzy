package Opmodes.TestOpmodes;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import Opmodes.BasicOpmode;
import State.Action.Action;
import State.Action.ActionController;
import State.Action.InstantAction;
import Utils.OpmodeStatus;
import Utils.ProgramClock;

@Config
@TeleOp
public class CameraDuckTester extends BasicOpmode {
    public static double PAN = 0.5;
    public static double TILT = 0.8;
    @Override
    public void setup() {
        ActionController.addAction(new InstantAction() {
            @Override
            public void update() {
                hardware.getIntakeSystem().getPanServo().setPosition(PAN);
                hardware.getIntakeSystem().getCameraServo().setPosition(TILT);
                hardware.getIntakeSystem().setPower(0.01);
            }
        });

        OpmodeStatus.bindOnStart(new Action() {
            @Override
            public void update() {
                if(hardware.getIntakeSystem().getCamera().getDuckPipeline().getPosition() < 50){
                    PAN -= (ProgramClock.getFrameTimeSeconds() * 60) / 270;
                }
                hardware.getIntakeSystem().getCamera().switchDuck();
                hardware.getIntakeSystem().getPanServo().setPosition(PAN);
            }
        });
    }
}
