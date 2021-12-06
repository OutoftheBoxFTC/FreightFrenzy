package Hardware.SmartDevices.Ultrasonic;

import com.qualcomm.robotcore.hardware.AnalogInput;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

import Hardware.SmartDevices.SmartAnalogInput.SmartAnalogInput;

public abstract class SmartUltrasonicSensor extends SmartAnalogInput {
    public SmartUltrasonicSensor(AnalogInput analogInput) {
        super(analogInput);
    }

    public abstract double getDistanceCm();

    public double getDistance(DistanceUnit unit){
        switch (unit){
            case METER:
                return getDistanceCm() / 100.0;
            case CM:
                return getDistanceCm();
            case MM:
                return getDistanceCm() * 10.0;
            case INCH:
                return getDistanceCm() / 2.54;
            default:
                return getValue();
        }
    }
}
