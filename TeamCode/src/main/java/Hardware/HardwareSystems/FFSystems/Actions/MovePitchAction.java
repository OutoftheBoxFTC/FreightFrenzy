package Hardware.HardwareSystems.FFSystems.Actions;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.util.RobotLog;

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
    public static double P = -0.01, I = 0, D = 0.0000, F = 0;

    private double targetPos = 0;
    private int numFrames = 0;
    private Angle targetAngle = Angle.ZERO();
    private TurretSystem system;
    private PIDFSystem pid;

    public MovePitchAction(TurretSystem system){
        this.system = system;
        pid = new PIDFSystem(P, I, D, 0.1);
        targetPos = Double.NaN;
    }

    public void setTargetAngle(Angle angle){
        targetAngle = angle;
        targetPos = system.getPitchMotorPos() + ((int) (MathUtils.getRotDist(angle, system.getFakePitchPos()).degrees() * TurretSystem.TICKS_PER_DEGREE_PANCAKES));
    }

    @Override
    public void update() {
        pid.setCoefficients(P, 0, D, F);
        if(Double.isNaN(targetPos)){
            return;
        }

        if(isAtTarget()){
            numFrames ++;
            //double check juuuuust in case
            if(numFrames > 7 && Math.abs(MathUtils.getRotDist(system.getPitchPosition(), targetAngle).degrees()) > 9){
                //setTargetAngle(targetAngle);
            }
            system.setPitchMotorPower(0);
            return;
        }
        numFrames = 0;
        FtcDashboard.getInstance().getTelemetry().addData("Target Pos", targetPos);
        double power = pid.getCorrection(targetPos - system.getPitchMotorPos(), 0);
        RobotLog.ii("Target Pos", targetPos + "|" + system.getPitchMotorPos() + " | " + power);
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
        return Math.abs(targetPos - system.getPitchMotorPos()) < (10);
    }
}
