package Utils.MechanismPathGeneration;

import MechanismPathGeneration.Constraints.MechanismConstraint;

public class RangeConstraint extends MechanismConstraint {
    public static RangeConstraint DEFAULTS = new RangeConstraint(
            -90,
            90,
            -10,
            40,
            0,
            200
    );

    public RangeConstraint(double turrMin, double turrMax, double pitchMin, double pitchMax, double exMin, double exMax) {
        super(turrMin, turrMax, pitchMin, pitchMax, exMin, exMax);
    }

    @Override
    public boolean isConstrained(double turretPos, double pitchPos, double extensionPos) {
        if(turretPos > turrMax || turretPos < turrMin){
            return true;
        }

        if(pitchPos > pitchMax || pitchPos < pitchMin){
            return true;
        }

        if(extensionPos > exMax || extensionPos < exMin){
            return true;
        }

        return false;
    }
}
