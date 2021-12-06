package Odometry;

import com.qualcomm.robotcore.util.RobotLog;

import Hardware.HardwareSystems.UGSystems.OdometrySystem;
import MathSystems.Angle;
import MathSystems.ConstantVMathUtil;
import MathSystems.Position;
import MathSystems.Vector.Vector2;
import MathSystems.Vector.Vector3;
import Utils.ProgramClock;

/**
 * For a breakdown of how this works
 *
 * https://www.desmos.com/calculator/lzxnrmvsnd
 *
 * For the derivation of these equations (Requires a bit of calculus knowledge)
 *
 * https://www.desmos.com/calculator/ezczsdozdt
 *
 * For a video explanation of this algorithm:
 * https://www.youtube.com/watch?v=YcZqutpT1r8
 *
 * If you need more explanation... uhhhh
 * good luck
 * take calc ab ig
 */
public class ConstantVOdometer extends Odometer {
    private final Position prevPosition;
    private final Vector2 prevInc = Vector2.ZERO();
    public ConstantVOdometer(Position position, Position velocity) {
        super(position, velocity);
        prevPosition = position.clone();
        this.prevInc.set(Vector2.ZERO());
    }

    @Override
    public void reset() {
        this.position.set(Position.ZERO());
        this.velocity.set(Position.ZERO());
        this.prevPosition.set(Position.ZERO());
        this.prevInc.set(Vector2.ZERO());
    }

    @Override
    public Vector2 getRelativeIncrements(OdometrySystem system) {
        double rot = (system.getOdometryRightInc() - system.getOdometryLeftInc()) / 2.0;
        rot *= OdometryConstants.ODOMETRY_CPR;
        return new Vector2((system.getOdometryLeftInc() + system.getOdometryRightInc())/2.0, system.getOdometryAuxInc() - (rot * OdometryConstants.ODOMETRY_RPA));
    }

    @Override
    public Vector3 getStaticIncrements(Vector2 relativeIncrements, OdometrySystem system) {
        double rot = (system.getOdometryRightInc() - system.getOdometryLeftInc()) / 2.0;
        rot *= OdometryConstants.ODOMETRY_CPR;
        Vector2 rotated = ConstantVMathUtil.toRobotCentric(relativeIncrements.getA(), relativeIncrements.getB(), rot);
        double absRot = ((system.getOdometryRight() - system.getOdometryLeft())/2.0) * OdometryConstants.ODOMETRY_CPR;
        double tau = (2 * Math.PI);
        rotated.set(rotated.rotate(Angle.radians(((-absRot % tau) + tau) % tau)));
        return rotated.toVector3((system.getOdometryRightInc() - system.getOdometryLeftInc()) / 2.0);
    }

    @Override
    public void setPositionAndVelocity(Vector3 staticIncrements, Position position, Position velocity) {
        Vector2 linInc = staticIncrements.getVector2().scale(OdometryConstants.ODOMETRY_CPI);
        linInc.setB(linInc.getB() * -1);
        position.set(position.add(linInc, Angle.radians(staticIncrements.getC() * OdometryConstants.ODOMETRY_CPR)));
        double tau = (2 * Math.PI);
        position.setR(Angle.radians(((position.getR().radians() % tau) + tau) % tau));
        double dt = ProgramClock.getFrameTimeSeconds();
        velocity.set(position.add(prevPosition.invert()).scale(dt));
        prevPosition.set(position);
    }
}
