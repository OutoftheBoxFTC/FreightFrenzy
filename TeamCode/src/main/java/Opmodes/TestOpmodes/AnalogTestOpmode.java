package Opmodes.TestOpmodes;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.configuration.LynxConstants;

import java.util.List;

import Hardware.SmartDevices.SmartAnalogInput.SmartAnalogInput;
import Hardware.SmartDevices.SmartLynxModule.SmartLynxModule;
import Hardware.SmartDevices.SmartServo.SmartServo;

@Config
@TeleOp
public class AnalogTestOpmode extends LinearOpMode {
    public static int PORT_ANALOG = 0;
    public static double RANGE = 360;
    public static double MAX_VOLTAGE = 5;
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
        SmartAnalogInput ai = (CHUB ? smartChub.getAnalogInput(PORT_ANALOG) : smartEhub.getAnalogInput(PORT_ANALOG));
        waitForStart();
        while (opModeIsActive()){
            FtcDashboard.getInstance().getTelemetry().addData("Voltage", ai.getVoltage());
            FtcDashboard.getInstance().getTelemetry().addData("Value", (ai.getVoltage()/MAX_VOLTAGE) * RANGE);
            FtcDashboard.getInstance().getTelemetry().update();
        }
    }
}
