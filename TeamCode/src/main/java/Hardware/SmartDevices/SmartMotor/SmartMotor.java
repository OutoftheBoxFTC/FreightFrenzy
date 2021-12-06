package Hardware.SmartDevices.SmartMotor;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import org.firstinspires.ftc.robotcore.external.navigation.VoltageUnit;

public class SmartMotor {
    private final DcMotorEx motor;
    private double lastPower = 0;

    private boolean voltageCorrection;
    private double voltageCorrectionFactor = 1;

    private LynxModule module;
    private DcMotor.ZeroPowerBehavior behavior;
    private boolean behaviorChanged;

    public SmartMotor(DcMotor motor){
        this.motor = (DcMotorEx) motor;
        voltageCorrection = false;
        behavior = this.motor.getZeroPowerBehavior();
        behaviorChanged = false;
    }

    public void enableVoltageCorrection(LynxModule motorModule){
        this.module = motorModule;
        this.voltageCorrection = true;
    }

    public void disableVoltageCorrection(){
        this.voltageCorrection = false;
    }

    public void setVoltageCorrectionFactor(double voltageCorrectionFactor) {
        this.voltageCorrectionFactor = voltageCorrectionFactor;
    }

    public void setPower(double power){
        if(Math.abs(power - lastPower) > 0.001){
            double powerLoc = power;
            if(voltageCorrection) {
                double factor = (12 / module.getInputVoltage(VoltageUnit.VOLTS)) * voltageCorrectionFactor;
                powerLoc = power * factor;
            }
            motor.setPower(powerLoc);
            lastPower = powerLoc;
        }
        if(behaviorChanged)
            motor.setZeroPowerBehavior(behavior);
    }

    public void setZeroPowerBehavior(DcMotor.ZeroPowerBehavior behavior){
        if(behavior != this.behavior) {
            this.behavior = behavior;
            behaviorChanged = true;
        }
    }

    public DcMotor.ZeroPowerBehavior getZeroPowerBehavior() {
        return behavior;
    }

    public double getPower() {
        return lastPower;
    }

    public void reverse(){
        motor.setDirection(motor.getDirection() == DcMotorSimple.Direction.FORWARD ? DcMotorSimple.Direction.REVERSE : DcMotorSimple.Direction.FORWARD);
    }

    public DcMotorEx getMotor() {
        return motor;
    }
}
