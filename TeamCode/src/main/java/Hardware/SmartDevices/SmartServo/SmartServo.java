package Hardware.SmartDevices.SmartServo;

import com.qualcomm.robotcore.hardware.PwmControl.*;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.ServoImplEx;

import MathSystems.Angle;

public class SmartServo {
    private ServoImplEx servo;
    private PwmRange pmwRange;
    private double lastPosition;

    public SmartServo(Servo servo){
        this.servo = (ServoImplEx) servo;
        pmwRange = this.servo.getPwmRange();
    }

    public void setPosition(double position){
        if(Math.abs(position - lastPosition) > 0.0001){
            servo.setPosition(position);
            lastPosition = position;
        }
    }

    public void setPositionUs(double positionUs){
        double servoPos = (positionUs - pmwRange.usPulseLower) / (pmwRange.usPulseUpper - pmwRange.usPulseLower);
        setPosition(servoPos);
    }

    public void setPmwRange(double usLower, double usUpper){
        this.pmwRange = new PwmRange(usLower, usUpper);
        servo.setPwmRange(pmwRange);
    }

    public void disableServo(){
        servo.setPwmDisable();
    }

    public void enableServo(){
        servo.setPwmEnable();
    }

    public void setAngleGobilda(Angle angle){
        setPosition(angle.degrees() / 270.0);
    }

    public ServoImplEx getServo() {
        return servo;
    }
}
