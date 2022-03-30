package Hardware.Pipelines;

import com.acmerobotics.dashboard.FtcDashboard;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;

import Hardware.FFHardwareController;
import Hardware.HardwareSystems.FFSystems.IntakeSystem;

public class LineFinderCamera {
    private LineFinderPipeline pipeline;
    private TSEPipeline tsePipeline;
    private OpenCvCamera camera;
    private boolean opened = false;

    public LineFinderCamera(HardwareMap hardwareMap, IntakeSystem intakeSystem){
        pipeline = new LineFinderPipeline();
        tsePipeline = new TSEPipeline();
        WebcamName webcamName = hardwareMap.get(WebcamName.class, "lineCam");
        camera = OpenCvCameraFactory.getInstance().createWebcam(webcamName);
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

    public LineFinderPipeline getLinePipeline() {
        return pipeline;
    }

    public TSEPipeline getTSEPipeline(){
        return tsePipeline;
    }

    public void switchTSE(){
        camera.setPipeline(tsePipeline);
    }

    public void switchLine(){
        camera.setPipeline(pipeline);
    }

    public void setExposure(){

    }

    public boolean isOpened() {
        return opened;
    }
}
