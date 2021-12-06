package Utils.PathUtils;

import MathSystems.Position;
import MathSystems.Vector.Vector3;
import State.Action.Action;
import Utils.PathUtils.Markers.BasicPathMarker;
import Utils.PathUtils.Markers.ClosestPointMarker;
import Utils.PathUtils.Markers.PathMarker;
import Utils.PathUtils.Markers.PathMarkerBase;

import java.util.ArrayList;

public class ContinousPathBuilder {
    private ArrayList<Segment> segments = new ArrayList<>();
    private ArrayList<PathMarkerBase> markers = new ArrayList<>();
    private Position lastStart;

    public ContinousPathBuilder(Position start){
        this.lastStart = start;
    }

    public ContinousPathBuilder lineTo(Position end){
        segments.add(new Line(lastStart, end));
        lastStart = end;
        return this;
    }

    public ContinousPathBuilder addPathMarker(Action action){
        markers.add(new PathMarker(segments.size(), action));
        return this;
    }

    public ContinousPathBuilder addPathMarker(double progress, Action action){
        markers.add(new PathMarker(segments.size() * progress, action));
        return this;
    }

    public ContinousPathBuilder addClosestPointMarker(Position position, Action action){
        markers.add(new ClosestPointMarker(position, action));
        return this;
    }

    public ContinousPathBuilder addOnEndpoint(Action action){
        markers.add(new BasicPathMarker(1, action));
        return this;
    }

    public ContinousPathBuilder addOnBeginning(Action action){
        markers.add(new BasicPathMarker(0, action));
        return this;
    }

    public Path build(){
        ArrayList<Segment> newSegments = new ArrayList<>();
        newSegments.add(new Spline(segments.get(0).get(0), segments.get(0).get(1), segments.get(0).deriv(0), segments.get(0).deriv(1), Vector3.ZERO(), segments.get(0).secondDeriv(1)));
        for(int i = 1; i < segments.size(); i ++){
            Segment prev = segments.get(i-1);
            Segment now = segments.get(i);
            if(i < segments.size()-1) {
                newSegments.add(new Spline(prev.get(1), now.get(1), prev.deriv(1), now.deriv(1), prev.secondDeriv(1), now.secondDeriv(1)));
            }else{
                newSegments.add(new Spline(prev.get(1), now.get(1), prev.deriv(1), Vector3.ZERO(), prev.secondDeriv(1), Vector3.ZERO()));
            }
        }
        Path p = new Path(newSegments);
        for(PathMarkerBase marker : markers){
            marker.configure(p);
        }
        return p;
    }
}
