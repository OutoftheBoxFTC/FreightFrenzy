package Drive.BasicDrive;

import Hardware.HardwareSystems.FFSystems.DrivetrainSystem;
import MathSystems.Angle;
import MathSystems.MathUtils;
import MathSystems.Position;
import MathSystems.Vector.Vector2;
import MathSystems.Vector.Vector3;
import MathSystems.Vector.Vector4;
import State.Action.Action;
import Utils.PathUtils.Path;
import Utils.PathUtils.PathUtil;

public class PurePursuit implements Action {
    private DrivetrainSystem drivetrainSystem;
    private Position position;
    private Path path;
    private double speed, radius;

    public PurePursuit(DrivetrainSystem drivetrainSystem, Position position, Path path, double speed, double radius){
        this.position = position;
        this.path = path;
        this.speed = speed;
        this.drivetrainSystem = drivetrainSystem;
    }

    public PurePursuit(DrivetrainSystem drivetrainSystem, Position position, Path path){
        this(drivetrainSystem, position, path, 1, 10);
    }

    @Override
    public void update() {
        int besti = path.getSegments().size()-1;
        double minDist = Double.MAX_VALUE;
        for(int i = 0; i < path.getSegments().size(); i ++) {
            Vector2 solution1 = Vector2.ZERO(), solution2 = Vector2.ZERO();
            int solutions = findLineCircleIntersections(position.getX(), position.getY(), radius, path.getSegments().get(i).get(0).getPos(), path.getSegments().get(i).get(1).getPos(), solution1, solution2);
            if(solutions == 1){
                double dist = solution1.distanceTo(path.getEndpoint().getPos());
                if(dist < minDist){
                    besti = i;
                    minDist = dist;
                }
            }
            if(solutions == 2){
                if(solution1.distanceTo(path.getSegments().get(i).get(1).getPos()) < solution2.distanceTo(path.getSegments().get(i).get(1).getPos())){
                    minDist = solution1.distanceTo(path.getEndpoint().getPos());
                }else{
                    minDist = solution2.distanceTo(path.getEndpoint().getPos());
                }
                besti = i;
            }
        }

        Position endPos = path.getEndpoint();
        if(minDist == Double.MAX_VALUE){
            endPos = path.getEndpoint();
        }else{
            Vector2 solution1 = Vector2.ZERO(), solution2 = Vector2.ZERO();
            int solutions = findLineCircleIntersections(position.getX(), position.getY(), radius, path.getSegments().get(besti).get(0).getPos(), path.getSegments().get(besti).get(1).getPos(), solution1, solution2);
            if(solutions == 1){
                endPos = new Position(solution1, path.getEndpoint().getR());
            }else if(solutions == 2){
                if(solution1.distanceTo(path.getSegments().get(besti).get(1).getPos()) < solution2.distanceTo(path.getSegments().get(besti).get(1).getPos())){
                    endPos = new Position(solution1, path.getEndpoint().getR());
                }else{
                    endPos = new Position(solution2, path.getEndpoint().getR());
                }
            }
        }

        Vector2 diff = endPos.getPos().subtract(position.getPos());
        Angle angle = Angle.radians(Math.atan2(diff.getB(), diff.getA()));

        double rot = position.getR().radians() + (speed > 0 ? 0 : Math.toRadians(180));
        double tau = (2 * Math.PI);
        rot = ((rot % tau) + tau) % tau;

        Angle angleDiff = MathUtils.getRotDist(angle, Angle.radians(rot));

        if(Math.abs(angleDiff.degrees()) > 5) {
            drivetrainSystem.setPower(new Vector3(0, speed, Math.signum(angleDiff.degrees()) * 0.4));
        }
    }

    private int findLineCircleIntersections(
            double cx, double cy, double radius,
            Vector2 point1, Vector2 point2, Vector2 solution1, Vector2 solution2)
    {
        double dx, dy, A, B, C, det, t;

        dx = point2.getA() - point1.getA();
        dy = point2.getB() - point1.getB();

        A = dx * dx + dy * dy;
        B = 2 * (dx * (point1.getA() - cx) + dy * (point1.getB() - cy));
        C = (point1.getA() - cx) * (point1.getA() - cx) +
                (point1.getB() - cy) * (point1.getB() - cy) -
                radius * radius;

        det = B * B - 4 * A * C;
        if ((A <= 0.0000001) || (det < 0))
        {
            // No real solutions.
            return 0;
        }
        else if (det == 0)
        {
            // One solution.
            t = -B / (2 * A);
            Vector2 intersection1 =
                    new Vector2(point1.getA() + t * dx, point1.getB() + t * dy);
            Vector2 intersection2 = new Vector2(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
            solution1.set(intersection1);
            return 1;
        }
        else
        {
            // Two solutions.
            t = (float)((-B + Math.sqrt(det)) / (2 * A));
            Vector2 intersection1 =
                    new Vector2(point1.getA() + t * dx, point1.getB() + t * dy);
            t = (float)((-B - Math.sqrt(det)) / (2 * A));
            Vector2 intersection2 =
                    new Vector2(point1.getA() + t * dx, point1.getB() + t * dy);
            solution1.set(intersection1);
            solution2.set(intersection2);
            return 2;
        }
    }
}
