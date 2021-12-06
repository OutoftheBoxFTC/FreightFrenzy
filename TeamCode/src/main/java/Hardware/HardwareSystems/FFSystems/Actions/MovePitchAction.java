package Hardware.HardwareSystems.FFSystems.Actions;

import Hardware.HardwareSystems.FFSystems.FFConstants;
import Hardware.HardwareSystems.FFSystems.TurretSystem;
import MathSystems.Angle;
import MathSystems.MathUtils;
import MathSystems.Profiling.AccelProfile;
import MathSystems.Profiling.AccelProfileProvider;
import MathSystems.Profiling.AccelTimeState;
import MathSystems.Profiling.ConstantAccelProfiler;
import State.Action.Action;

public class MovePitchAction implements Action {
    private Angle targetAngle;
    private TurretSystem system;
    private AccelProfile profile;
    private AccelProfileProvider provider;

    public MovePitchAction(TurretSystem system){
        this.system = system;
        targetAngle = Angle.ZERO();
    }

    public void setTargetAngle(Angle angle){
        if(angle.equals(targetAngle)){
            return;
        }
        targetAngle = angle;
        if(!provider.finished()) {
            profile = ConstantAccelProfiler.profileConstantAccel(provider.get(), new AccelTimeState(0, angle.degrees(),
                    0, 0), FFConstants.Pitch.PITCH_MAX_VEL, FFConstants.Pitch.PITCH_MAX_ACCEL);
        }else{
            profile = ConstantAccelProfiler.profileConstantAccel(
                    new AccelTimeState(0, system.getPitchPosition().degrees(), 0, 0),
                    new AccelTimeState(0, angle.degrees(), 0, 0),
                    FFConstants.Pitch.PITCH_MAX_VEL, FFConstants.Pitch.PITCH_MAX_ACCEL);
        }
        provider.setProfile(profile);
        provider.start();
    }

    @Override
    public void update() {
        if(provider.finished()){
            Angle error = MathUtils.getRotDist(system.getPitchPosition(), targetAngle);
            if(Math.abs(error.degrees()) < 2){
                system.setPitchMotorPower(0);
                return;
            }
            system.setPitchMotorPower(MathUtils.sign(error.degrees()) * FFConstants.Pitch.PITCH_CORRECTION_SPEED);
        }else {
            AccelTimeState state = provider.get();
            double turretPower = (state.vel / FFConstants.Pitch.PITCH_MAX_VEL) + (state.accel + FFConstants.Pitch.PITCH_KACCEL);
            system.setPitchMotorPower(MathUtils.signedMax(turretPower, FFConstants.Pitch.PITCH_KSTATIC));
        }
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
        Angle error = MathUtils.getRotDist(system.getTurretPosition(), targetAngle);
        return Math.abs(error.degrees()) < 2;
    }
}
