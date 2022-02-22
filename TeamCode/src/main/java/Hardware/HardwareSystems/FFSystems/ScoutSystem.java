package Hardware.HardwareSystems.FFSystems;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
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

        moveTurretAction = new MoveTurretAction(this);
        movePitchAction = new MovePitchAction(this);
        moveExtensionAction = new MoveExtensionAction(this);

        bucketServo = ehub.getServo(2);
        armServo = ehub.getServo(0);

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
        //moveExtensionAction.submit();
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
                if(moveExtensionAction.isAtTarget()){
                    transitionReady = true;
                }
                break;
            case OUTTAKING:
                intake.lock();
                if(intake.locked()){
                    transitionReady = true;
                }
                break;
            case TRANSFER:
                moveExtensionAction.setTargetPos(100);
                setBucketPreset();
                moveTurretAction.setTargetAngle(Angle.ZERO());
                movePitchAction.setTargetAngle(Angle.ZERO());
                if(moveExtensionAction.isAtTarget() && moveTurretAction.isAtTarget() && movePitchAction.isAtTarget()){
                    transitionReady = true;
                }
                break;
            case PRELOAD_ANGLE:
                moveExtensionAction.setTargetPos(200);
                moveTurretAction.setTargetAngle(scoutTarget.turretAngle);
                movePitchAction.setTargetAngle(scoutTarget.pitchAngle);
                if(forward){
                    setBucketScore();
                }else {
                    setBucketPreset();
                }
                if(moveExtensionAction.isAtTarget() && moveTurretAction.isAtTarget() && movePitchAction.isAtTarget()){
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
        //bucketServo.setPosition(0.1);
    }

    public void setBucketPreset(){
        //bucketServo.setPosition(0.4);
    }

    public void setBucketScore(){
        //bucketServo.setPosition(0.9);
    }

    public void setExtensionBrake() {
        extensionMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
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

    public void setScoutTarget(SCOUT_STATE state){
        this.cachedTarget = state;
    }

    public DcMotorEx getExtensionMotor() {
        return extensionMotor.getMotor();
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
