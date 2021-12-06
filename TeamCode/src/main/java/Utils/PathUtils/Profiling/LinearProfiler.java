package Utils.PathUtils.Profiling;

import MathSystems.MathUtils;
import MathSystems.Position;
import MathSystems.Vector.Vector2;
import MathSystems.Vector.Vector3;
import Utils.PathUtils.*;

import java.util.*;

public class LinearProfiler {

    public static LinearProfile profile(Path path, double velMax, double minVel, double accelFor, double accelBack, int samples){
        double step = (1.0/((double)samples));

        Map<Double, Double> velMap = new TreeMap<>();
        Stack<Double> keyStack = new Stack<>();
        //First step is to compute the maximum velocity at various points in the path
        double prevLinVel = 0;
        for(int i = 0; i < path.getSegments().size(); i ++) {
            Segment s = path.getSegments().get(i);
            for (double t = 0; t <= 1; t += step) {
                Position prev = s.get(t - step);
                Position now = s.get(t);
                Vector3 vel = s.deriv(t);

                double dP = now.getPos().distanceTo(prev.getPos());
                double maxVelChange = Math.sqrt(2 * accelFor * dP);

                Vector2 linVel = vel.getVector2();

                if(linVel.length() - prevLinVel > maxVelChange) {
                    //Obey forward consistency in profiling
                    double nextVel = prevLinVel + (MathUtils.sign(linVel.length() - prevLinVel) * maxVelChange);
                    double maxVel = Math.min(Math.abs(nextVel), velMax);
                    if(Math.abs(MathUtils.sign(nextVel) * maxVel) < minVel){
                        velMap.put(i + t, MathUtils.sign(nextVel) * minVel);
                        prevLinVel = MathUtils.sign(nextVel) * minVel;
                    }else {
                        velMap.put(i + t, MathUtils.sign(nextVel) * maxVel);
                        prevLinVel = MathUtils.sign(nextVel) * maxVel;
                    }
                }else{
                    double maxVel = Math.min(linVel.length(), velMax);
                    //double maxVel = linVel.length();
                    if(Math.abs(maxVel) < minVel){
                        velMap.put(i + t, minVel);
                        prevLinVel = minVel;
                    }else {
                        velMap.put(i + t, maxVel);
                        prevLinVel = maxVel;
                    }
                }
            }
        }

        LinearProfile profile = new LinearProfile(velMap, new TreeMap<>());

        prevLinVel = 0;
        //Go back through the profile to make sure it obeys the maximum velocity constraint
        TreeMap<Double, Double> newMap = new TreeMap<>();
        for(int i = path.getSegments().size()-1; i >= 0; i --){
            Segment s = path.getSegments().get(i);
            for(double t = 1; t >= 0; t -= step){
                Position now = s.get(t - step);
                Position prev = s.get(t);
                Vector3 vel = s.deriv(t);

                double dP = now.getPos().distanceTo(prev.getPos());
                double maxVelChange = Math.sqrt(2 * accelBack * dP);

                double linVel = profile.getL(i + t);

                if(linVel - prevLinVel > maxVelChange) {
                    //Obey forward consistency in profiling
                    double nextVel = prevLinVel + (MathUtils.sign(linVel - prevLinVel) * maxVelChange);
                    double maxVel = Math.min(Math.abs(nextVel), velMax);
                    if(Math.abs(MathUtils.sign(nextVel) * maxVel) < minVel){
                        newMap.put(i + t, MathUtils.sign(nextVel) * minVel);
                        prevLinVel = MathUtils.sign(nextVel) * minVel;
                    }else {
                        newMap.put(i + t, MathUtils.sign(nextVel) * maxVel);
                        prevLinVel = MathUtils.sign(nextVel) * maxVel;
                    }
                }else{
                    double maxVel = Math.min(linVel, velMax);
                    //double maxVel = linVel.length();
                    if(Math.abs(maxVel) < minVel){
                        newMap.put(i + t, minVel);
                        prevLinVel = minVel;
                    }else {
                        newMap.put(i + t, maxVel);
                        prevLinVel = maxVel;
                    }
                }
            }
        }

        return new LinearProfile(newMap, new TreeMap<>());
    }
}
