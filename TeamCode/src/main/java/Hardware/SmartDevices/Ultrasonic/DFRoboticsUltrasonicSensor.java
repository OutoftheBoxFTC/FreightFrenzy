package Hardware.SmartDevices.Ultrasonic;

import com.qualcomm.robotcore.hardware.AnalogInput;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

import Hardware.SmartDevices.SmartAnalogInput.SmartAnalogInput;

public class DFRoboticsUltrasonicSensor extends SmartUltrasonicSensor {
    public DFRoboticsUltrasonicSensor(AnalogInput analogInput) {
        super(analogInput);
    }

    public double getDistanceCm(){
        return getValue() * 520.0;
    }
}
