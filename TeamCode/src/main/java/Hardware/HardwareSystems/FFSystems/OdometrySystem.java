package Hardware.HardwareSystems.FFSystems;

import com.qualcomm.hardware.rev.Rev2mDistanceSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

import Hardware.HardwareSystems.HardwareSystem;
import Hardware.SmartDevices.SmartLynxModule.SmartLynxModule;
import Hardware.SmartDevices.SmartMotor.SmartMotor;

public class OdometrySystem implements HardwareSystem {
    private SmartMotor fl, fr, bl, br;

    private Rev2mDistanceSensor left, right;
    private double leftDist, rightDist;

    private long timer = 0;

    public OdometrySystem(SmartLynxModule chub, HardwareMap hardwareMap){
        fl = chub.getMotor(0);
        fr = chub.getMotor(1);
        bl = chub.getMotor(2);
        br = chub.getMotor(3);
        left = hardwareMap.get(Rev2mDistanceSensor.class, "left");
        right = hardwareMap.get(Rev2mDistanceSensor.class, "right");
    }

    @Override
    public void initialize() {
        fl.getMotor().setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        fr.getMotor().setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        bl.getMotor().setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        br.getMotor().setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        timer = System.currentTimeMillis() + 100;
    }

    @Override
    public void update() {
        if(System.currentTimeMillis() > timer){
            timer = System.currentTimeMillis() + 100;
            leftDist = left.getDistance(DistanceUnit.INCH);
            rightDist = right.getDistance(DistanceUnit.INCH);
        }
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
}
