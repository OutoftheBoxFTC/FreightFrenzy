package Utils.PathUtils;

import MathSystems.Angle;
import MathSystems.MathUtils;
import MathSystems.Position;
import MathSystems.Vector.Vector3;

public class Spline implements Segment{
    SplinePart x, y, r;

    public Spline(Position start, Position end, Vector3 startDeriv, Vector3 endDeriv, Vector3 startAccel, Vector3 endAccel){
        x = new SplinePart(start.getX(), end.getX(), startDeriv.getA(), endDeriv.getA(), startAccel.getA(), endAccel.getA());
        y = new SplinePart(start.getY(), end.getY(), startDeriv.getB(), endDeriv.getB(), startAccel.getB(), endAccel.getB());
        r = new SplinePart(start.getR().radians(), end.getR().radians(), startDeriv.getC(), endDeriv.getC(), startAccel.getC(), endAccel.getC());
    }

    @Override
    public Position get(double t) {
        return new Position(x.get(t), y.get(t), Angle.radians(r.get(t)));
    }

    @Override
    public Vector3 deriv(double t) {
        return new Vector3(x.getDeriv(t), y.getDeriv(t), r.getDeriv(t));
    }

    @Override
    public Vector3 secondDeriv(double t) {
        return new Vector3(x.getSecondDeriv(t), y.getSecondDeriv(t), r.getSecondDeriv(t));
    }

    @Override
    public Position project(Position pos) {
        return get(projectPos(pos));
    }

    @Override
    public double projectPos(Position pos) {
        double curr = 0.5;
        double length = 0;

        for(double t = 0.01; t <= 1; t += 0.01){
            Position prev = get(t-0.01);
            Position now = get(t);
            length += prev.getPos().distanceTo(now.getPos());
        }

        for(int i = 0; i < 250; i ++){
            Position p = get(curr);
            Vector3 deriv = deriv(curr);

            double ds = pos.getPos().subtract(p.getPos()).dot(deriv.getVector2());

            ds = ds / deriv.getVector2().dot(deriv.getVector2());

            if(MathUtils.epsilonEquals(ds, 0)){
                break;
            }

            curr += (ds / length);

            if(curr < 0){
                //curr = 0;
            }
            if(curr > 1){
                //curr = 1;
            }
        }

        return (Math.max(0, Math.min(curr, 1)));
    }
}
