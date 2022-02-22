package Opmodes.TestOpmodes;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;

import Hardware.Pipelines.LineFinderPipeline;
import Opmodes.BasicOpmode;
import State.Action.Action;
import Utils.OpmodeStatus;

@TeleOp
@Config
public class CameraTester extends BasicOpmode {
    public static double CAMERA_ANGLE = 0;
    @Override
    public void setup() {
        OpmodeStatus.bindOnStart(() -> hardware.getIntakeSystem().getCameraServo().setPosition(CAMERA_ANGLE));
        WebcamName webcamName = hardwareMap.get(WebcamName.class, "lineCam");
        OpenCvCamera camera = OpenCvCameraFactory.getInstance().createWebcam(webcamName);
        camera.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener() {
            @Override
            public void onOpened() {
                camera.openCameraDevice();
                camera.setPipeline(new LineFinderPipeline());
                camera.startStreaming(320, 240, OpenCvCameraRotation.UPRIGHT);
            }

            @Override
            public void onError(int errorCode) {

            }
        });
        FtcDashboard.getInstance().startCameraStream(camera, 60);
    }
}
