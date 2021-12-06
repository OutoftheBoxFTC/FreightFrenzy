package Utils.PathUtils;

import MathSystems.Position;
import MathSystems.Vector.Vector3;

public class PathUtil {

    public static double getPathLen(Path path){
        return getPathLen(path, 10);
    }

    public static double getPathLen(Path path, double samples){
        double len = 0;
        double inc = 1.0/samples;
        for(Segment s : path.getSegments()){
            for(double t = inc; t <= 1; t += inc){
                Position prev = s.get(t-inc);
                Position now = s.get(t);
                len += prev.getPos().distanceTo(now.getPos());
            }
        }
        return len;
    }

    public static double projectClosest(Position position, Path path){
        return projectClosest(position, path, 0.5);
    }

    public static double projectClosest(Position position, Path path, double guess){
        double curr = guess;
        double length = PathUtil.getPathLen(path);

        for(int i = 0; i < 250; i ++){
            Position p = path.get(curr);
            Vector3 deriv = path.deriv(curr);

            double ds = position.getPos().subtract(p.getPos()).dot(deriv.getVector2());

            ds = ds / deriv.getVector2().dot(deriv.getVector2());

            if(Math.abs(ds) < 0.001){
                break;
            }

            curr += (ds / length);

            if(curr < 0){
                break;
            }
            if(curr > 1){
                break;
            }
        }

        return Math.max(0, Math.min(curr, 1));
    }

    public static double project(Position position, Path path){
        return project(position, path, 0.01);
    }

    public static double project(Position position, Path path, double ds){
        double best = 0;
        double minDist = Double.MAX_VALUE;

        //System.out.println(position);

        for(double d = 0; d <= 1; d += (ds)){
            double val = projectClosest(position, path, d);
            double dist = path.get(val).getPos().distanceTo(position.getPos());

            //System.out.println("Test: " + d + " | " + val + " | " + dist);

            if(dist < minDist){
                minDist = dist;
                best = val;
            }
        }
        return best;
    }

    public static Position projectNew(Position position, Path path){
        double minDist = Double.MAX_VALUE;
        Position bestPos = Position.ZERO();

        for(Segment s : path.getSegments()){
            Position pos = s.project(position);
            if(pos.getPos().distanceTo(position.getPos()) < minDist){
                minDist = pos.getPos().distanceTo(position.getPos());
                bestPos.set(pos);
            }
        }

        return bestPos;
    }

    public static double projectPosNew(Position position, Path path){
        double minDist = Double.MAX_VALUE;
        Position bestPos = Position.ZERO();
        double projectPos = 0;

        for(Segment s : path.getSegments()){
            double d = s.projectPos(position);
            Position pos = s.get(d);
            if(pos.getPos().distanceTo(position.getPos()) < minDist){
                minDist = pos.getPos().distanceTo(position.getPos());
                bestPos.set(pos);
                projectPos = d;
            }
        }

        return projectPos;
    }
}
