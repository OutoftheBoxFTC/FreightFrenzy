package MathSystems.Profiling;

public class AccelTimeState {
    public double dTime, time;
    public double pos, vel, accel;

    public AccelTimeState(double dTime, double pos, double vel, double accel){
        this.dTime = dTime;
        this.pos = pos;
        this.vel = vel;
        this.accel = accel;
    }

    public AccelTimeState getAt(double dTime){
        AccelTimeState tmp = new AccelTimeState(
                0,
                pos + (vel * dTime) + (0.5 * accel * dTime * dTime),
                vel + (accel * dTime),
                accel
        );
        tmp.time = time + dTime;
        return tmp;
    }

    @Override
    public String toString() {
        return String.format("Time: %2f Pos: %2f Vel: %2f Accel: %2f dTime: %2f", time, pos, vel, accel, dTime);
    }
}
