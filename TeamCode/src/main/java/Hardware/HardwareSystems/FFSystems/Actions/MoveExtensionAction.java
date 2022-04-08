package Hardware.HardwareSystems.FFSystems.Actions;

import com.acmerobotics.dashboard.config.Config;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

import Hardware.HardwareSystems.FFSystems.FFConstants;
import Hardware.HardwareSystems.FFSystems.ScoutSystem;
import MathSystems.MathUtils;
import State.Action.Action;
import Utils.PID.PIDSystem;
@Config
public class MoveExtensionAction implements Action {
    public static final double MM_PER_TICK = (58 * Math.PI) / (28 * (3.7 * 2));

    public static double P = 100, I = 0, D = 0;

    private double maxSpeed = 1;

    private double targetPos;
    private ScoutSystem system;
    private PIDSystem pid;

    private boolean pidActive = true;

    public MoveExtensionAction(ScoutSystem system){
        this.system = system;
        pid = new PIDSystem(P, I, D, 0.1);
        targetPos = Double.NaN;
    }

    public void setTargetPos(double pos, DistanceUnit unit){
        pos = unit.toMm(pos) / MM_PER_TICK;
        if(pos == this.targetPos){
            return;
        }
        targetPos = pos;
    }

    @Override
    public void update() {
        pid.setCoefficients(P, 0, D);
        if(!pidActive){
            return;
        }
        system.setExtensionBrake();
        if(Double.isNaN(targetPos) || isAtTarget() || (targetPos == 0 && system.getExtensionPosition() < 10)){
            if(system.getCurrentState() == ScoutSystem.SCOUT_STATE.HOME_IN_INTAKE) {
                system.setExtensionMotorPower(-1);
            }else{
                system.setExtensionMotorPower(0.1);
            }
            return;
        }
        double power = pid.getCorrection(targetPos - system.getExtensionPosition());

        if(targetPos == 0 && targetPos < system.getExtensionPosition()){
            power = 1 * Math.signum(power);
        }

        system.setExtensionMotorPower(MathUtils.signedMin(MathUtils.signedMax(power, 0.6), maxSpeed));
    }

    @Override
    public void onEnd() {
        system.setExtensionMotorPower(0);
    }

    @Override
    public boolean shouldDeactivate() {
        return false;
    }

    public boolean isAtTarget(){
        double error = targetPos - system.getExtensionPosition();
        return Math.abs(error) < DistanceUnit.INCH.toMm(1) / MM_PER_TICK ;
    }

    public double getTargetPos() {
        return targetPos;
    }

    public void setPidActive(boolean pidActive) {
        this.pidActive = pidActive;
    }

    public void setMaxSpeed(double maxSpeed) {
        this.maxSpeed = maxSpeed;
    }
}