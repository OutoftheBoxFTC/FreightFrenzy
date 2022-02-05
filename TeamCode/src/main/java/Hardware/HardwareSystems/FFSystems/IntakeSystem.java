package Hardware.HardwareSystems.FFSystems;

import com.acmerobotics.dashboard.FtcDashboard;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.hardware.rev.RevColorSensorV3;
import com.qualcomm.robotcore.hardware.ColorRangeSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.VoltageUnit;

import Hardware.FFHardwareController;
import Hardware.HardwareSystems.HardwareSystem;
import Hardware.SmartDevices.SmartLynxModule.SmartLynxModule;
import Hardware.SmartDevices.SmartMotor.SmartMotor;
import State.Action.Action;
import State.Action.ActionController;
import State.Action.ActionQueue;
import State.Action.InstantAction;
import State.Action.StandardActions.DelayAction;

public class IntakeSystem implements HardwareSystem {
    private SmartMotor intakeMotor;
    private DigitalChannel intakeStop;
    private RevColorSensorV3 bucketSensor;
    private double power;
    private long timer = 0;
    private double distance = 0;
    private double hubVoltage = 12;
    private LynxModule expansionHub;

    public IntakeSystem(SmartLynxModule chub, SmartLynxModule revHub, HardwareMap map){
        intakeMotor = revHub.getMotor(FFConstants.ExpansionPorts.INTAKE_MOTOR_PORT);
        intakeStop = chub.getDigitalController(1);
        bucketSensor = map.get(RevColorSensorV3.class, "bucketSensor");
        expansionHub = revHub.getModule();
    }

    @Override
    public void initialize() {
        intakeStop.setMode(DigitalChannel.Mode.INPUT);
        intakeMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    }

    @Override
    public void update() {
        intakeMotor.setPower(power);
        distance = bucketSensor.getDistance(DistanceUnit.MM);
    }

    public void setPower(double power){
        this.power = power;
    }

    public void setPowerNormalized(double power){
        hubVoltage = expansionHub.getInputVoltage(VoltageUnit.VOLTS);
        double factor = 12.0 / hubVoltage;
        setPower(power * factor);
    }

    public double getIntakeCurrent(){
        return intakeMotor.getMotor().getCurrent(CurrentUnit.AMPS);
    }

    public boolean getIntakeStop(){
        return !intakeStop.getState();
    }

    public boolean inIntake() {
        return distance < 30 && distance != 0;
    }

    public double getDistance() {
        return distance;
    }

    public ActionQueue getOuttakeAction(FFHardwareController hardware){
        ActionQueue queue = new ActionQueue();
        queue.submitAction(new InstantAction() {
            @Override
            public void update() {
                hardware.getTurretSystem().setExtensionMotorPower(-0.1);
                hardware.getTurretSystem().setBucketPosRaw(0.1);
                hardware.getTurretSystem().closeArm();
            }
        });
        queue.submitAction(new DelayAction(75));
        queue.submitAction(new InstantAction() {
            @Override
            public void update() {
                hardware.getIntakeSystem().setPower(-1);
                hardware.getDuckSystem().setDuckPower(1);
                ActionController.addAction(new Action() {
                    long timer = 0;

                    @Override
                    public void initialize() {
                        timer = System.currentTimeMillis() + 50;
                    }

                    @Override
                    public void update() {
                        if(System.currentTimeMillis() > timer){
                            hardware.getTurretSystem().openArm();
                            ActionController.getInstance().terminateAction(this);
                        }
                    }
                });
            }
        });
        queue.submitAction(new Action() {
            @Override
            public void update() {

            }

            @Override
            public boolean shouldDeactivate() {
                RobotLog.ii("Current", hardware.getIntakeSystem().getIntakeCurrent()+"");
                return hardware.getIntakeSystem().getIntakeCurrent() < 0.5;
            }
        });
        queue.submitAction(new InstantAction() {
            @Override
            public void update() {
                hardware.getIntakeSystem().setPower(1);
                hardware.getTurretSystem().openArm();
                hardware.getTurretSystem().setBucketPosRaw(0.4);
                hardware.getTurretSystem().moveExtensionRaw(200);
            }
        });
        queue.submitAction(new DelayAction(75));
        queue.submitAction(new InstantAction() {
            @Override
            public void update() {
                hardware.getTurretSystem().closeArm();
                hardware.getDuckSystem().setDuckPower(0);
            }
        });
        return queue;
    }
}
