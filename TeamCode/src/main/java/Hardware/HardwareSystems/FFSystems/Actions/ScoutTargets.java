package Hardware.HardwareSystems.FFSystems.Actions;

import Hardware.HardwareSystems.FFSystems.ScoutSystem;
import MathSystems.Angle;

public class ScoutTargets {
    public static SCOUTTarget getTarget(ScoutSystem.SCOUT_ALLIANCE alliance, ScoutSystem.SCOUT_TARGET target){
        if(alliance == ScoutSystem.SCOUT_ALLIANCE.RED){
            switch (target){
                case ALLIANCE_HIGH:
                    return new SCOUTTarget(Angle.degrees(-60), Angle.degrees(36), 47.5);
                case ALLIANCE_MID:
                    return new SCOUTTarget(Angle.degrees(-57.8), Angle.degrees(17), 27);
                case ALLIANCE_LOW:
                    return new SCOUTTarget(Angle.degrees(-57.8), Angle.degrees(9), 30);
                case SHARED:
                    return new SCOUTTarget(Angle.degrees(70), Angle.degrees(10), 15);
            }
        }else{
            switch (target){
                case ALLIANCE_HIGH:
                    return new SCOUTTarget(Angle.degrees(60), Angle.degrees(36), 47.5);
                case ALLIANCE_MID:
                    return new SCOUTTarget(Angle.degrees(57.8), Angle.degrees(17), 27);
                case ALLIANCE_LOW:
                    return new SCOUTTarget(Angle.degrees(57.8), Angle.degrees(9), 28);
                case SHARED:
                    return new SCOUTTarget(Angle.degrees(-70), Angle.degrees(10), 20);
            }
        }
        if(target == ScoutSystem.SCOUT_TARGET.PASSTHROUGH){
            return new SCOUTTarget(Angle.ZERO(), Angle.degrees(50), 20);
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
