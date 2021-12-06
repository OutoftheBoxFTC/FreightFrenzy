package Hardware.SmartDevices.SmartAnalogInput;

import com.qualcomm.robotcore.hardware.AnalogInput;

import MathSystems.Angle;

public class SmartPotentiometer extends SmartAnalogInput{
    private final Angle minAngle, maxAngle;
    private Angle offsetAngle;

    public SmartPotentiometer(SmartAnalogInput smartAnalogInput, Angle minAngle, Angle maxAngle) {
        super(smartAnalogInput.getAnalogInput());
        this.minAngle = minAngle;
        this.maxAngle = maxAngle;
    }

    public void setOffsetAngle(Angle offsetAngle) {
        this.offsetAngle = offsetAngle;
    }

    public Angle getAngle(){
        double travelRange = maxAngle.degrees() - minAngle.degrees();
        double angle = minAngle.degrees() + (getValue() * travelRange);
        if(offsetAngle != null) {
            angle -= offsetAngle.degrees();
        }

        return Angle.degrees(angle);
    }
}
