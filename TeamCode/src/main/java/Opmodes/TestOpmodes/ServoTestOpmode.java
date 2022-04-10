package Opmodes.TestOpmodes;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.configuration.LynxConstants;

import java.util.List;

import Hardware.SmartDevices.SmartLynxModule.SmartLynxModule;
import Hardware.SmartDevices.SmartServo.SmartServo;

@Config
@TeleOp
public class ServoTestOpmode extends LinearOpMode {
    public static int PORT_SERVO = 0;
    public static double POSITION = 0;
    public static boolean CHUB = false, REVERSE = false;
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
        if(REVERSE){
            motor.getServo().setDirection(Servo.Direction.REVERSE);
        }
        waitForStart();
        while(opModeIsActive())
            motor.setPosition(POSITION);
    }
}
