package Hardware.HardwareSystems.FFSystems;

import com.acmerobotics.dashboard.FtcDashboard;
import com.qualcomm.hardware.rev.Rev2mDistanceSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

import Hardware.HardwareSystems.HardwareSystem;
import Hardware.SmartDevices.AsyncRev2MSensor;
import Hardware.SmartDevices.MB1242;
import Hardware.SmartDevices.SmartLynxModule.SmartLynxModule;
import Hardware.SmartDevices.SmartMotor.SmartMotor;
import Hardware.SmartDevices.Ultrasonic.DFRoboticsUltrasonicSensor;

public class OdometrySystem implements HardwareSystem {
    private SmartMotor fl, fr, bl, br;

    private Rev2mDistanceSensor left, right;
    private AsyncRev2MSensor leftSensor, rightSensor;
    private MB1242 forwardSensor;
    private double leftDist, rightDist;

    private long timer = 0;

    public OdometrySystem(SmartLynxModule chub, HardwareMap hardwareMap){
        fl = chub.getMotor(0);
        fr = chub.getMotor(1);
        bl = chub.getMotor(2);
        br = chub.getMotor(3);
        left = hardwareMap.get(Rev2mDistanceSensor.class, "left");
        //right = hardwareMap.get(Rev2mDistanceSensor.class, "right");
        leftSensor = new AsyncRev2MSensor(left);
        //rightSensor = new AsyncRev2MSensor(right);
        forwardSensor = hardwareMap.tryGet(MB1242.class, "frontSensor");
    }

    @Override
    public void initialize() {
        fl.getMotor().setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        fr.getMotor().setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        bl.getMotor().setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        br.getMotor().setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        timer = System.currentTimeMillis() + 100;

        leftSensor.setMeasurementIntervalMs(100);
        //rightSensor.setMeasurementIntervalMs(100);
    }

    @Override
    public void update() {
        if(System.currentTimeMillis() > timer){
            timer = System.currentTimeMillis() + 100;
        }
        leftDist = leftSensor.getDistance(DistanceUnit.INCH);
        //rightDist = rightSensor.getDistance(DistanceUnit.INCH);
    }

    public double getFl(){
        return fl.getMotor().getCurrentPosition();
    }

    public double getFr(){
        return fr.getMotor().getCurrentPosition();
    }

    public double getBl(){
        return bl.getMotor().getCurrentPosition();
    }

    public double getBr(){
        return br.getMotor().getCurrentPosition();
    }

    public double getLeftDist() {
        return leftDist;
    }

    public double getRightDist() {
        return rightDist;
    }

    public double getForwardDist(){
        return forwardSensor.getDistance(DistanceUnit.INCH);
    }
}
