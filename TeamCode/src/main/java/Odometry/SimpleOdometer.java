package Odometry;

import Hardware.HardwareSystems.UGSystems.OdometrySystem;
import MathSystems.Angle;
import MathSystems.Position;
import Utils.ProgramClock;
import MathSystems.Vector.Vector2;
import MathSystems.Vector.Vector3;

public class SimpleOdometer extends Odometer {
    private final Position prevPosition;
    public SimpleOdometer(Position position, Position velocity) {
        super(position, velocity);
        prevPosition = position.clone();
    }

    @Override
    public void reset() {
        this.position.set(Position.ZERO());
        this.velocity.set(Position.ZERO());
        this.prevPosition.set(Position.ZERO());
    }

    @Override
    public Vector2 getRelativeIncrements(OdometrySystem system) {
        double rot = (system.getOdometryRightInc() - system.getOdometryLeftInc()) / 2.0;
        rot *= OdometryConstants.ODOMETRY_CPR;
        return new Vector2((system.getOdometryLeftInc() + system.getOdometryRightInc())/2.0, system.getOdometryAuxInc() - (rot * OdometryConstants.ODOMETRY_RPA));
    }

    @Override
    public Vector3 getStaticIncrements(Vector2 relativeIncrements, OdometrySystem system) {
        double rot = (system.getOdometryRight() - system.getOdometryLeft()) / 2.0;
        rot *= OdometryConstants.ODOMETRY_CPR;
        Vector2 rotated = relativeIncrements.rotate(Angle.radians(rot));
        return rotated.toVector3((system.getOdometryRightInc() - system.getOdometryLeftInc()) / 2.0);
    }

    @Override
    public void setPositionAndVelocity(Vector3 staticIncrements, Position position, Position velocity) {
        Vector2 linInc = staticIncrements.getVector2().scale(OdometryConstants.ODOMETRY_CPI);
        position.set(position.add(linInc, Angle.radians(staticIncrements.getC())));
        double tau = (2 * Math.PI);
        position.setR(Angle.radians(((position.getR().radians() % tau) + tau) % tau));
        double dt = ProgramClock.getFrameTimeSeconds();
        velocity.set(position.add(prevPosition.invert()).scale(dt));
        prevPosition.set(position);
    }
}
