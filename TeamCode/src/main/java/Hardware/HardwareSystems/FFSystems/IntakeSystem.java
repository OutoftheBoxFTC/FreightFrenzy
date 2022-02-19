package Hardware.HardwareSystems.FFSystems;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.hardware.rev.RevColorSensorV3;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

import Hardware.HardwareSystems.HardwareSystem;
import Hardware.SmartDevices.SmartLynxModule.SmartLynxModule;
import Hardware.SmartDevices.SmartMotor.SmartMotor;
import Hardware.SmartDevices.SmartServo.SmartServo;

public class IntakeSystem implements HardwareSystem {
    private SmartMotor intakeMotor;

    private SmartServo intakeStop;

    private double power;
    private long timer = 0;
    private double distance = 0;
    private double hubVoltage = 12;
    private LynxModule expansionHub;

    private INTAKE_STATE currentState = INTAKE_STATE.IDLE, targetState = INTAKE_STATE.IDLE;

    public IntakeSystem(SmartLynxModule chub, SmartLynxModule revHub, HardwareMap map){
        intakeMotor = chub.getMotor(0);
        intakeStop = revHub.getServo(0);
        expansionHub = revHub.getModule();
    }

    @Override
    public void initialize() {
        intakeMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    }

    @Override
    public void update() {
        double current = intakeMotor.getMotor().getCurrent(CurrentUnit.MILLIAMPS);
        if(System.currentTimeMillis() > timer){
            if(currentState != targetState){
                currentState = targetState;
            }
        }
        switch (currentState){
            case IDLE:
                setPower(0);
                unlockIntake();
                break;
            case LOCKED:
                setPower(-0.2);
                lockIntake();
                break;
            case LOCKING:
                setPower(-0.3);
                if(System.currentTimeMillis() > timer){
                    timer = System.currentTimeMillis() + 100;
                }
                targetState = INTAKE_STATE.LOCKED;
                break;
            case OUTTAKING:
                setPower(-1);
                if(System.currentTimeMillis() > timer) {
                    timer = System.currentTimeMillis() + 100;
                }
                targetState = INTAKE_STATE.LOCKING;
                break;
            case INTAKING:
                setPower(1);
                break;
        }
        intakeMotor.setPower(-power);
    }

    public void setPower(double power){
        this.power = power;
    }

    public double getDistance() {
        return distance;
    }

    public void outtake() {
        if(currentState == INTAKE_STATE.IDLE) {
            targetState = INTAKE_STATE.OUTTAKING;
        }
        if(currentState == INTAKE_STATE.LOCKED) {
            unlockIntake();
            timer = System.currentTimeMillis() + 100;
        }
    }

    public void lock(){
        if(currentState == INTAKE_STATE.IDLE) {
            targetState = INTAKE_STATE.OUTTAKING;
        }
    }

    public void intake(){
        targetState = INTAKE_STATE.INTAKING;
        if(currentState == INTAKE_STATE.LOCKED){
            unlockIntake();
            timer = System.currentTimeMillis() + 100;
        }
    }

    public void idleIntake(){
        targetState = INTAKE_STATE.IDLE;
    }

    public boolean locked() {
        return currentState == INTAKE_STATE.LOCKED;
    }

    public void lockIntake(){
        intakeStop.setPosition(0.33);
    }

    public void unlockIntake(){
        intakeStop.setPosition(0.23);
    }

    enum INTAKE_STATE{
        IDLE,
        OUTTAKING,
        INTAKING,
        LOCKING,
        LOCKED
    }
}