package Hardware.HardwareSystems.UGSystems;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

import Hardware.HardwareSystems.HardwareSystem;
import Hardware.SmartDevices.SmartMotor.SmartMotor;
import Hardware.SmartDevices.SmartServo.SmartServo;

public class IntakeSystem implements HardwareSystem {
    private SmartMotor intakeLower, intakeUpper;
    private SmartServo intakeShield;

    private double intakePower, shieldPosition;

    public IntakeSystem(DcMotor intakeLower, DcMotor intakeUpper, Servo intakeShield){
        this.intakeLower = new SmartMotor(intakeLower);
        this.intakeUpper = new SmartMotor(intakeUpper);
        this.intakeShield = new SmartServo(intakeShield);
    }

    @Override
    public void update() {
        intakeLower.setPower(intakePower);
        intakeUpper.setPower(intakePower);

        intakeShield.setPositionUs(shieldPosition);
    }

    public void setIntakePower(double intakePower) {
        this.intakePower = intakePower;
    }

    public void intake(){
        this.intakePower = 1;
    }

    public void outtake(){
        this.intakePower = -1;
    }

    public void stopIntake(){
        this.intakePower = 0;
    }

    public void shieldDown(){
        this.shieldPosition = 1175;
    }

    public void shieldUp(){
        this.shieldPosition = 2250;
    }

    public void shieldIdle(){
        this.shieldPosition = 1400;
    }
}
