package Utils.PID;

import com.qualcomm.robotcore.util.RobotLog;

import MathSystems.MathUtils;

public class PIDSystem {
    private double kp, ki, kd, target, error, previousError, dt, integralRange;
    private double proportional, integral, derivative;
    private long prevTime;
    private double prevErrorSign = 0;

    public PIDSystem(double kp, double ki, double kd){
        this.kp = kp;
        this.ki = ki;
        this.kd = kd;
        target = 0;
        error = 0;
        previousError = 0;
        proportional = 0;
        integral = 0;
        derivative = 0;
        dt = 0;
        this.integralRange = 5;
    }

    public PIDSystem(double kp, double ki, double kd, double integralRange){
        this.kp = kp;
        this.ki = ki;
        this.kd = kd;
        error = 0;
        previousError = 0;
        proportional = 0;
        integral = 0;
        derivative = 0;
        dt = 0;
        prevTime = 0;
        this.integralRange = integralRange;
    }

    public void setCoefficients(double kp, double ki, double kd){
        this.kp = kp;
        this.ki = ki;
        this.kd = kd;
    }

    public double getCorrection(double error){
        proportional = error * kp;
        if(dt != 0){
            if(Math.abs(error) < integralRange) {
                integral += error * ki * dt;
                if(MathUtils.sign(error) != prevErrorSign){
                    integral = 0;
                }
            }
            if(previousError != 0){
                derivative = kd * (error - previousError) / dt;
            }
        }

        previousError = error;
        if(prevTime == 0)
            prevTime = System.currentTimeMillis();
        dt = MathUtils.millisToSec(System.currentTimeMillis() - prevTime);
        prevTime = System.currentTimeMillis();

        prevErrorSign = MathUtils.sign(error);

        return proportional + integral - derivative;
    }

    public void setTarget(double target) {
        this.target = target;
    }

    public double getTarget() {
        return target;
    }

    public void reset(){
        error = 0;
        previousError = 0;
        proportional = 0;
        integral = 0;
        derivative = 0;
        dt = 0;
    }
}
