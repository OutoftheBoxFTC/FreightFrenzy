package Utils.PathUtils;

import MathSystems.Position;
import MathSystems.Vector.Vector3;
import Utils.PathUtils.Markers.PathMarkerBase;

import java.util.ArrayList;

public class Path{
    private final ArrayList<Segment> segments;
    private final ArrayList<PathMarkerBase> markers;

    public Path(ArrayList<Segment> segments){
        this.segments = segments;
        this.markers = new ArrayList<>();
    }

    public Path(ArrayList<Segment> segments, ArrayList<PathMarkerBase> markers){
        this.segments = segments;
        this.markers = markers;
    }

    public ArrayList<Segment> getSegments() {
        return segments;
    }

    public Position get(double t){
        int i = (int)(t / (1.0 / segments.size()));
        double r = t % (1.0 / segments.size());

        if(i >= segments.size()){
            i = segments.size() - 1;
            r = 1;
        }

        if(i < 0){
            i = 0;
            r = 0;
        }

        return segments.get(i).get(r);
    }

    public Vector3 deriv(double t){
        int i = (int)(t / (1.0 / segments.size()));
        double r = t % (1.0 / segments.size());

        if(i >= segments.size()){
            i = segments.size() - 1;
            r = 1;
        }

        if(i < 0){
            i = 0;
            r = 0;
        }

        return segments.get(i).deriv(r);
    }

    public Position getEndpoint(){
        return get(1);
    }

    public ArrayList<PathMarkerBase> getMarkers() {
        return markers;
    }
}
