package Utils.PathUtils.Profiling;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;

public class LinearProfile {
    private Map<Double, Double> lStates, rStates;

    public LinearProfile(Map<Double, Double> lStates, Map<Double, Double> rStates){
        this.lStates = lStates;
        this.rStates = rStates;
    }

    public double getL(double t){
        double remaining = t;
        double prev = 0;
        for(double d : lStates.keySet()){
            remaining -= (d - prev);
            if(remaining < 0){
                if(lStates.containsKey(prev)) {
                    return lStates.get(prev);
                }else{
                    return 0;
                }
            }
            prev = d;
        }
        return 0;
    }

    public double getR(double t){
        double remaining = t;
        double prev = 0;
        for(double d : rStates.keySet()){
            t -= (d - prev);
            if(t < 0){
                return rStates.get(prev);
            }
            prev = d;
        }
        return rStates.get(1.0);
    }

    public Map<Double, Double> getlStates() {
        return lStates;
    }

    public Map<Double, Double> getrStates() {
        return rStates;
    }
}
