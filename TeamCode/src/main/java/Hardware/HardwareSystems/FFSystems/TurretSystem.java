package Hardware.HardwareSystems.FFSystems;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Hardware.HardwareSystems.FFSystems.Actions.MoveExtensionAction;
import Hardware.HardwareSystems.FFSystems.Actions.MovePitchAction;
import Hardware.HardwareSystems.FFSystems.Actions.MoveTurretAction;
import Hardware.HardwareSystems.HardwareSystem;
import Hardware.SmartDevices.SmartAnalogInput.SmartPotentiometer;
import Hardware.SmartDevices.SmartLynxModule.SmartLynxModule;
import Hardware.SmartDevices.SmartMotor.SmartMotor;
import Hardware.SmartDevices.SmartServo.SmartServo;
import MathSystems.Angle;
import MathSystems.MathUtils;
import State.Action.ActionController;
import State.Action.ActionQueue;
@Config
public class TurretSystem implements HardwareSystem {
    public static final double TICKS_PER_DEGREE_PANCAKES = -6.604477;
    public static final int PITCH_SMOOTHING = 20;
    private Angle finalTurretAngle = Angle.ZERO(), finalPitchAngle = Angle.ZERO();

    private final MoveTurretAction moveTurretAction;
    private final MovePitchAction movePitchAction;
    private final MoveExtensionAction moveExtensionAction;

    private final SmartMotor turretMotor, pitchMotor, extensionMotor;
    private final SmartServo bucketServo;
    private final SmartPotentiometer turretPotentiometer, pitchPotentiometer;

    private Angle turretAngle, prevTurretAngle, turretVel;

    private final List<Angle> pitchFilter;

    private int offset = 0;

    private long last;

    public TurretSystem(SmartLynxModule chub, SmartLynxModule ehub, HardwareMap map){

        pitchFilter = Collections.synchronizedList(new ArrayList<>());

        //turretMotor = new SmartMotor(map.dcMotor.get("turretMotor"));
        turretMotor = ehub.getMotor(FFConstants.ExpansionPorts.TURRET_MOTOR_PORT);
        turretPotentiometer = new SmartPotentiometer(chub.getAnalogInput(FFConstants.ExpansionPorts.TURRET_POTENTIOMETER_PORT),
                FFConstants.Turret.TURRET_MIN_ANGLE, FFConstants.Turret.TURRET_MAX_ANGLE);

        turretPotentiometer.setOffsetAngle(Angle.degrees(150));

        pitchMotor = ehub.getMotor(FFConstants.ExpansionPorts.PITCH_MOTOR_PORT);
        pitchPotentiometer = new SmartPotentiometer(ehub.getAnalogInput(FFConstants.ExpansionPorts.PITCH_POTENTIOMETER_PORT),
                FFConstants.Pitch.PITCH_MIN_ANGLE, FFConstants.Pitch.PITCH_MAX_ANGLE);

        pitchPotentiometer.setOffsetAngle(Angle.degrees(258));

        extensionMotor = ehub.getMotor(FFConstants.ExpansionPorts.EXTENSION_MOTOR_PORT);

        moveTurretAction = new MoveTurretAction(this);
        movePitchAction = new MovePitchAction(this);
        moveExtensionAction = new MoveExtensionAction(this);

        bucketServo = ehub.getServo(0);

        turretAngle = Angle.ZERO();
        prevTurretAngle = Angle.ZERO();
        turretVel = Angle.ZERO();
    }

    @Override
    public void initialize() {
        last = System.currentTimeMillis();
        moveTurretAction.submit();
        movePitchAction.submit();
        moveExtensionAction.submit();
        offset = getExtensionPosition();
        extensionMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        //pitchMotor.getMotor().setTargetPosition(pitchMotor.getMotor().getCurrentPosition());
        //pitchMotor.getMotor().setPower(0.4);
        //pitchMotor.getMotor().setMode(DcMotor.RunMode.RUN_TO_POSITION);
    }

    @Override
    public void update() {
        long now = System.currentTimeMillis();
        double dt = (now - last) / 1000.0;

        turretAngle = Angle.degrees(turretPotentiometer.getAngle().degrees() * 1.1632653); // 49, 8
        Angle dTurret = MathUtils.getRotDist(prevTurretAngle, turretAngle);
        turretVel = Angle.degrees(dTurret.degrees() / dt);

        prevTurretAngle = turretAngle;

        synchronized (pitchFilter) {
            pitchFilter.add(pitchPotentiometer.getAngle());
            if (pitchFilter.size() > PITCH_SMOOTHING) {
                pitchFilter.remove(0);
            }
        }

        last = now;
    }

    public Angle getTurretPosition(){
        return turretAngle;
    }

    public Angle getTurretVel() {
        return turretVel;
    }

    public void moveTurretRaw(Angle angle){
        if(!angle.equals(finalTurretAngle)){
            finalTurretAngle = angle;
            moveTurretAction.setTargetAngle(angle);
        }
    }

    public void setTurretMotorPower(double power){
        turretMotor.setPower(power);
    }

    public void setPitchMotorPower(double pitchPower){
        pitchMotor.setPower(pitchPower);
    }

    public void setExtensionMotorPower(double power){
        extensionMotor.setPower(power);
    }

    public int getPitchMotorPos(){
        return pitchMotor.getMotor().getCurrentPosition();
    }

    public Angle getPitchPosition(){
        return pitchPotentiometer.getAngle();
    }

    public void movePitchRaw(Angle angle){
        if(!angle.equals(finalPitchAngle)){
            finalPitchAngle = angle;
            int targetEncoderPos = (int) (MathUtils.getRotDist(angle, getPitchPosition()).degrees() * TICKS_PER_DEGREE_PANCAKES);
            movePitchAction.setTargetAngle(finalPitchAngle);
        }
    }

    public int getExtensionPosition(){
        return extensionMotor.getMotor().getCurrentPosition() - offset;
    }

    public SmartMotor getExtensionMotor() {
        return extensionMotor;
    }

    public void moveExtensionRaw(double target) {
        moveExtensionAction.setTargetPos(target);
    }

    public boolean isTurretAtPos(){
        return moveTurretAction.isAtTarget();
    }

    public boolean isPitchAtPos(){
        return movePitchAction.isAtTarget();
    }

    public boolean isExtensionAtPos(){
        return moveExtensionAction.isAtTarget();
    }

    public void setBucketPosRaw(double pos){
        bucketServo.setPosition(pos);
    }
}
