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
import State.Action.Action;
import State.Action.ActionQueue;
import State.Action.InstantAction;
import State.Action.StandardActions.DelayAction;
import Utils.OpmodeStatus;

@TeleOp
public class CameraRepeatTester extends BasicOpmode {
    @Override
    public void setup() {
        LineFinderPipeline pipeline = new LineFinderPipeline();
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
        for(int i = 0; i < 5; i ++) {
            queue.submitAction(new InstantAction() {
                @Override
                public void update() {
                    hardware.getIntakeSystem().moveCameraDown();
                }
            });
            queue.submitAction(new DelayAction(400));
            queue.submitAction(new Action() {
                @Override
                public void update() {
                    hardware.getDrivetrainSystem().setPower(new Vector3(-0.2, -1, 0));
                }

                @Override
                public boolean shouldDeactivate() {
                    return pipeline.getY() > 0;
                }
            });
            queue.submitAction(new Action() {
                long start = 0;

                @Override
                public void initialize() {
                    start = System.currentTimeMillis() + 1000;
                }

                @Override
                public void update() {
                    hardware.getDrivetrainSystem().setPower(Vector3.ZERO());
                    hardware.getIntakeSystem().getCameraServo().setPosition(0.85);
                    pipeline.pitchOffset = -30;
                    //hardware.getIntakeSystem().moveCameraUp();
                    telemetry.addData("Distance", pipeline.getRealY());
                    telemetry.addData("Real Distance", pipeline.getRealY() + 10);
                }

                @Override
                public boolean shouldDeactivate() {
                    return System.currentTimeMillis() > start;
                }
            });
            queue.submitAction(new InstantAction() {
                @Override
                public void update() {
                    hardware.getDrivetrainSystem().setPower(new Vector3(-0.2, 1, 0));
                }
            });
            queue.submitAction(new DelayAction(1000));
            queue.submitAction(new InstantAction() {
                @Override
                public void update() {
                    hardware.getDrivetrainSystem().setPower(Vector3.ZERO());
                }
            });
        }
        queue.submitAction(new DelayAction(1000));
        OpmodeStatus.bindOnStart(queue);
    }
}
