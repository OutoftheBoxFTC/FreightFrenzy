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
    public static double P = -0.015, I = 0, D = 0;

    private double targetPos;
    private TurretSystem system;
    private PIDSystem pid;

    public MoveExtensionAction(TurretSystem system){
        this.system = system;
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
            if(system.getExtensionPosition() < 300) {
                power = sign * Math.min(0.7, Math.abs(power));
            }else{
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
        return Math.abs(error) < 10;
    }
}
