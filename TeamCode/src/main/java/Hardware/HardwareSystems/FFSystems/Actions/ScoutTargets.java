package Hardware.HardwareSystems.FFSystems.Actions;

import Hardware.HardwareSystems.FFSystems.ScoutSystem;
import MathSystems.Angle;

public class ScoutTargets {
    public static SCOUTTarget getTarget(ScoutSystem.SCOUT_ALLIANCE alliance, ScoutSystem.SCOUT_TARGET target){
        if(alliance == ScoutSystem.SCOUT_ALLIANCE.RED){
            switch (target){
                case ALLIANCE_HIGH:
                    return new SCOUTTarget(Angle.degrees(-58), Angle.degrees(32), 47.5);
                case ALLIANCE_HIGH_AUTO:
                    return new SCOUTTarget(Angle.degrees(-58), Angle.degrees(26), 45);
                case ALLIANCE_MID:
                    return new SCOUTTarget(Angle.degrees(-57), Angle.degrees(17), 38);
                case ALLIANCE_LOW:
                    return new SCOUTTarget(Angle.degrees(-58), Angle.degrees(6), 38);
                case SHARED:
                    return new SCOUTTarget(Angle.degrees(85), Angle.degrees(10), 14);
            }
        }else{
            switch (target){
                case ALLIANCE_HIGH:
                    return new SCOUTTarget(Angle.degrees(68), Angle.degrees(30), 42.5);
                case ALLIANCE_HIGH_AUTO:
                    return new SCOUTTarget(Angle.degrees(68), Angle.degrees(25.5), 42.5);
                case ALLIANCE_MID:
                    return new SCOUTTarget(Angle.degrees(57.8), Angle.degrees(15), 41);
                case ALLIANCE_LOW:
                    return new SCOUTTarget(Angle.degrees(57.8), Angle.degrees(6), 39);
                case SHARED:
                    return new SCOUTTarget(Angle.degrees(-70), Angle.degrees(10), 14);
            }
        }
        if(target == ScoutSystem.SCOUT_TARGET.PASSTHROUGH){
            return new SCOUTTarget(Angle.ZERO(), Angle.degrees(47), 20);
        }
        if(target == ScoutSystem.SCOUT_TARGET.CAP_GRAB){
            return new SCOUTTarget(Angle.ZERO(), Angle.degrees(0), 20);
        }
        if(target == ScoutSystem.SCOUT_TARGET.CAP_PLACE){
            return new SCOUTTarget(Angle.ZERO(), Angle.degrees(50), 40);
        }
        if(target == ScoutSystem.SCOUT_TARGET.LONG_PASSTHROUGH){
            return new SCOUTTarget(Angle.degrees(0), Angle.degrees(36), 40);
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
