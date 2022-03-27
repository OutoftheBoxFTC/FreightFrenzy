package State.Action.StandardActions;

import com.acmerobotics.roadrunner.profile.MotionProfile;
import com.acmerobotics.roadrunner.profile.MotionProfileGenerator;
import com.acmerobotics.roadrunner.profile.MotionState;
import com.acmerobotics.roadrunner.util.NanoClock;
import com.qualcomm.robotcore.util.RobotLog;

import Hardware.SmartDevices.SmartServo.SmartServo;
import State.Action.Action;

public class ServoProfileAction implements Action {
    private double accel, vel, start, end;
    private SmartServo servo;
    private MotionProfile profile;
    private NanoClock clock;
    private double startTime;

    public ServoProfileAction(SmartServo servo, double vel, double accel, double target){
        this.vel = vel;
        this.accel = accel;
        this.end = target;
        this.servo = servo;
        clock = NanoClock.system();
    }

    @Override
    public void initialize() {
        this.start = servo.getServo().getPosition();
        MotionState start = new MotionState(this.start, 0);
        MotionState end = new MotionState(this.end, 0);
        profile = MotionProfileGenerator.generateSimpleMotionProfile(start, end, vel, accel);
        startTime = clock.seconds();
    }

    @Override
    public void update() {
        double dt = clock.seconds() - startTime;
        RobotLog.ii("Time", dt+" | " + profile.duration());
        if(dt < profile.duration()){
            double pos = profile.get(dt).getX();
            servo.setPosition(pos);
            RobotLog.ii("pos", "" + pos);
        }else{
            RobotLog.ii("Time", dt+" | " + profile.duration());
            servo.setPosition(profile.end().getX());
            deactivateNow();
        }
    }
}
