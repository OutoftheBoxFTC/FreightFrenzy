package Hardware.HardwareSystems.FFSystems;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

import java.util.HashMap;

import Hardware.HardwareSystems.FFSystems.Actions.MoveExtensionAction;
import Hardware.HardwareSystems.FFSystems.Actions.MovePitchAction;
import Hardware.HardwareSystems.FFSystems.Actions.MoveTurretAction;
import Hardware.HardwareSystems.FFSystems.Actions.ScoutTargets;
import Hardware.HardwareSystems.HardwareSystem;
import Hardware.SmartDevices.SmartAnalogInput.SmartPotentiometer;
import Hardware.SmartDevices.SmartLynxModule.SmartLynxModule;
import Hardware.SmartDevices.SmartMotor.SmartMotor;
import Hardware.SmartDevices.SmartServo.SmartServo;
import MathSystems.Angle;
import MathSystems.MathUtils;

@Config
public class ScoutSystem implements HardwareSystem {
    public static int TURRET_SMOOTHING = 5;
    private Angle finalTurretAngle = Angle.ZERO(), finalPitchAngle = Angle.ZERO();

    private final MoveTurretAction moveTurretAction;
    private final MovePitchAction movePitchAction;
    private final MoveExtensionAction moveExtensionAction;

    private SmartPotentiometer pitchPot;

    private final SmartMotor turretMotor, pitchMotor, extensionMotor;
    private final SmartServo bucketServo, armServo;
    private DigitalChannel bucketHall;

    private Angle turretAngle, prevTurretAngle, turretVel, initialPitch;

    private int initialTurret;

    private int offset = 0;

    private long timer = 0;

    private double extensionPreload = 15;

    private boolean forward = false;

    private long last;

    private LynxModule chub;
    private IntakeSystem intake;

    private SCOUT_STATE currentState = SCOUT_STATE.HOME_IN_INTAKE, targetState = SCOUT_STATE.HOME_IN_INTAKE, cachedTarget = SCOUT_STATE.HOME_IN_INTAKE;
    private SCOUT_ALLIANCE scout_alliance = SCOUT_ALLIANCE.BLUE;
    private SCOUT_TARGET scout_target = SCOUT_TARGET.ALLIANCE_HIGH;
    private ScoutTargets.SCOUTTarget scoutTarget;

    private boolean transitionReady = true;

    private boolean auto = false;

    public ScoutSystem(SmartLynxModule chub, SmartLynxModule ehub, IntakeSystem intake){

        //turretMotor = new SmartMotor(map.dcMotor.get("turretMotor"));
        turretMotor = chub.getMotor(1);

        pitchMotor = chub.getMotor(2);

        extensionMotor = chub.getMotor(3);

        bucketHall = chub.getDigitalController(0);

        pitchPot = new SmartPotentiometer(chub.getAnalogInput(1), 463.9175);

        moveTurretAction = new MoveTurretAction(this);
        movePitchAction = new MovePitchAction(this);
        moveExtensionAction = new MoveExtensionAction(this);

        bucketServo = chub.getServo(4);
        armServo = chub.getServo(5);

        turretAngle = Angle.ZERO();
        prevTurretAngle = Angle.ZERO();
        turretVel = Angle.ZERO();
        this.chub = chub.getModule();

        this.intake = intake;
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
        initialPitch = Angle.degrees(-(pitchPot.getAngle().degrees() - 226.61668   ));
        timer = System.currentTimeMillis() + 100;
        initialTurret = 0;
        scoutTarget = new ScoutTargets.SCOUTTarget(Angle.ZERO(), Angle.ZERO(), 0);
    }

    @Override
    public void update() {
        //RobotLog.i(initialPitch.degrees()+"");
        if(System.currentTimeMillis() > timer && timer != 0) {
            pitchMotor.getMotor().setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            turretMotor.getMotor().setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            timer = 0;
        }

        switch (currentState){
            case HOMING:
                break;
            case HOME_IN_INTAKE:
                moveExtensionAction.setTargetPos(0, DistanceUnit.INCH);
                setBucketIntakePos();
                if(intake.itemInIntake()){
                    closeArm();
                }else{
                    openArm();
                }
                if(moveExtensionAction.isAtTarget()){
                    transitionReady = true;
                }
                break;
            case OUTTAKING:
                intake.lock();
                closeArm();
                if(intake.locked()){
                    transitionReady = true;
                }
                break;
            case TRANSFER:
                if(forward) {
                    if(!moveExtensionAction.isAtTarget()) {
                        bucketServo.disableServo();
                    }
                }
                moveExtensionAction.setTargetPos(9, DistanceUnit.INCH);
                setBucketPreset();
                moveTurretAction.setTargetAngle(Angle.ZERO());
                movePitchAction.setTargetAngle(Angle.degrees(9.5));
                if(moveExtensionAction.isAtTarget()){
                    bucketServo.enableServo();
                    setBucketPreset();
                }
                if(moveExtensionAction.isAtTarget() && moveTurretAction.isAtTarget() && movePitchAction.isAtTarget() && !bucketHall.getState()){
                    transitionReady = true;
                }
                break;
            case PRELOAD_ANGLE:
                if(forward){
                    moveExtensionAction.setTargetPos(extensionPreload, DistanceUnit.INCH);
                    bucketServo.enableServo();

                    if(moveExtensionAction.isAtTarget() && moveTurretAction.isAtTarget() && movePitchAction.isAtTarget()){
                        transitionReady = true;
                    }

                    setBucketScore();
                    moveTurretAction.setTargetAngle(scoutTarget.turretAngle);
                    movePitchAction.setTargetAngle(scoutTarget.pitchAngle);
                }else{
                    moveExtensionAction.setTargetPos(bucketHall.getState() ? extensionPreload : 9, DistanceUnit.INCH);
                    if(getExtensionRealDistance(DistanceUnit.INCH) < 33){
                        moveTurretAction.setTargetAngle(Angle.ZERO());
                        movePitchAction.setTargetAngle(Angle.degrees(9.5));
                    }

                    if(moveExtensionAction.isAtTarget() && moveTurretAction.isAtTarget() && movePitchAction.isAtTarget() && getExtensionRealDistance(DistanceUnit.INCH) < 11){
                        transitionReady = true;
                    }

                    setBucketPreset();
                }
                if(this.auto){
                    MoveExtensionAction.P = 0.1;
                }
                break;
            case SCORE:
                if(this.auto){
                    MoveExtensionAction.P = 0.01;
                }
                moveExtensionAction.setTargetPos(scoutTarget.extension, DistanceUnit.INCH);
                setBucketScore();
                if(moveExtensionAction.isAtTarget()){
                    transitionReady = true;
                }
                break;
        }

        if(transitionReady){
            if(cachedTarget != targetState){
                targetState = cachedTarget;
            }else {
                if(currentState != targetState)
                    currentState = SCOUT_STATE.fromValue((int) ((Math.signum(targetState.index - currentState.index)) + currentState.index));
            }
            if(currentState != targetState) {
                transitionReady = false;
            }
        }

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

    public double getExtensionMotorPower(){
        return extensionMotor.getPower();
    }

    public int getPitchMotorPos(){
        return (int) (pitchMotor.getMotor().getCurrentPosition() + (initialPitch.degrees() * 18.189));
    }

    public void movePitchRaw(Angle angle){
        if(!angle.equals(finalPitchAngle)){
            finalPitchAngle = angle;
            movePitchAction.setTargetAngle(finalPitchAngle);
        }
    }

    public void moveExtensionRaw(double position, DistanceUnit unit){
        moveExtensionAction.setTargetPos(position, unit);
    }

    public int getExtensionPosition(){
        return extensionMotor.getMotor().getCurrentPosition() - offset;
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

    public void setBucketIntakePos(){
        bucketServo.setPosition(0.13);
    }

    public void setBucketPreset(){
        bucketServo.setPosition(0.48);
    }

    public void setBucketScore(){
        bucketServo.setPosition(1);
    }

    public void setExtensionBrake() {
        extensionMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    }

    public void setArmPos(double pos){
        this.armServo.setPosition(pos);
    }

    public void closeArm(){
        setArmPos(0.55);
    }

    public void openArm(){
        setArmPos(0.75);
    }

    public double getTurretEncoderPos(){
        return turretMotor.getMotor().getCurrentPosition() - initialTurret;
    }

    public void setScoutAlliance(SCOUT_ALLIANCE scout_alliance) {
        this.scout_alliance = scout_alliance;
        scoutTarget = ScoutTargets.getTarget(scout_alliance, scout_target);
    }

    public void setScoutFieldTarget(SCOUT_TARGET scout_target) {
        this.scout_target = scout_target;
        scoutTarget = ScoutTargets.getTarget(scout_alliance, scout_target);
    }

    public boolean isScoutIdle(){
        return currentState == targetState && transitionReady;
    }

    public SCOUT_STATE getCurrentState() {
        return currentState;
    }

    public void setScoutTarget(SCOUT_STATE state){
        forward = state.index > currentState.index;
        this.cachedTarget = state;
    }

    public DcMotorEx getExtensionMotor() {
        return extensionMotor.getMotor();
    }

    public SCOUT_STATE getScoutTarget() {
        return cachedTarget;
    }

    public SmartPotentiometer getPitchPot() {
        return pitchPot;
    }

    public SmartServo getBucketServo() {
        return bucketServo;
    }

    public void bypassSetState(SCOUT_STATE state){
        this.cachedTarget = state;
        this.targetState = state;
        this.currentState = state;
        transitionReady = false;
    }

    public void setExtensionPreload(double distance) {
        this.extensionPreload = distance;
    }

    public void setAuto(boolean auto) {
        this.auto = auto;
    }

    public double getExtensionRealDistance(DistanceUnit unit){
        return unit.fromMm(getExtensionPosition() * MoveExtensionAction.MM_PER_TICK);
    }

    public enum SCOUT_STATE {
        HOMING(-1),
        HOME_IN_INTAKE(0),
        OUTTAKING(1),
        TRANSFER(2),
        PRELOAD_ANGLE(3),
        SCORE(4);

        private static final HashMap<Integer, SCOUT_STATE> states = new HashMap<>();
        static{
            for(SCOUT_STATE state : values()){
                states.put(state.index, state);
            }
        }

        public static SCOUT_STATE fromValue(int value){
            return states.get(value);
        }

        public final int index;

        SCOUT_STATE(int index) {
            this.index = index;
        }
    }

    public enum SCOUT_TARGET{
        ALLIANCE_HIGH,
        ALLIANCE_MID,
        ALLIANCE_LOW,
        SHARED
    }

    public enum SCOUT_ALLIANCE{
        RED,
        BLUE
    }
}
