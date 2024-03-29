package Hardware.HardwareSystems.FFSystems.Actions;

import com.acmerobotics.dashboard.config.Config;

import Hardware.HardwareSystems.FFSystems.FFConstants;
import Hardware.HardwareSystems.FFSystems.TurretSystem;
import MathSystems.Angle;
import MathSystems.MathUtils;
import State.Action.Action;
import Utils.PID.PIDSystem;
@Config
public class MoveExtensionAction implements Action {
    public static double P = -0.5, I = 0, D = 0;

    private double targetPos;
    private TurretSystem system;
    private PIDSystem pid;

    private boolean pidActive = true;

    public MoveExtensionAction(TurretSystem system){
        this.system = system;
        P = -0.5;
        pid = new PIDSystem(P, I, D, 0.1);
        targetPos = Double.NaN;
    }

    public void setTargetPos(double pos){
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
        if(Double.isNaN(targetPos) || isAtTarget()){
            system.setExtensionMotorPower(0);
            return;
        }
        double power = pid.getCorrection(targetPos - system.getExtensionPosition());
        if(Math.abs(power) > 0.6){
            //power = MathUtils.sign(power) * 0.6;
        }
        if(targetPos < system.getExtensionPosition()){
            double sign = MathUtils.sign(power);
            if(system.getExtensionPosition() < 200) {
                power = sign * Math.min(0.6, Math.abs(power));
            }
        }else{
            if(system.getExtensionPosition() < 160){
                double sign = MathUtils.sign(power);
                power = sign * Math.min(0.8, Math.abs(power));
            }
        }
        system.setExtensionMotorPower(MathUtils.signedMax(power, FFConstants.Extension.EXTENSION_KSTATIC));
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
        return Math.abs(error) < 20;
    }

    public double getTargetPos() {
        return targetPos;
    }

    public void setPidActive(boolean pidActive) {
        this.pidActive = pidActive;
    }
}
