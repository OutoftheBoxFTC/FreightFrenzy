package Hardware.HardwareSystems.FFSystems;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

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

@Config
public class TurretSystem implements HardwareSystem {
    public static final double TICKS_PER_DEGREE_PANCAKES = -6.604477;
    public static int TURRET_SMOOTHING = 5;
    private Angle finalTurretAngle = Angle.ZERO(), finalPitchAngle = Angle.ZERO();

    private final MoveTurretAction moveTurretAction;
    private final MovePitchAction movePitchAction;
    private final MoveExtensionAction moveExtensionAction;

    private final SmartMotor turretMotor, pitchMotor, extensionMotor;
    private final SmartServo bucketServo, armServo;
    private final SmartPotentiometer turretPotentiometer, pitchPotentiometer;

    private Angle turretAngle, prevTurretAngle, turretVel, initialPitch;

    private int initialTurret;

    private final List<Angle> pitchFilter;

    private int offset = 0;

    private long timer = 0;

    private long last;

    private LynxModule chub;

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

        pitchPotentiometer.setOffsetAngle(Angle.degrees(258+11.3+10));

        extensionMotor = ehub.getMotor(FFConstants.ExpansionPorts.EXTENSION_MOTOR_PORT);

        moveTurretAction = new MoveTurretAction(this);
        movePitchAction = new MovePitchAction(this);
        moveExtensionAction = new MoveExtensionAction(this);

        bucketServo = ehub.getServo(2);
        armServo = ehub.getServo(0);

        turretAngle = Angle.ZERO();
        prevTurretAngle = Angle.ZERO();
        turretVel = Angle.ZERO();
        this.chub = chub.getModule();
    }

    @Override
    public void initialize() {
        last = System.currentTimeMillis();
        moveTurretAction.submit();
        movePitchAction.submit();
        moveExtensionAction.submit();
        offset = getExtensionPosition();
        extensionMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        pitchMotor.getMotor().setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        turretMotor.getMotor().setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        initialPitch = getPitchPosition();
        timer = System.currentTimeMillis() + 100;
        MoveExtensionAction.P = -0.0045;
        turretAngle = Angle.degrees((turretPotentiometer.getAngle().degrees() * 1.066666669146008) - 9.13 + 12 - 15); // 49, 8
        initialTurret = (int) (turretAngle.degrees() * 11.0194174);
        //pitchMotor.getMotor().setTargetPosition(pitchMotor.getMotor().getCurrentPosition());
        //pitchMotor.getMotor().setPower(0.4);
        //pitchMotor.getMotor().setMode(DcMotor.RunMode.RUN_TO_POSITION);
    }

    @Override
    public void update() {
        if(System.currentTimeMillis() > timer && timer != 0) {
            pitchMotor.getMotor().setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            turretMotor.getMotor().setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            timer = 0;
        }
        long now = System.currentTimeMillis();
        double dt = (now - last) / 1000.0;

        turretAngle = Angle.degrees((turretPotentiometer.getAngle().degrees() * 1.066666669146008) - 9.13 + 5 - 2.5 - 2.5); // 49, 8

        Angle dTurret = MathUtils.getRotDist(prevTurretAngle, turretAngle);
        turretVel = Angle.degrees(dTurret.degrees() / dt);

        prevTurretAngle = turretAngle;

        synchronized (pitchFilter) {
            pitchFilter.add(turretAngle);
            if (pitchFilter.size() > TURRET_SMOOTHING) {
                pitchFilter.remove(0);
            }
            double sum = 0;
            for(Angle a : pitchFilter){
                sum += a.degrees();
            }
            sum = sum / pitchFilter.size();
            turretAngle = Angle.degrees(sum);
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
        angle = Angle.degrees(-angle.degrees());
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

    public Angle getFakePitchPos(){
        double pitchEncoder = getPitchMotorPos();
        double encoderAngle = pitchEncoder / -TICKS_PER_DEGREE_PANCAKES;
        return Angle.degrees(encoderAngle + initialPitch.degrees());
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
        bucketServo.enableServo();
        bucketServo.setPosition(pos);
    }

    public Angle getPitchTarget(){
        return finalPitchAngle;
    }

    public void setExtensionFloat() {
        extensionMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
    }

    public void setExtensionBrake() {
        extensionMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    }

    public double getExtensionTarget(){
        return moveExtensionAction.getTargetPos();
    }

    public void setExPIDActive(boolean active){
        moveExtensionAction.setPidActive(active);
    }

    public void setTurretPIDActive(boolean active){
        moveTurretAction.setEnabled(active);
    }

    public void tareExtensionMotor(){
        offset = extensionMotor.getMotor().getCurrentPosition();
    }

    public void tareExtensionMotor(int currentPos){
        int pos = extensionMotor.getMotor().getCurrentPosition();

        offset = pos - currentPos;
    }

    public SmartServo getBucketServo() {
        return bucketServo;
    }

    public void setArmPos(double pos){
        armServo.enableServo();
        this.armServo.setPosition(pos);
    }

    public void closeArm(){
        setArmPos(0.525);
    }

    public void openArm(){
        setArmPos(0.7);
    }

    public double getTurretMotorPower() {
        return turretMotor.getPower();
    }

    public Angle getTurretTarget() {
        return finalTurretAngle;
    }

    public SmartPotentiometer getTurretPotentiometer() {
        return turretPotentiometer;
    }

    public double getTurretEncoderPos(){
        return turretMotor.getMotor().getCurrentPosition() - initialTurret;
    }

    public SmartMotor getTurretMotor() {
        return turretMotor;
    }
}
