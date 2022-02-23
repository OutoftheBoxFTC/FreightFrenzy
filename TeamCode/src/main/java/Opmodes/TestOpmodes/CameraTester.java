package Opmodes.TestOpmodes;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;

import Hardware.Pipelines.LineFinderPipeline;
import MathSystems.Vector.Vector3;
import Opmodes.BasicOpmode;
import State.Action.Action;
import Utils.OpmodeStatus;

@TeleOp
@Config
public class CameraTester extends BasicOpmode {
    public static double CAMERA_ANGLE = 0;
    @Override
    public void setup() {
        hardware.getIntakeSystem().moveCameraDown();
        hardware.getIntakeSystem().unlockIntake();
        CAMERA_ANGLE = hardware.getIntakeSystem().getCameraServo().getServo().getPosition();
        WebcamName webcamName = hardwareMap.get(WebcamName.class, "lineCam");
        OpenCvCamera camera = OpenCvCameraFactory.getInstance().createWebcam(webcamName);
        LineFinderPipeline pipeline = new LineFinderPipeline();
        camera.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener() {
            @Override
            public void onOpened() {
                camera.openCameraDevice();
                camera.setPipeline(pipeline);
                camera.startStreaming(320, 240, OpenCvCameraRotation.UPRIGHT);
            }

            @Override
            public void onError(int errorCode) {

            }
        });
        FtcDashboard.getInstance().startCameraStream(camera, 60);

        OpmodeStatus.bindOnStart(new Action() {
            long timer = 0;
            @Override
            public void update() {
                FtcDashboard.getInstance().getTelemetry().addData("Y", pipeline.getRealY());
                FtcDashboard.getInstance().getTelemetry().update();
                if(pipeline.getY() > 0){
                    //hardware.getIntakeSystem().intake();
                }else{
                    //hardware.getIntakeSystem().idleIntake();
                }
                hardware.getIntakeSystem().getCameraServo().setPosition(CAMERA_ANGLE);
            }
        });

        OpmodeStatus.bindOnStart(() -> hardware.getDrivetrainSystem().setPower(new Vector3(gamepad1.left_stick_x, -gamepad1.left_stick_y, gamepad1.right_stick_x)));

    }
}
