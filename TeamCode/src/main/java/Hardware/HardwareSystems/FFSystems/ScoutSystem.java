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
    public static double EXTENSION_START_ANGLE = 30;

    private Angle finalTurretAngle = Angle.ZERO(), finalPitchAngle = Angle.ZERO();

    private final MoveTurretAction moveTurretAction;
    private final MovePitchAction movePitchAction;
    private final MoveExtensionAction moveExtensionAction;

    private SmartPotentiometer pitchPot;
    private SmartPotentiometer turretPot;

    private final SmartMotor turretMotor, pitchMotor, extensionMotor;
    private final SmartServo bucketServo, armServo, slideLockServo;
    private DigitalChannel bucketHall;

    private Angle turretAngle, prevTurretAngle, turretVel, initialPitch;

    private int initialTurret;

    private int offset = 0;

    private long timer = 0;

    private double extensionPreload = 8, extensionScoreOffset = 0, turretOffset;

    private boolean forward = false;

    private long last;
    private long timer2 = 0;

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
        intake.scoutSystem = this;

        turretMotor = chub.getMotor(1);

        pitchMotor = chub.getMotor(2);

        extensionMotor = chub.getMotor(3);

        slideLockServo = chub.getServo(1);

        bucketHall = chub.getDigitalController(0);

        pitchPot = new SmartPotentiometer(chub.getAnalogInput(1), 463.9175);

        turretPot = new SmartPotentiometer(ehub.getAnalogInput(3), 360, 5);

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
        initialPitch = Angle.degrees(-(pitchPot.getAngle().degrees() - 176));
        timer = System.currentTimeMillis() + 100;
        //initialTurret = -((int) (getTurretPotAngle().degrees() * 8.07333333))   ;
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
                moveExtensionAction.setMaxSpeed(1);
                movePitchAction.setTargetAngle(Angle.degrees(7));
                if(intake.itemInIntake()){
                    closeArm();
                }else{
                    if(getExtensionRealDistance(DistanceUnit.INCH) < 1) {
                        openArm();
                        slideLockServo.enableServo();
                        slideLockServo.setPosition(0.2);
                    }
                }
                if(!forward && !moveExtensionAction.isAtTarget()){
                    //intake.outtake();
                }
                if(moveExtensionAction.isAtTarget()){
                    setBucketIntakePos();
                    if(timer2 == 0)
                        timer2 = System.currentTimeMillis() + 100;
                }else{
                    timer2 = 0;
                }
                transitionReady = true;
                break;
            case OUTTAKING:
                timer2 = 0;
                if(forward) {
                    intake.setPower(0.1);
                    moveExtensionAction.setMaxSpeed(1);
                    closeArm();
                    moveExtensionAction.setTargetPos(5, DistanceUnit.INCH);
                    if(moveExtensionAction.isAtTarget()){
                        transitionReady = true;
                    }
                }else{
                    moveExtensionAction.setTargetPos(0, DistanceUnit.INCH);
                    moveExtensionAction.setMaxSpeed(1);
                    transitionReady = true;
                }
                break;
            case TRANSFER:
                if(forward) {
                    moveExtensionAction.setTargetPos(5, DistanceUnit.INCH);
                    if(!auto) {
                        //movePitchAction.setTargetAngle(scoutTarget.pitchAngle);
                    }
                    transitionReady = true;
                }else{
                    setBucketIntakePos();
                    moveExtensionAction.setTargetPos(2, DistanceUnit.INCH);
                    movePitchAction.setTargetAngle(Angle.degrees(4));
                }
                moveTurretAction.setTargetAngle(Angle.ZERO());
                if(moveExtensionAction.isAtTarget() && moveTurretAction.isAtTarget() && movePitchAction.isAtTarget()
                        //&& !bucketHall.getState()
                ){
                    transitionReady = true;
                }
                break;
            case PRELOAD_ANGLE:
                if(forward) {
                    intake.setPower(0);
                }
                if(forward){
                    moveTurretAction.setTargetAngle(Angle.degrees(scoutTarget.turretAngle.degrees() + turretOffset));
                    Angle error = Angle.degrees(Math.abs(moveTurretAction.getTargetPos() - getTurretEncoderPos()) / MoveTurretAction.TURRET_CONSTANT);
                    //RobotLog.ii("Error", error.degrees()+" | " + moveTurretAction.getTargetPos() + " | " + getTurretEncoderPos());
                    if(Math.abs(error.degrees()) < EXTENSION_START_ANGLE && !auto){
                        moveExtensionAction.setTargetPos(scoutTarget.extension, DistanceUnit.INCH);
                        if(!auto) {
                            movePitchAction.setTargetAngle(scoutTarget.pitchAngle);
                        }
                        slideLockServo.disableServo();
                    }
                    if(auto){
                        moveExtensionAction.setTargetPos(extensionPreload, DistanceUnit.INCH);
                    }

                    if(moveExtensionAction.isAtTarget() && moveTurretAction.isAtTarget() && movePitchAction.isAtTarget()) {
                        transitionReady = true;
                    }
                    if(targetState != SCOUT_STATE.PRELOAD_ANGLE && getExtensionRealDistance(DistanceUnit.INCH) > 15){
                        setBucketScore();
                    }
                }else{
                    //moveExtensionAction.setTargetPos(bucketHall.getState() ? extensionPreload : 9, DistanceUnit.INCH);
                    moveExtensionAction.setTargetPos(13, DistanceUnit.INCH);
                    slideLockServo.enableServo();
                    slideLockServo.setPosition(0.6);
                    if(getExtensionRealDistance(DistanceUnit.INCH) < 18){
                        moveTurretAction.setTargetAngle(Angle.ZERO());
                        if(getFieldTarget() != SCOUT_TARGET.PASSTHROUGH)
                            movePitchAction.setTargetAngle(Angle.degrees(7));
                    }

                    if(moveExtensionAction.isAtTarget() && moveTurretAction.isAtTarget() && movePitchAction.isAtTarget() && getExtensionRealDistance(DistanceUnit.INCH) < 15){
                        transitionReady = true;
                    }

                    closeArm();

                    setBucketIntakePos();
                }
                moveExtensionAction.setMaxSpeed(1);
                break;
            case SCORE:
                if(this.auto){
                    if(forward) {
                        moveExtensionAction.setMaxSpeed(1);
                    }else{
                        moveExtensionAction.setMaxSpeed(1);
                    }
                }
                moveExtensionAction.setTargetPos(scoutTarget.extension+extensionScoreOffset, DistanceUnit.INCH);
                moveTurretAction.setTargetAngle(Angle.degrees(scoutTarget.turretAngle.degrees() + turretOffset));
                movePitchAction.setTargetAngle(scoutTarget.pitchAngle);

                if(getFieldTarget() == SCOUT_TARGET.CAP_GRAB || getFieldTarget() == SCOUT_TARGET.CAP_PLACE){
                    movePitchAction.setPIDActive(false);
                    moveExtensionAction.setPidActive(false);
                }else{
                    movePitchAction.setPIDActive(true);
                    moveExtensionAction.setPidActive(true);
                }

                if(getFieldTarget() == SCOUT_TARGET.ALLIANCE_LOW || getFieldTarget() == SCOUT_TARGET.ALLIANCE_MID){
                    setBucketAngleAuto();
                }else {
                    setBucketScore();
                }
                if(moveExtensionAction.isAtTarget()){
                    transitionReady = true;
                }
                break;
        }

        if(transitionReady){
            if(cachedTarget != targetState){
                targetState = cachedTarget;
            }else {
                if(currentState != targetState) {
                    currentState = SCOUT_STATE.fromValue((int) ((Math.signum(targetState.index - currentState.index)) + currentState.index));
                    transitionReady = false;
                }
            }

        }

    }

    public Angle getTurretPosition(){
        return Angle.degrees(getTurretEncoderPos() / 8.07333333);
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
        bucketServo.setPosition(0.23);
    }

    public void setBucketPreset(){
        bucketServo.setPosition(0.48);
    }

    public void setBucketAngleAuto(){
        bucketServo.setPosition(0.9);
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
        setArmPos(0.22);
    }

    public void openArm(){
        setArmPos(0.52);
    }

    public void kickArm(){
        setArmPos(0.88);
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

    public SCOUT_STATE   getCurrentState() {
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

    public SCOUT_TARGET getFieldTarget(){
        return this.scout_target;
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

    public void setExtensionScoreOffset(double extensionScoreOffset) {
        this.extensionScoreOffset = extensionScoreOffset;
    }

    public double getExtensionTarget(){
        return scoutTarget.extension+extensionScoreOffset;
    }

    public void moveExtensionScoreOffset(double offset){
        this.extensionScoreOffset += offset;
    }

    public void moveTurretOffset(double offset){
        this.turretOffset += offset;
    }

    public void setTurretOffset(double offset){
        this.turretOffset = offset;
    }

    public void disableScout(){
        moveTurretAction.deactivateNow();
        moveExtensionAction.deactivateNow();
        movePitchAction.deactivateNow();
    }

    public void disableTurretPID(){
        moveTurretAction.setEnabled(false);
    }

    public void enableTurretPID(){
        moveTurretAction.setEnabled(true);
    }

    public void disableExtensionPID(){
        moveExtensionAction.setPidActive(false);
    }

    public void enableExtensionPID(){
        moveExtensionAction.setPidActive(true);
    }

    public Angle getTurretPotAngle(){
        return Angle.degrees(this.turretPot.getAngle().degrees() - 127);
    }

    public SmartServo getSlideLockServo() {
        return slideLockServo;
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
        SHARED,
        PASSTHROUGH,
        CAP_GRAB,
        CAP_PLACE,
        LONG_PASSTHROUGH
    }

    public enum SCOUT_ALLIANCE{
        RED,
        BLUE
    }
}
