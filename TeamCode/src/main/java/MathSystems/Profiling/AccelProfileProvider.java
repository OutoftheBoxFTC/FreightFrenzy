package MathSystems.Profiling;

public class AccelProfileProvider {
    public static final double NANO_TO_SEC = (1/1000000000.0);

    private AccelProfile profile;
    private long startTime;

    public AccelProfileProvider(AccelProfile profile){
        this.profile = profile;
    }

    public void start(){
        startTime = System.nanoTime();
    }

    public AccelTimeState get(){
        double nowSec = (System.nanoTime() - startTime) * NANO_TO_SEC;
        if(nowSec > profile.getTimeLength()){
            nowSec = profile.getTimeLength();
        }
        return profile.getAt(nowSec);
    }

    public boolean finished(){
        double nowSec = (System.nanoTime() - startTime) * NANO_TO_SEC;
        return nowSec > profile.getTimeLength();
    }

    public void setProfile(AccelProfile profile) {
        this.profile = profile;
    }
}
