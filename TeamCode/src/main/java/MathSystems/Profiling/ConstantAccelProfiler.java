package MathSystems.Profiling;

import MathSystems.MathUtils;

public class ConstantAccelProfiler {
    public static AccelProfile profileConstantAccel(double start, double end, double velMax, double accelMax){
        if(start > end){
            //To make the math easier we always go from start to end
            return profileConstantAccel(end, start, velMax, accelMax).flipped();
        }

        double totalDist = end - start;
        double accelDist = ((velMax * velMax) / (2 * accelMax)) * 2;

        if(accelDist > totalDist){
            //Accelerate for half the time then de-accelerate for the second half
            //(Spaceship profile)
            double velPeakDist = totalDist / 2;
            double velPeakTime = (Math.sqrt(2) * Math.sqrt(velPeakDist)) / (Math.sqrt(accelMax));

            AccelProfile profile = new AccelProfile(new AccelTimeState(velPeakTime, start, 0, accelMax));
            profile.addState(new AccelTimeState(velPeakTime, 0, 0, -accelMax));
            return profile;
        }else{
            //Accelerate to max v, coast at max v, then de-accelerate
            //(Car profile)

            double maxVelTime = velMax / accelMax;
            double coastDist = totalDist - accelDist;
            double coastTime = coastDist / velMax;

            AccelProfile profile = new AccelProfile(new AccelTimeState(maxVelTime, start, 0, accelMax));
            profile.addState(new AccelTimeState(coastTime, 0, 0, 0));
            profile.addState(new AccelTimeState(maxVelTime, 0, 0, -accelMax));
            return profile;
        }
    }

    public static AccelProfile profileConstantAccel(AccelTimeState start, AccelTimeState end, double velMax, double accelMax){
        double dir = MathUtils.sign(end.pos - start.pos);

        double maxVel = velMax * dir;
        double maxAccel = accelMax * dir;

        AccelProfile profile;
        if(!MathUtils.epsilonEquals(MathUtils.sign(start.vel), dir) && !MathUtils.epsilonEquals(start.vel, 0)){
            //We are travelling in the wrong direction, need to switch directions
            double timeToZero = Math.abs(start.vel / maxAccel);
            AccelTimeState zeroTimeState = new AccelTimeState(timeToZero, start.pos, start.vel, maxAccel);

            double distAfterZero = zeroTimeState.getAt(timeToZero).pos;

            AccelProfile travelProfile = profileConstantAccel(distAfterZero, end.pos, velMax, accelMax);

            profile = new AccelProfile(zeroTimeState);
            profile.addProfile(travelProfile);
            return profile;
        }else{
            double timeToZero = Math.abs(start.vel / maxAccel);
            double stopDist = (start.vel * timeToZero) + (0.5 * -maxAccel * (timeToZero * timeToZero));
            if(Math.abs(stopDist) > Math.abs((end.pos - start.pos))){
                //Overshoot, correct
                AccelProfile correction = profileConstantAccel(start.pos + stopDist, end.pos, velMax, accelMax);
                profile = new AccelProfile(new AccelTimeState(timeToZero, start.pos, start.vel, -maxAccel));
                profile.addProfile(correction);
                return profile;
            }

            AccelTimeState accelToMax = new AccelTimeState(Math.abs((maxVel - start.vel) / maxAccel), 0, start.vel, maxAccel);
            AccelTimeState deaccelFromMax = new AccelTimeState(Math.abs((maxVel) / maxAccel), 0, maxVel, -maxAccel);

            double remainingDist = Math.abs(end.pos - start.pos) - Math.abs((accelToMax.getAt(accelToMax.dTime).pos + deaccelFromMax.getAt(deaccelFromMax.dTime).pos));

            if(remainingDist < 0){
                double maxSearchVel = maxVel;
                double minSearchVel = -maxVel;
                for(int i = 0; i < 50; i ++){
                    double searchedVel = (maxSearchVel + minSearchVel) / 2.0;
                    accelToMax = null; //Technically redundant, but forces gc to remove the instance
                    deaccelFromMax = null;
                    accelToMax = new AccelTimeState(Math.abs((searchedVel - start.vel) / maxAccel), 0, start.vel, maxAccel);
                    deaccelFromMax = new AccelTimeState(Math.abs((searchedVel) / maxAccel), 0, searchedVel, -maxAccel);

                    remainingDist = Math.abs(end.pos - start.pos) - Math.abs((accelToMax.getAt(accelToMax.dTime).pos + deaccelFromMax.getAt(deaccelFromMax.dTime).pos));

                    if(Math.abs(remainingDist) < 0.01){
                        break;
                    }

                    if(remainingDist > 0){
                        minSearchVel = searchedVel;
                    }else{
                        maxSearchVel = searchedVel;
                    }
                }
                profile = new AccelProfile(accelToMax);
                profile.addState(deaccelFromMax);
                return profile;
            }
            accelToMax = null; //Technically redundant, but forces gc to remove the instance
            deaccelFromMax = null;
            accelToMax = new AccelTimeState(Math.abs((maxVel - start.vel) / maxAccel), start.pos, start.vel, maxAccel);
            deaccelFromMax = new AccelTimeState(Math.abs((maxVel) / maxAccel), 0, maxVel, -maxAccel);

            remainingDist = accelToMax.getAt(accelToMax.dTime).pos + deaccelFromMax.getAt(deaccelFromMax.dTime).pos;
            remainingDist = end.pos - remainingDist;

            double coastTime = Math.abs(remainingDist / maxVel);
            profile = new AccelProfile(accelToMax);
            profile.addState(new AccelTimeState(coastTime, 0, maxVel, 0));
            profile.addState(deaccelFromMax);
            return profile;
        }
    }

}
