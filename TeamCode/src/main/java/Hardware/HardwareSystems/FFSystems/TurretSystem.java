package Hardware.HardwareSystems.FFSystems;

import Hardware.HardwareSystems.FFSystems.Actions.MovePitchAction;
import Hardware.HardwareSystems.FFSystems.Actions.MoveTurretAction;
import Hardware.HardwareSystems.HardwareSystem;
import Hardware.SmartDevices.SmartAnalogInput.SmartPotentiometer;
import Hardware.SmartDevices.SmartLynxModule.SmartLynxModule;
import Hardware.SmartDevices.SmartMotor.SmartMotor;
import MathSystems.Angle;
import MathSystems.MathUtils;
import State.Action.ActionController;
import State.Action.ActionQueue;

public class TurretSystem implements HardwareSystem {
    private Angle finalTurretAngle = Angle.ZERO(), finalPitchAngle = Angle.ZERO();

    private final MoveTurretAction moveTurretAction;

    private final SmartMotor turretMotor, pitchMotor;
    private final SmartPotentiometer turretPotentiometer, pitchPotentiometer;

    private Angle turretAngle, prevTurretAngle, turretVel;
    private long last;

    public TurretSystem(SmartLynxModule module){
        turretMotor = module.getMotor(FFConstants.ExpansionPorts.TURRET_MOTOR_PORT);
        turretPotentiometer = new SmartPotentiometer(module.getAnalogInput(FFConstants.ExpansionPorts.TURRET_POTENTIOMETER_PORT),
                FFConstants.Turret.TURRET_MIN_ANGLE, FFConstants.Turret.TURRET_MAX_ANGLE);

        pitchMotor = module.getMotor(FFConstants.ExpansionPorts.PITCH_MOTOR_PORT);
        pitchPotentiometer = new SmartPotentiometer(module.getAnalogInput(FFConstants.ExpansionPorts.PITCH_POTENTIOMETER_PORT),
                FFConstants.Pitch.PITCH_MIN_ANGLE, FFConstants.Pitch.PITCH_MAX_ANGLE);

        moveTurretAction = new MoveTurretAction(this);

        turretAngle = Angle.ZERO();
        prevTurretAngle = Angle.ZERO();
        turretVel = Angle.ZERO();
    }

    @Override
    public void initialize() {
        last = System.currentTimeMillis();
        moveTurretAction.submit();
    }

    @Override
    public void update() {
        long now = System.currentTimeMillis();
        double dt = (now - last) / 1000.0;

        turretAngle = turretPotentiometer.getAngle();
        Angle dTurret = MathUtils.getRotDist(prevTurretAngle, turretAngle);
        turretVel = Angle.degrees(dTurret.degrees() / dt);

        prevTurretAngle = turretAngle;

        last = now;
    }

    public Angle getTurretPosition(){
        return turretPotentiometer.getAngle();
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

    public void setTurretPowerRaw(double power){
        turretMotor.setPower(power);
    }

    public void setPitchMotorPower(double pitchPower){
        pitchMotor.setPower(pitchPower);
    }

    public Angle getPitchPosition(){
        return pitchPotentiometer.getAngle();
    }

    public void movePitchRaw(Angle angle){
        if(!angle.equals(finalPitchAngle)){
            finalPitchAngle = angle;
            moveTurretAction.setTargetAngle(angle);
        }
    }


}
