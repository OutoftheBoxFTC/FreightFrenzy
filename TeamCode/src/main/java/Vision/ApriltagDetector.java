package Vision;

import com.acmerobotics.dashboard.FtcDashboard;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.opencv.core.Point;
import org.openftc.apriltag.AprilTagDetection;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;

import java.util.ArrayList;

import State.Action.Action;

public class ApriltagDetector implements Action {
    private static final float DEC_HIGH = 3;
    private static final float DEC_LOW = 2;
    private static final float THRESH_CLOSE = 1f;
    private static final int NUM_NODETECT_FRAMES = 4;

    private static final double TAGSIZE = 0.1;
    int nodetectFrames = 0;

    private double fx = 578.272;
    private double fy = 578.272;
    private double cx = 402.145;
    private double cy = 221.506;

    private OpenCvCamera camera;
    private AprilTagDetectionPipeline pipeline;

    private Point lastPoint = new Point(0, 0);

    public ApriltagDetector(HardwareMap hardwareMap){
        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        camera = OpenCvCameraFactory.getInstance().createWebcam(hardwareMap.get(WebcamName.class, "Webcam"), cameraMonitorViewId);
        pipeline = new AprilTagDetectionPipeline(TAGSIZE, fx, fy, cx, cy);

        camera.setPipeline(pipeline);
        camera.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener() {
            @Override
            public void onOpened() {
                camera.startStreaming(1920, 1080, OpenCvCameraRotation.UPRIGHT);
                FtcDashboard.getInstance().startCameraStream(camera, 0);
            }

            @Override
            public void onError(int errorCode) {

            }
        });
    }

    @Override
    public void update() {
        ArrayList<AprilTagDetection> detections = pipeline.getLatestDetections();
        if(detections != null){
            if(detections.size() == 0){
                nodetectFrames++;
                if(nodetectFrames > NUM_NODETECT_FRAMES){
                    pipeline.setDecimation(DEC_LOW);
                }
            }else{
                nodetectFrames = 0;
                if(detections.get(0).pose.z < THRESH_CLOSE){
                    //pipeline.setDecimation(DEC_HIGH);
                }
                lastPoint = detections.get(0).center;
            }
        }
    }

    public Point getLastPoint() {
        return lastPoint;
    }

    public POSITION getPosition(){
        if(getLastPoint().x > 1250){
            return POSITION.LEFT;
        }else if(getLastPoint().x > 1100){
            return POSITION.CENTRE;
        }else{
            return POSITION.RIGHT;
        }
    }

    public void shutdown() {
        camera.closeCameraDeviceAsync(() -> {
            camera.closeCameraDevice();
        });
    }

    public enum POSITION{
        LEFT,
        CENTRE,
        RIGHT
    }
}
