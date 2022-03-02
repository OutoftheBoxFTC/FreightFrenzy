package Hardware.Pipelines;

import com.acmerobotics.dashboard.FtcDashboard;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;

import Hardware.FFHardwareController;

public class LineFinderCamera {
    private LineFinderPipeline pipeline;
    private boolean opened = false;

    public LineFinderCamera(HardwareMap hardwareMap, FFHardwareController hardware){
        pipeline = new LineFinderPipeline(hardware.getIntakeSystem());
        hardware.getIntakeSystem().moveCameraInspection();
        WebcamName webcamName = hardwareMap.get(WebcamName.class, "lineCam");
        OpenCvCamera camera = OpenCvCameraFactory.getInstance().createWebcam(webcamName);
        camera.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener() {
            @Override
            public void onOpened() {
                camera.openCameraDevice();
                camera.setPipeline(pipeline);
                camera.startStreaming(320, 240, OpenCvCameraRotation.UPRIGHT);
                opened = true;
            }

            @Override
            public void onError(int errorCode) {

            }
        });
        FtcDashboard.getInstance().startCameraStream(camera, 60);
    }

    public LineFinderPipeline getPipeline() {
        return pipeline;
    }

    public boolean isOpened() {
        return opened;
    }
}
