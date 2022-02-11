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
    public static double P = -0.1, I = 0, D = 0;

    private Double targetPos = null;
    private TurretSystem system;
    private PIDSystem pid;

    private boolean enabled = true;

    public MoveTurretAction(TurretSystem system){
        this.system = system;
        pid = new PIDSystem(P, I, D, 0.1);
        targetPos = null;
    }

    public void setTargetAngle(Angle angle){
        targetPos = angle.degrees() * 11.0194174;
    }

    @Override
    public void update() {
        pid.setCoefficients(P, I, D);
        if(targetPos == null || !enabled){
            return;
        }
        if(isAtTarget()){
            system.setTurretMotorPower(0);
            return;
        }
        if(system.getTurretPosition().degrees() < -43){
            //system.setTurretMotorPower(0);
            //return;
        }
        double power = pid.getCorrection(targetPos - system.getTurretEncoderPos());
        system.setTurretMotorPower(MathUtils.signedMax(power, FFConstants.Turret.TURRET_KSTATIC));

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
        if(targetPos == null){
            return false;
        }
        return Math.abs(targetPos - system.getTurretEncoderPos()) < 25;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
