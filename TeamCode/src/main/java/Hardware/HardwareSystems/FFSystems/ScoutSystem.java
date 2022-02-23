package Hardware.HardwareSystems.FFSystems;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.util.RobotLog;

import java.util.HashMap;

import Hardware.HardwareSystems.FFSystems.Actions.MoveExtensionAction;
import Hardware.HardwareSystems.FFSystems.Actions.MovePitchAction;
import Hardware.HardwareSystems.FFSystems.Actions.MoveTurretAction;
import Hardware.HardwareSystems.FFSystems.Actions.ScoutTargets;
import Hardware.HardwareSystems.HardwareSystem;
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

    private final SmartMotor turretMotor, pitchMotor, extensionMotor;
    private final SmartServo bucketServo, armServo;
    private DigitalChannel bucketHall;

    private Angle turretAngle, prevTurretAngle, turretVel, initialPitch;

    private int initialTurret;

    private int offset = 0;

    private long timer = 0;

    private long last;

    private LynxModule chub;
    private IntakeSystem intake;

    private SCOUT_STATE currentState = SCOUT_STATE.HOME_IN_INTAKE, targetState = SCOUT_STATE.HOME_IN_INTAKE, cachedTarget = SCOUT_STATE.HOME_IN_INTAKE;
    private SCOUT_ALLIANCE scout_alliance = SCOUT_ALLIANCE.BLUE;
    private SCOUT_TARGET scout_target = SCOUT_TARGET.ALLIANCE_HIGH;
    private ScoutTargets.SCOUTTarget scoutTarget;

    private boolean transitionReady = true;

    public ScoutSystem(SmartLynxModule chub, SmartLynxModule ehub, IntakeSystem intake){

        //turretMotor = new SmartMotor(map.dcMotor.get("turretMotor"));
        turretMotor = chub.getMotor(1);

        pitchMotor = chub.getMotor(2);

        extensionMotor = chub.getMotor(3);

        bucketHall = chub.getDigitalController(0);

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
        initialPitch = Angle.ZERO();
        timer = System.currentTimeMillis() + 100;
        initialTurret = 0;
        scoutTarget = new ScoutTargets.SCOUTTarget(Angle.ZERO(), Angle.ZERO(), 0);
    }

    @Override
    public void update() {

        if(System.currentTimeMillis() > timer && timer != 0) {
            pitchMotor.getMotor().setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            turretMotor.getMotor().setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            timer = 0;
        }

        boolean forward = targetState.index > currentState.index;

        switch (currentState){
            case HOMING:
                break;
            case HOME_IN_INTAKE:
                moveExtensionAction.setTargetPos(0);
                setBucketIntakePos();
                openArm();
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
                moveExtensionAction.setTargetPos(100);
                setBucketPreset();
                moveTurretAction.setTargetAngle(Angle.ZERO());
                movePitchAction.setTargetAngle(Angle.ZERO());
                if(moveExtensionAction.isAtTarget() && moveTurretAction.isAtTarget() && movePitchAction.isAtTarget() && !bucketHall.getState()){
                    transitionReady = true;
                }
                break;
            case PRELOAD_ANGLE:
                moveExtensionAction.setTargetPos(300);
                moveTurretAction.setTargetAngle(scoutTarget.turretAngle);
                movePitchAction.setTargetAngle(scoutTarget.pitchAngle);
                boolean bucketReady = true;
                if(forward){
                    setBucketScore();
                }else {
                    bucketReady = !bucketHall.getState();
                    setBucketPreset();
                }
                if(moveExtensionAction.isAtTarget() && moveTurretAction.isAtTarget() && movePitchAction.isAtTarget() && bucketReady){
                    transitionReady = true;
                }
                break;
            case SCORE:
                moveExtensionAction.setTargetPos(scoutTarget.extension);
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
        return pitchMotor.getMotor().getCurrentPosition();
    }

    public void movePitchRaw(Angle angle){
        if(!angle.equals(finalPitchAngle)){
            finalPitchAngle = angle;
            movePitchAction.setTargetAngle(finalPitchAngle);
        }
    }

    public void moveExtensionRaw(double position){
        moveExtensionAction.setTargetPos(position);
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
        bucketServo.setPosition(0.5);
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
        setArmPos(0.8);
    }

    public void openArm(){
        setArmPos(1);
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
        this.cachedTarget = state;
    }

    public DcMotorEx getExtensionMotor() {
        return extensionMotor.getMotor();
    }

    public SCOUT_STATE getScoutTarget() {
        return cachedTarget;
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
