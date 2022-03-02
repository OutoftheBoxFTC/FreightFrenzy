package Opmodes.TestOpmodes;

import com.acmerobotics.dashboard.FtcDashboard;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;

import Hardware.Pipelines.LineFinderPipeline;
import MathSystems.Vector.Vector3;
import Opmodes.BasicOpmode;
import RoadRunner.drive.SampleMecanumDrive;
import State.Action.Action;
import State.Action.ActionController;
import State.Action.ActionQueue;
import State.Action.InstantAction;
import State.Action.StandardActions.DelayAction;
import Utils.OpmodeStatus;

@TeleOp
public class CameraDriveTester extends BasicOpmode {
    @Override
    public void setup() {
        SampleMecanumDrive drive = new SampleMecanumDrive(hardwareMap);

        LineFinderPipeline pipeline = new LineFinderPipeline(hardware.getIntakeSystem());
        hardware.getIntakeSystem().moveCameraInspection();
        WebcamName webcamName = hardwareMap.get(WebcamName.class, "lineCam");
        OpenCvCamera camera = OpenCvCameraFactory.getInstance().createWebcam(webcamName);
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

        ActionQueue queue = new ActionQueue();
        queue.submitAction(new InstantAction() {
            @Override
            public void update() {
                hardware.getIntakeSystem().moveCameraDown();
            }
        });
        queue.submitAction(new DelayAction(50));
        queue.submitAction(new InstantAction() {
            @Override
            public void update() {
                hardware.getDrivetrainSystem().setPower(new Vector3(0, -0.7, 0));
            }
        });
        queue.submitAction(new DelayAction(300));
        queue.submitAction(new Action() {
            @Override
            public void update() {
                hardware.getDrivetrainSystem().setPower(new Vector3(0, -0.7, 0));
            }

            @Override
            public boolean shouldDeactivate() {
                return pipeline.getY() > 0;
            }
        });
        queue.submitAction(new Action() {
            @Override
            public void update() {
                hardware.getDrivetrainSystem().setPower(Vector3.ZERO());
                hardware.getIntakeSystem().getCameraServo().setPosition(0.85);
                pipeline.pitchOffset = -30;
                //hardware.getIntakeSystem().moveCameraUp();
                telemetry.addData("Distance", pipeline.getRealY());
                telemetry.addData("Real Distance", pipeline.getRealY() + 10);
            }
        });
        OpmodeStatus.bindOnStart(queue);
    }
}
