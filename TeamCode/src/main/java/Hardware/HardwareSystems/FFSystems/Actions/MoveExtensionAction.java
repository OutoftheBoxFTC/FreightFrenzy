package Hardware.HardwareSystems.FFSystems.Actions;

import Hardware.HardwareSystems.FFSystems.FFConstants;
import Hardware.HardwareSystems.FFSystems.TurretSystem;
import MathSystems.Angle;
import MathSystems.MathUtils;
import State.Action.Action;
import Utils.PID.PIDSystem;

public class MoveExtensionAction implements Action {
    public static double P = 0.01, I = 0, D = 0;

    private double targetPos;
    private TurretSystem system;
    private PIDSystem pid;

    public MoveExtensionAction(TurretSystem system){
        this.system = system;
        pid = new PIDSystem(P, I, D, 0.1);
        targetPos = 0;
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
        if(isAtTarget()){
            system.setExtensionMotorPower(0);
            return;
        }
        if(system.getPitchPosition().degrees() < -43){
            system.setExtensionMotorPower(0);
            return;
        }
        double power = pid.getCorrection(targetPos - system.getExtensionPosition());
        system.setExtensionMotorPower(MathUtils.signedMax(power, FFConstants.Extension.EXTENSION_KSTATIC));
    }

    @Override
    public void onEnd() {
        system.setPitchMotorPower(0);
    }

    @Override
    public boolean shouldDeactivate() {
        return false;
    }

    public boolean isAtTarget(){
        double error = targetPos - system.getExtensionPosition();
        return Math.abs(error) < 0.25;
    }
}
