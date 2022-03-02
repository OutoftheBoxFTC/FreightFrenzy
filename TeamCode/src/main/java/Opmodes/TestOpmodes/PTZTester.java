package Opmodes.TestOpmodes;

import com.acmerobotics.dashboard.FtcDashboard;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;

import Hardware.Pipelines.LineFinderCamera;
import Hardware.Pipelines.LineFinderPipeline;
import MathSystems.Angle;
import Opmodes.BasicOpmode;
import State.Action.Action;
import Utils.OpmodeStatus;
import Utils.ProgramClock;
@TeleOp
public class PTZTester extends BasicOpmode {
    @Override
    public void setup() {
        LineFinderCamera camera = new LineFinderCamera(hardwareMap, hardware);

        hardware.getIntakeSystem().moveCameraSearch();
        OpmodeStatus.bindOnStart(new Action() {
            double pan = 270.0/2, tilt = 0.85;
            @Override
            public void update() {
                if(gamepad1.dpad_up){
                    tilt += ProgramClock.getFrameTimeSeconds() * 0.3;
                }
                if(gamepad1.dpad_down){
                    tilt -= ProgramClock.getFrameTimeSeconds() * 0.3;
                }
                if(gamepad1.dpad_left){
                    pan += ProgramClock.getFrameTimeSeconds() * 60;
                }
                if(gamepad1.dpad_right){
                    pan -= ProgramClock.getFrameTimeSeconds() * 60;
                }
                telemetry.addData("Pan", pan);
                telemetry.addData("Tilt", tilt);
                telemetry.addData("Pitch", camera.getPipeline().pitchOffset);
                telemetry.addData("Dist", camera.getPipeline().getRealY());
                if(gamepad1.y){
                    hardware.getIntakeSystem().getCameraServo().setPosition(0.2483);
                    hardware.getIntakeSystem().panCamera(Angle.degrees(128.775));
                }else if(gamepad1.a){
                    hardware.getIntakeSystem().getCameraServo().setPosition(0.81074);
                    hardware.getIntakeSystem().panCamera(Angle.degrees(128.775));
                }else if(gamepad1.x){
                    hardware.getIntakeSystem().getCameraServo().setPosition(0.66841);
                    hardware.getIntakeSystem().panCamera(Angle.degrees(243.7563));
                }else if(gamepad1.b){
                    hardware.getIntakeSystem().getCameraServo().setPosition(0.67979);
                    hardware.getIntakeSystem().panCamera(Angle.degrees(20.09129));
                }else {
                    hardware.getIntakeSystem().getCameraServo().setPosition(tilt);
                    hardware.getIntakeSystem().panCamera(Angle.degrees(pan));
                }
            }
        });
    }
}
