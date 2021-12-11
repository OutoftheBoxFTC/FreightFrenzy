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
    public static double P = 0.04, I = 0.1, D = 0;

    private Angle targetAngle;
    private TurretSystem system;
    private PIDSystem pid;

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
        pid.setCoefficients(P, 0, D);
        if(targetAngle == null){
            return;
        }
        if(isAtTarget()){
            system.setTurretMotorPower(0);
            return;
        }
        if(system.getTurretPosition().degrees() < -43){
            system.setTurretMotorPower(0);
            return;
        }
        double power = pid.getCorrection(MathUtils.getRotDist(system.getTurretPosition(), targetAngle).degrees());
        system.setTurretMotorPower(MathUtils.signedMax(power, FFConstants.Turret.TURRET_KSTATIC));

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
        Angle error = MathUtils.getRotDist(system.getTurretPosition(), targetAngle);
        return Math.abs(error.degrees()) < 1;
    }
}
