package Utils.PathUtils;

import MathSystems.Angle;
import MathSystems.MathUtils;
import MathSystems.Position;
import MathSystems.Vector.Vector2;
import MathSystems.Vector.Vector3;

public class Line implements Segment{
    Position start, end;
    Vector2 slope;
    Angle rChange;
    public Line(Position start, Position end){
        this.start = start;
        this.end = end;
        this.slope = end.add(start.scale(-1)).getPos();
        this.rChange = MathUtils.getRotDist(start.getR(), end.getR());
    }

    @Override
    public Position get(double t) {
        return new Position(start.getPos().add(slope.scale(t)), Angle.degrees((start.getR().degrees() + (rChange.degrees() * t)) % 360));
    }

    @Override
    public Vector3 deriv(double t) {
        return new Vector3(slope, rChange.radians());
    }

    @Override
    public Vector3 secondDeriv(double t) {
        return Vector3.ZERO();
    }

    @Override
    public Position project(Position pos) {
        if(slope.getA() == 0){
            Vector2 v = new Vector2(start.getX(), pos.getY());
            double angR = start.getR().radians() + (rChange.radians() * ((pos.getY() - start.getY()) / slope.getB()));
            return new Position(v, Angle.radians(angR));
        }
        if(slope.getB() == 0){
            Vector2 v = new Vector2(pos.getX(), start.getY());
            double angR = start.getR().radians() + (rChange.radians() * ((pos.getX() - start.getX()) / slope.getA()));
            return new Position(v, Angle.radians(angR));
        }

        Vector2 closest = MathUtils.getClosestPoint(start.getPos(), end.getPos(), pos.getPos());
        double angR = start.getR().radians() + (rChange.radians() * ((pos.getX() - start.getX()) / slope.getA()));
        return new Position(closest, Angle.radians(angR));
    }

    @Override
    public double projectPos(Position pos) {
        if(slope.getA() == 0){
            return ((pos.getY() - start.getY()) / slope.getB());
        }
        if(slope.getB() == 0){
            return ((pos.getX() - start.getX()) / slope.getA());
        }

        return ((pos.getX() - start.getX()) / slope.getA());
    }
}
