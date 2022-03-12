package Hardware.HardwareSystems.FFSystems.Actions;

import com.acmerobotics.dashboard.config.Config;

import Hardware.HardwareSystems.FFSystems.FFConstants;
import Hardware.HardwareSystems.FFSystems.ScoutSystem;
import MathSystems.Angle;
import MathSystems.MathUtils;
import State.Action.Action;
import Utils.PID.PIDFSystem;

@Config
public class MovePitchAction implements Action {
    public static double P = 0.01, I = 0, D = 0.0, F = 0;

    private double targetPos = 0;
    private int numFrames = 0;
    private Angle targetAngle = Angle.ZERO();
    private ScoutSystem system;
    private PIDFSystem pid;

    private boolean active = true;

    public MovePitchAction(ScoutSystem system){
        this.system = system;
        pid = new PIDFSystem(P, I, D, 0.1);
        targetPos = Double.NaN;
    }

    public void setTargetAngle(Angle angle){
        targetAngle = angle;
        targetPos = angle.degrees() * 18.189;
    }

    @Override
    public void update() {
        if(!active){
            return;
        }
        pid.setCoefficients(P, 0, D, F);
        if(Double.isNaN(targetPos)){
            return;
        }

        if(isAtTarget()){
            system.setPitchMotorPower(0);
            return;
        }
        double power = pid.getCorrection(targetPos - system.getPitchMotorPos(), 0);

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
        return Math.abs(targetPos - system.getPitchMotorPos()) < (15);
    }

    public void setPIDActive(boolean b) {
        active = b;
    }
}
