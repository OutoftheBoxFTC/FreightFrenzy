package Hardware.HardwareSystems.FFSystems;

import com.acmerobotics.dashboard.FtcDashboard;
import com.qualcomm.hardware.rev.RevColorSensorV3;
import com.qualcomm.robotcore.hardware.ColorRangeSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

import Hardware.HardwareSystems.HardwareSystem;
import Hardware.SmartDevices.SmartLynxModule.SmartLynxModule;
import Hardware.SmartDevices.SmartMotor.SmartMotor;

public class IntakeSystem implements HardwareSystem {
    private SmartMotor intakeMotor;
    private DigitalChannel intakeStop;
    private RevColorSensorV3 bucketSensor;
    private double power;
    private long timer = 0;
    private double distance = 0;

    public IntakeSystem(SmartLynxModule chub, SmartLynxModule revHub, HardwareMap map){
        intakeMotor = revHub.getMotor(FFConstants.ExpansionPorts.INTAKE_MOTOR_PORT);
        intakeStop = chub.getDigitalController(1);
        bucketSensor = map.get(RevColorSensorV3.class, "bucketSensor");
    }

    @Override
    public void initialize() {
        intakeStop.setMode(DigitalChannel.Mode.INPUT);
        intakeMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    }

    @Override
    public void update() {
        intakeMotor.setPower(power);
        if(System.currentTimeMillis() > timer){
            timer = System.currentTimeMillis() + 50;
            distance = bucketSensor.getDistance(DistanceUnit.MM);
        }
    }

    public void setPower(double power){
        this.power = power;
    }

    public boolean getIntakeStop(){
        return !intakeStop.getState();
    }

    public boolean inIntake() {
        return distance < 30;
    }

    public double getDistance() {
        return distance;
    }
}
