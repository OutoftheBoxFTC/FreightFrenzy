package Hardware.SmartDevices.SmartAnalogInput;

import com.qualcomm.robotcore.hardware.AnalogInput;

import MathSystems.Angle;

public class SmartPotentiometer extends SmartAnalogInput{
    private double angleConstant;
    private Angle offsetAngle;

    public SmartPotentiometer(SmartAnalogInput smartAnalogInput, double angleConstant) {
        super(smartAnalogInput.getAnalogInput());
        this.angleConstant = angleConstant;
    }

    public SmartPotentiometer(SmartAnalogInput smartAnalogInput, double angleConstant, double maxVoltage) {
        super(smartAnalogInput.getAnalogInput(), maxVoltage);
        this.angleConstant = angleConstant;
    }

    public void setOffsetAngle(Angle offsetAngle) {
        this.offsetAngle = offsetAngle;
    }

    public Angle getAngle(){
        double angle = getValue() * angleConstant;
        if(offsetAngle != null) {
            angle -= offsetAngle.degrees();
        }

        return Angle.degrees(angle);
    }
}
