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
        hardware.getIntakeSystem().moveCameraSearch();
        OpmodeStatus.bindOnStart(new Action() {
            double pan = 0.5, tilt = 0.85, zoom = 0;
            @Override
            public void update() {
                if(gamepad1.dpad_up){
                    tilt += ProgramClock.getFrameTimeSeconds() * 0.3;
                }
                if(gamepad1.dpad_down){
                    tilt -= ProgramClock.getFrameTimeSeconds() * 0.3;
                }
                if(gamepad1.dpad_left){
                    pan += ProgramClock.getFrameTimeSeconds() * 0.4;
                }
                if(gamepad1.dpad_right){
                    pan -= ProgramClock.getFrameTimeSeconds() * 0.4;
                }
                zoom += ProgramClock.getFrameTimeSeconds() * 0.2 * gamepad1.left_stick_y;
                if(zoom >= 0.5){
                    zoom = 0.49;
                }
                if(zoom < 0){
                    zoom = 0;
                }
                telemetry.addData("Pan", pan);
                telemetry.addData("Tilt", tilt);
                if(gamepad1.y){
                    hardware.getIntakeSystem().moveCameraInspection();
                    hardware.getIntakeSystem().setZoom(0);
                }else if(gamepad1.a){
                    hardware.getIntakeSystem().moveCameraLine();
                    hardware.getIntakeSystem().setZoom(0);
                }else if(gamepad1.x){
                    hardware.getIntakeSystem().moveCameraRedTSE();
                    hardware.getIntakeSystem().setZoom(0);
                }else if(gamepad1.b){
                    hardware.getIntakeSystem().moveCameraBlueTSE();
                    hardware.getIntakeSystem().setZoom(0);
                }else {
                    hardware.getIntakeSystem().getCameraServo().setPosition(tilt);
                    hardware.getIntakeSystem().getPanServo().setPosition(pan);
                    hardware.getIntakeSystem().setZoom(zoom);
                }
            }
        });
    }
}
