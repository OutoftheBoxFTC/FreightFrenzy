package Opmodes.Config;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import Opmodes.BasicOpmode;
import State.Action.Action;
import Utils.OpmodeStatus;
import Vision.ApriltagDetector;
@TeleOp
public class VisionTesting extends BasicOpmode {
    @Override
    public void setup() {
        ApriltagDetector detector = new ApriltagDetector(hardwareMap);
        OpmodeStatus.bindOnStart(detector);
        OpmodeStatus.bindOnStart(() -> {
            telemetry.addData("X", detector.getLastPoint().x);
            telemetry.addData("Y", detector.getLastPoint().y);
            String position = "";
            if(detector.getLastPoint().x > 1200){
                position = "LEFT";
            }else if( detector.getLastPoint().x > 1000){
                position = "CENTRE";
            }else{
                position = "RIGHT";
            }
            telemetry.addData("Position", position);
        });
    }
}
