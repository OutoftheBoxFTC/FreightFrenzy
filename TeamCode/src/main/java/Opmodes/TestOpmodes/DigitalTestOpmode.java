package Opmodes.TestOpmodes;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.DigitalChannelController;
import com.qualcomm.robotcore.hardware.configuration.LynxConstants;

import java.util.List;

import Hardware.SmartDevices.SmartLynxModule.SmartLynxModule;
import Hardware.SmartDevices.SmartServo.SmartServo;

@Config
public class DigitalTestOpmode extends LinearOpMode {
    public static int PORT_DIGITAL = 0;
    public static double POSITION = 0;
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
        DigitalChannel channel = (CHUB ? smartChub.getDigitalController(PORT_DIGITAL) : smartEhub.getDigitalController(PORT_DIGITAL));
        channel.setMode(DigitalChannel.Mode.INPUT);
        waitForStart();
        while(opModeIsActive()) {
            FtcDashboard.getInstance().getTelemetry().addData("Channel", channel.getState());
            FtcDashboard.getInstance().getTelemetry().update();
        }
    }
}
