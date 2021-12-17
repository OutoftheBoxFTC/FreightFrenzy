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

    private double targetPos = 0;
    private TurretSystem system;
    private PIDFSystem pid;

    public MovePitchAction(TurretSystem system){
        this.system = system;
        pid = new PIDFSystem(P, I, D, 0.1);
        targetPos = Double.NaN;
    }

    public void setTargetAngle(Angle angle){
        targetPos = system.getPitchMotorPos() + ((int) (MathUtils.getRotDist(angle, system.getPitchPosition()).degrees() * TurretSystem.TICKS_PER_DEGREE_PANCAKES));
    }

    @Override
    public void update() {
        pid.setCoefficients(P, 0, D, F);
        if(Double.isNaN(targetPos)){
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
        double power = pid.getCorrection(targetPos - system.getPitchMotorPos(), Math.cos(system.getTurretPosition().radians()));
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
        return Math.abs(targetPos - system.getPitchMotorPos()) < (2 * TurretSystem.TICKS_PER_DEGREE_PANCAKES);
    }
}
