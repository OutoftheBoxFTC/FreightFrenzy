package Hardware.HardwareSystems.FFSystems.Actions;

import Hardware.HardwareSystems.FFSystems.ScoutSystem;
import MathSystems.Angle;

public class ScoutTargets {
    public static SCOUTTarget getTarget(ScoutSystem.SCOUT_ALLIANCE alliance, ScoutSystem.SCOUT_TARGET target){
        if(alliance == ScoutSystem.SCOUT_ALLIANCE.RED){
            switch (target){
                case ALLIANCE_HIGH:
                    return new SCOUTTarget(Angle.degrees(-50), Angle.degrees(7.5), 600);
                case ALLIANCE_MID:
                    return new SCOUTTarget(Angle.degrees(-50), Angle.degrees(5), 410);
                case ALLIANCE_LOW:
                    return new SCOUTTarget(Angle.degrees(-50), Angle.degrees(-15), 420);
                case SHARED:
                    return new SCOUTTarget(Angle.degrees(-60), Angle.degrees(0), 250);
            }
        }else{
            switch (target){
                case ALLIANCE_HIGH:
                    return new SCOUTTarget(Angle.degrees(50), Angle.degrees(7.5), 420);
                case ALLIANCE_MID:
                    return new SCOUTTarget(Angle.degrees(50), Angle.degrees(5), 410);
                case ALLIANCE_LOW:
                    return new SCOUTTarget(Angle.degrees(50), Angle.degrees(-15), 420);
                case SHARED:
                    return new SCOUTTarget(Angle.degrees(60), Angle.degrees(0), 250);
            }
        }
        return new SCOUTTarget(Angle.ZERO(), Angle.ZERO(), 0);
    }

    public static class SCOUTTarget{
        public Angle turretAngle, pitchAngle;
        public double extension;

        public SCOUTTarget(Angle turretAngle, Angle pitchAngle, double extension){
            this.turretAngle = turretAngle;
            this.pitchAngle = pitchAngle;
            this.extension = extension;
        }
    }
}
