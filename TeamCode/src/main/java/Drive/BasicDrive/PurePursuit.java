package Drive.BasicDrive;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.geometry.Pose2d;

import Hardware.HardwareSystems.FFSystems.DrivetrainSystem;
import MathSystems.Angle;
import MathSystems.MathUtils;
import MathSystems.Position;
import MathSystems.Vector.Vector2;
import MathSystems.Vector.Vector3;
import MathSystems.Vector.Vector4;
import RoadRunner.drive.SampleTankDrive;
import State.Action.Action;
import Utils.PathUtils.Path;
import Utils.PathUtils.PathUtil;
import Utils.PathUtils.Segment;

public class PurePursuit implements Action {
    private SampleTankDrive drivetrainSystem;
    private Position position;
    private Path path;
    private double speed, radius;

    public PurePursuit(SampleTankDrive drivetrainSystem, Position position, Path path, double speed, double radius){
        this.position = position;
        this.path = path;
        this.speed = speed;
        this.drivetrainSystem = drivetrainSystem;
        this.radius = radius;
    }

    public PurePursuit(SampleTankDrive drivetrainSystem, Position position, Path path){
        this(drivetrainSystem, position, path, 1, 5);
    }

    @Override
    public void update() {

        TelemetryPacket packet = new TelemetryPacket();
        packet.fieldOverlay().setStroke("blue");
        for(Segment s : path.getSegments()){
            packet.fieldOverlay().strokeLine(s.get(0).getX(), s.get(0).getY(), s.get(1).getX(), s.get(1).getY());
        }
        packet.fieldOverlay().setStroke("red");
        packet.fieldOverlay().strokeCircle(position.getX(), position.getY(), radius);
        packet.fieldOverlay().setFill("black");

        int besti = path.getSegments().size()-1;
        for(int i = 0; i < path.getSegments().size(); i ++) {
            Vector2 solution1 = Vector2.ZERO(), solution2 = Vector2.ZERO();
            int solutions = findLineCircleIntersections(position.getX(), position.getY(), radius, path.getSegments().get(i).get(0).getPos(), path.getSegments().get(i).get(1).getPos(), solution1, solution2);
            if(solutions != 0){
                besti = i;
            }
        }

        Position endPos = path.getEndpoint();
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

        packet.fieldOverlay().fillRect(endPos.getX(), endPos.getY(), 1, 1);

        Vector2 diff = endPos.getPos().subtract(position.getPos());
        Angle angle = Angle.radians(Math.atan2(diff.getA(), diff.getB()));

        double rot = position.getR().radians() + (speed > 0 ? 0 : Math.toRadians(180));
        double tau = (2 * Math.PI);
        rot = ((rot % tau) + tau) % tau;

        Angle angleDiff = MathUtils.getRotDist(angle, Angle.radians(rot));

        angleDiff = Angle.radians(((angleDiff.radians() - (Math.PI/2)  % tau) + tau) % tau);

        angleDiff = MathUtils.getRotDist(Angle.ZERO(), angleDiff);

        if(Math.abs(angleDiff.degrees()) > 5) {
            drivetrainSystem.setDrivePower(new Pose2d(0, 0, Math.signum(angleDiff.degrees()) * -1));
            //drivetrainSystem.setPower(new Vector3(0, speed, Math.signum(angleDiff.degrees()) * 0.4));
        }else{
            //drivetrainSystem.setDrivePower(new Pose2d(0.6, 0, Math.signum(angleDiff.degrees()) * -0.3));
        }

        if(position.getPos().distanceTo(path.getEndpoint().getPos()) < 2 || Math.abs(angleDiff.degrees()) > 90){
            //deactivateNow();
        }
        packet.put("Angle", angleDiff.degrees());
        FtcDashboard.getInstance().sendTelemetryPacket(packet);
        FtcDashboard.getInstance().getTelemetry().update();
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
