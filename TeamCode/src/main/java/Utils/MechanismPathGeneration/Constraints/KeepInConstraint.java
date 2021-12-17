package Utils.MechanismPathGeneration.Constraints;

import MechanismPathGeneration.RangeConstraint;

public class KeepInConstraint extends MechanismConstraint{
    private double turretMin = RangeConstraint.DEFAULTS.turrMin,
            turretMax = RangeConstraint.DEFAULTS.turrMax,
            pitchMin = RangeConstraint.DEFAULTS.pitchMin,
            pitchMax = RangeConstraint.DEFAULTS.pitchMax,
            exMin = RangeConstraint.DEFAULTS.exMin,
            exMax = RangeConstraint.DEFAULTS.exMax;

    private boolean turret = false, pitch = false, ex = false;

    public KeepInConstraint setTurretLimits(double turretMin, double turretMax){
        this.turretMin = turretMin;
        this.turretMax = turretMax;
        turret = true;
        return this;
    }

    public KeepInConstraint setPitchLimits(double pitchMin, double pitchMax){
        this.pitchMin = pitchMin;
        this.pitchMax = pitchMax;
        pitch = true;
        return this;
    }

    public KeepInConstraint setExtensionLimits(double exMin, double exMax){
        this.exMin = exMin;
        this.exMax = exMax;
        ex = true;
        return this;
    }

    public KeepInConstraint setTurretMin(double turretMin) {
        this.turretMin = turretMin;
        turret = true;
        return this;
    }

    public KeepInConstraint setTurretMax(double turretMax) {
        this.turretMax = turretMax;
        turret = true;
        return this;
    }

    public KeepInConstraint setPitchMin(double pitchMin) {
        this.pitchMin = pitchMin;
        pitch = true;
        return this;
    }

    public KeepInConstraint setPitchMax(double pitchMax) {
        this.pitchMax = pitchMax;
        pitch = true;
        return this;
    }

    public KeepInConstraint setExMin(double exMin) {
        this.exMin = exMin;
        ex = true;
        return this;
    }

    public KeepInConstraint setExMax(double exMax) {
        this.exMax = exMax;
        ex = true;
        return this;
    }


    @Override
    public boolean isConstrained(double turretPos, double pitchPos, double extensionPos) {
        int constrained = 0;
        if((turretPos >= turretMax && turretPos <= turretMin) || !turret){
            constrained ++;
        }
        if((pitchPos >= pitchMax && pitchPos <= pitchMin) || !pitch){
            constrained ++;
        }
        if((extensionPos >= exMax && extensionPos <= exMin) || !ex){
            constrained ++;
        }
        return constrained != 3 && constrained != 0;
    }
}
