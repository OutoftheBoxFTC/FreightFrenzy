package Utils.MechanismPathGeneration.Constraints;

import Utils.MechanismPathGeneration.RangeConstraint;

public class KeepOutConstraint extends MechanismConstraint{
    private double turretMin = RangeConstraint.DEFAULTS.turrMin,
            turretMax = RangeConstraint.DEFAULTS.turrMax,
            pitchMin = RangeConstraint.DEFAULTS.pitchMin,
            pitchMax = RangeConstraint.DEFAULTS.pitchMax,
            exMin = RangeConstraint.DEFAULTS.exMin,
            exMax = RangeConstraint.DEFAULTS.exMax;

    public KeepOutConstraint setTurretLimits(double turretMin, double turretMax){
        this.turretMin = turretMin;
        this.turretMax = turretMax;
        return this;
    }

    public KeepOutConstraint setPitchLimits(double pitchMin, double pitchMax){
        this.pitchMin = pitchMin;
        this.pitchMax = pitchMax;
        return this;
    }

    public KeepOutConstraint setExtensionLimits(double exMin, double exMax){
        this.exMin = exMin;
        this.exMax = exMax;
        return this;
    }

    public KeepOutConstraint setTurretMin(double turretMin) {
        this.turretMin = turretMin;
        return this;
    }

    public KeepOutConstraint setTurretMax(double turretMax) {
        this.turretMax = turretMax;
        return this;
    }

    public KeepOutConstraint setPitchMin(double pitchMin) {
        this.pitchMin = pitchMin;
        return this;
    }

    public KeepOutConstraint setPitchMax(double pitchMax) {
        this.pitchMax = pitchMax;
        return this;
    }

    public KeepOutConstraint setExMin(double exMin) {
        this.exMin = exMin;
        return this;
    }

    public KeepOutConstraint setExMax(double exMax) {
        this.exMax = exMax;
        return this;
    }


    @Override
    public boolean isConstrained(double turretPos, double pitchPos, double extensionPos) {
        int constrained = 0;
        if(turretPos <= turretMax && turretPos >= turretMin){
            constrained ++;
        }
        if(pitchPos <= pitchMax && pitchPos >= pitchMin){
            constrained ++;
        }
        if(extensionPos <= exMax && extensionPos >= exMin){
            constrained ++;
        }
        return constrained == 3;
    }
}
