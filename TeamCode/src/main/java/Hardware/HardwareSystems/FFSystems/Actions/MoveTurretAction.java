package Hardware.HardwareSystems.FFSystems.Actions;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;

import Hardware.HardwareSystems.FFSystems.FFConstants;
import Hardware.HardwareSystems.FFSystems.TurretSystem;
import MathSystems.Angle;
import MathSystems.MathUtils;
import State.Action.Action;
import Utils.PID.PIDSystem;
@Config
public class MoveTurretAction implements Action {
    public static double P = 0.04, I = 1, D = 0;

    private Angle targetAngle;
    private TurretSystem system;
    private PIDSystem pid;

    private boolean enabled = true;

    public MoveTurretAction(TurretSystem system){
        this.system = system;
        pid = new PIDSystem(P, I, D, 0.1);
        targetAngle = null;
    }

    public void setTargetAngle(Angle angle){
        if(targetAngle != null && angle.equals(targetAngle)){
            return;
        }
        targetAngle = angle;
    }

    @Override
    public void update() {
        pid.setCoefficients(P, I, D);
        if(targetAngle == null || !enabled){
            return;
        }
        if(targetAngle.degrees() < -50){
            targetAngle = Angle.degrees(-47);
        }
        if(isAtTarget()){
            system.setTurretMotorPower(0);
            return;
        }
        if(system.getTurretPosition().degrees() < -43){
            //system.setTurretMotorPower(0);
            //return;
        }
        double power = pid.getCorrection(MathUtils.getRotDist(system.getTurretPosition(), targetAngle).degrees());
        if(Math.abs(system.getTurretPosition().degrees()) < 5) {
            system.setTurretMotorPower(MathUtils.signedMax(power, 0.3));
        }else{
            system.setTurretMotorPower(MathUtils.signedMax(power, FFConstants.Turret.TURRET_KSTATIC));
        }

        FtcDashboard.getInstance().getTelemetry().addData("Error", MathUtils.getRotDist(system.getTurretPosition(), targetAngle).degrees());
        FtcDashboard.getInstance().getTelemetry().addData("Corr", pid.getCorrection(MathUtils.getRotDist(system.getTurretPosition(), targetAngle).degrees()));
        FtcDashboard.getInstance().getTelemetry().addData("Target", targetAngle.degrees());
    }

    @Override
    public void onEnd() {
        system.setTurretMotorPower(0);
    }

    @Override
    public boolean shouldDeactivate() {
        return false;
    }

    public boolean isAtTarget(){
        if(targetAngle == null){
            return false;
        }
        Angle error = MathUtils.getRotDist(system.getTurretPosition(), targetAngle);
        double tol = 1;
        if(Math.abs(system.getTurretPosition().degrees()) > 5){
            tol = 3;
        }
        return Math.abs(error.degrees()) < tol;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
