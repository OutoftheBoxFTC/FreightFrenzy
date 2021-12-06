package Utils.PathUtils;

import MathSystems.Position;
import MathSystems.Vector.Vector3;

public interface Segment {

    Position get(double t);

    Vector3 deriv(double t);

    Vector3 secondDeriv(double t);

    Position project(Position pos);

    double projectPos(Position pos);
}
