package Utils.MechanismPathGeneration.Constraints;

public abstract class MechanismConstraint {
    public double exMin, exMax, turrMin, turrMax, pitchMin, pitchMax;

    public MechanismConstraint(){
        this(0, 0, 0, 0, 0, 0);
    }

    public MechanismConstraint(double turrMin, double turrMax, double pitchMin, double pitchMax, double exMin, double exMax){
        this.turrMin = turrMin;
        this.turrMax = turrMax;
        this.pitchMin = pitchMin;
        this.pitchMax = pitchMax;
        this.exMin = exMin;
        this.exMax = exMax;
    }

    public abstract boolean isConstrained(double turretPos, double pitchPos, double extensionPos);
}
