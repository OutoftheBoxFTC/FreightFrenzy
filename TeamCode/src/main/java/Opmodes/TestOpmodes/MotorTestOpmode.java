package Opmodes.TestOpmodes;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.configuration.LynxConstants;

import java.util.List;

import Hardware.SmartDevices.SmartLynxModule.SmartLynxModule;
import Hardware.SmartDevices.SmartMotor.SmartMotor;
import Hardware.SmartDevices.SmartServo.SmartServo;

@Config
@TeleOp
public class MotorTestOpmode extends LinearOpMode {
    public static int PORT_MOTOR = 0;
    public static double POWER = 0;
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
        SmartMotor motor = (CHUB ? smartChub.getMotor(PORT_MOTOR) : smartEhub.getMotor(PORT_MOTOR));
        waitForStart();
        while(opModeIsActive())
            motor.setPower(POWER);
    }
}
