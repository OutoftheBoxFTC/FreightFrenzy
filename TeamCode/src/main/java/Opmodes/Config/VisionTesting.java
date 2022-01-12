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
        OpmodeStatus.bindOnStart(() -> {
            telemetry.addData("X", detector.getLastPoint().x);
            telemetry.addData("Y", detector.getLastPoint().y);
        });
    }
}
