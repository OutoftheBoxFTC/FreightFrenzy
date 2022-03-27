package Opmodes.TestOpmodes;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.configuration.LynxConstants;
import com.qualcomm.robotcore.util.RobotLog;

import java.util.List;

import Hardware.SmartDevices.SmartLynxModule.SmartLynxModule;
import Hardware.SmartDevices.SmartServo.SmartServo;
import State.Action.ActionController;
import State.Action.StandardActions.ServoProfileAction;

@Config
@TeleOp
public class ProfiledServoTestOpmode extends LinearOpMode {
    public static int PORT_SERVO = 0;
    public static double POSITION = 0, vel = 0.1, accel = 0.5;
    private double prevPos;
    public static boolean CHUB = false;
    private SmartLynxModule smartChub;
    private SmartLynxModule smartEhub;

    @Override
    public void runOpMode() throws InterruptedException {
        List<LynxModule> modules = hardwareMap.getAll(LynxModule.class);
        for(LynxModule lynx : modules){
            lynx.setBulkCachingMode(LynxModule.BulkCachingMode.AUTO);
            if(lynx.isParent() && LynxConstants.isEmbeddedSerialNumber(lynx.getSerialNumber())){
                smartChub = new SmartLynxModule(lynx, hardwareMap);
            }else{
                smartEhub = new SmartLynxModule(lynx, hardwareMap);
            }
        }
        SmartServo motor = (CHUB ? smartChub.getServo(PORT_SERVO) : smartEhub.getServo(PORT_SERVO));
        waitForStart();
        prevPos = POSITION;
        motor.setPosition(POSITION);
        while(opModeIsActive()) {
            if(POSITION != prevPos){
                RobotLog.i("Starting Profiled Move");
                ActionController.addAction(new ServoProfileAction(motor, vel, accel, POSITION));
            }
            prevPos = POSITION;
            ActionController.update();
        }
    }
}
