package Hardware.HardwareSystems.FFSystems;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.hardware.rev.RevColorSensorV3;
import com.qualcomm.robotcore.hardware.ColorRangeSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.DigitalChannelImpl;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.VoltageUnit;

import Hardware.HardwareSystems.HardwareSystem;
import Hardware.Pipelines.LineFinderCamera;
import Hardware.SmartDevices.SmartLynxModule.SmartLynxModule;
import Hardware.SmartDevices.SmartMotor.SmartMotor;
import Hardware.SmartDevices.SmartServo.SmartServo;

public class IntakeSystem implements HardwareSystem {
    public ScoutSystem scoutSystem;

    private SmartMotor intakeMotor;

    private SmartServo intakeTransfer, cameraServo, panServo;

    private double power;
    private long timer = 0, timer2 = 0, startMid = 0;
    private double distance = 100, transferDistance = 100;
    private double hubVoltage = 12;
    private double duckPower = 0;
    private LynxModule expansionHub;

    private ColorRangeSensor sensor;
    private RevColorSensorV3 transferSensor;
    private DigitalChannel t1, t2;

    private INTAKE_STATE currentState = INTAKE_STATE.IDLE;

    private LineFinderCamera camera;
    private HardwareMap map;

    public IntakeSystem(SmartLynxModule chub, SmartLynxModule revHub, HardwareMap map){
        intakeMotor = chub.getMotor(0);
        intakeTransfer = revHub.getServo(0);
        cameraServo = revHub.getServo(5);
        panServo = revHub.getServo(1);
        expansionHub = revHub.getModule();
        this.map = map;
        sensor = map.get(ColorRangeSensor.class, "intakeSensor");
        transferSensor = map.get(RevColorSensorV3.class, "transferSensor");
        camera = new LineFinderCamera(map, this);

        t1 = revHub.getDigitalController(0);
        t2 = revHub.getDigitalController(1);

        t1.setMode(DigitalChannel.Mode.INPUT);
        t2.setMode(DigitalChannel.Mode.INPUT);
    }

    @Override
    public void initialize() {
        intakeMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    }

    @Override
    public void update() {
        transferDistance = transferSensor.getDistance(DistanceUnit.INCH);
        switch (currentState){
            case IDLE:
                intakeMotor.setPower(power);
                if(power == 0){
                    transferMid();
                }else{
                    transferFlipOut();
                }
                break;
            case TRANSFER_READY:
                transferMid();
                if(startMid == 0){
                    startMid = System.currentTimeMillis();
                }
                if(scoutSystem.getCurrentState() == ScoutSystem.SCOUT_STATE.HOME_IN_INTAKE && scoutSystem.isScoutIdle() && scoutSystem.getExtensionRealDistance(DistanceUnit.INCH) < 2) {
                    currentState = INTAKE_STATE.TRANSFER_UP;
                    if(System.currentTimeMillis() - startMid < 300) {
                        timer = System.currentTimeMillis() + (300 - (System.currentTimeMillis() - startMid));
                    }
                }
                break;
            case TRANSFER_UP:
                startMid = 0;
                transferFlipIn();
                if(System.currentTimeMillis() > timer){
                    currentState = INTAKE_STATE.TRANSFERRING;
                    timer2 = System.currentTimeMillis() + (150 + 200);
                }
                break;
            case TRANSFERRING:
                intakeMotor.setPower(-1);
                if((itemInIntake() && (timer2 < System.currentTimeMillis() - 500)) || timer2 < System.currentTimeMillis()){
                    //intakeMotor.setPower(0);
                    currentState = INTAKE_STATE.IDLE;
                    if(itemInIntake()) {
                        scoutSystem.setScoutTarget(ScoutSystem.SCOUT_STATE.SCORE);
                    }
                }
                break;
            case DUCK:
                intakeMotor.setPower(duckPower);
                break;

        }
    }

    public boolean itemInTransfer(){
        return transferDistance < 0.5;
    }

    public double getNormalizedColour(){
        return transferSensor.getLightDetected();
    }

    public void setPower(double power){
        this.power = power;
    }

    public double getDistance() {
        return transferDistance;
    }

    public void outtake() {
        setPower(-1);
    }

    public void intake(){
        setPower(1);
    }

    public void idleIntake(){
        setPower(0);
    }

    public void transferFlipOut(){
        intakeTransfer.setPosition(0.14);
    }

    public void transferFlipIn(){
        intakeTransfer.setPosition(0.87);
    }

    public void transferMid(){
        intakeTransfer.setPosition(0.75);
    }

    public SmartServo getCameraServo() {
        return cameraServo;
    }

    public SmartServo getPanServo() {
        return panServo;
    }

    public void moveCameraDown(){
        cameraServo.setPosition(0.9);
    }

    public void moveCameraSearch(){
        cameraServo.setPosition(0.85);
    }

    public void moveCameraUp(){
        cameraServo.setPosition(0.6);
    }

    public boolean itemInIntake(){
        return sensor.getDistance(DistanceUnit.INCH) < 3.1;
    }

    public void panCameraNeutral(){
        panServo.setPosition(0.5);
        cameraServo.setPosition(0.2483);
    }

    public void moveCameraLine(){
        cameraServo.setPosition(0.8);
        panServo.setPosition(0.5);
        camera.switchLine();
    }

    public void moveCameraInspection(){
        cameraServo.setPosition(0.2);
        panServo.setPosition(0.5);
        camera.switchLine();
    }

    public void moveCameraRedTSE(){
        panServo.setPosition(13/270.0);
        cameraServo.setPosition(0.6796);
        camera.switchTSE();
    }

    public void moveCameraBlueTSE(){
        panServo.setPosition(243.7563/270.0);
        cameraServo.setPosition(0.66841);
        camera.switchTSE();
    }

    public void setDuckPower(double duckPower) {
        double scale = 12 / expansionHub.getInputVoltage(VoltageUnit.VOLTS);
        this.duckPower = (duckPower * scale);
    }

    public void startTransfer(){
        if(currentState == INTAKE_STATE.IDLE) {
            currentState = INTAKE_STATE.TRANSFER_READY;
            intakeMotor.setPower(0);
        }
    }

    public double getBucketSensorDistance(){
        return sensor.getDistance(DistanceUnit.INCH);
    }

    public void spinDuckBlue(){
        setPower(0.48);
    }

    public void spinDuckRed(){
        setDuckPower(-0.48);
    }

    public void setZoom(double zoom){
        camera.getLinePipeline().setZoomFactor(zoom);
    }

    public LineFinderCamera getCamera() {
        return camera;
    }

    public SmartMotor getIntakeMotor() {
        return intakeMotor;
    }

    public INTAKE_STATE getCurrentState() {
        return currentState;
    }

    public enum INTAKE_STATE{
        IDLE,
        TRANSFER_READY,
        TRANSFER_UP,
        TRANSFERRING,
        DUCK
    }
}