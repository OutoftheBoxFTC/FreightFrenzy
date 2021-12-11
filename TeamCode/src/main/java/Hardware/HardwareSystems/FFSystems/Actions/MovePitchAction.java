package Hardware.HardwareSystems.FFSystems.Actions;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;

import Hardware.HardwareSystems.FFSystems.FFConstants;
import Hardware.HardwareSystems.FFSystems.TurretSystem;
import MathSystems.Angle;
import MathSystems.MathUtils;
import MathSystems.Profiling.AccelProfile;
import MathSystems.Profiling.AccelProfileProvider;
import MathSystems.Profiling.AccelTimeState;
import MathSystems.Profiling.ConstantAccelProfiler;
import State.Action.Action;
import Utils.PID.PIDFSystem;
import Utils.PID.PIDSystem;
@Config
public class MovePitchAction implements Action {
    public static double P = 0.04, I = 0, D = 0, F = 0.1;

    private Angle targetAngle;
    private TurretSystem system;
    private PIDFSystem pid;

    public MovePitchAction(TurretSystem system){
        this.system = system;
        pid = new PIDFSystem(P, I, D, 0.1);
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
        pid.setCoefficients(P, 0, D, F);
        if(targetAngle == null){
            return;
        }
        if(isAtTarget()){
            system.setPitchMotorPower(0);
            return;
        }
        if(system.getPitchPosition().degrees() < -60){
            system.setPitchMotorPower(0);
            return;
        }
        double power = pid.getCorrection(MathUtils.getRotDist(system.getPitchPosition(), targetAngle).degrees(), 1);
        system.setPitchMotorPower(MathUtils.signedMax(power, FFConstants.Pitch.PITCH_KSTATIC));
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
        Angle error = MathUtils.getRotDist(system.getPitchPosition(), targetAngle);
        return Math.abs(error.degrees()) < 4;
    }
}
