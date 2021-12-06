package Utils.PathUtils;

import MathSystems.Position;

import java.util.ArrayList;

public class PathBuilder {
    private ArrayList<Segment> segments = new ArrayList<>();
    private Position lastStart;

    public PathBuilder(Position start){
        this.lastStart = start;
    }

    public PathBuilder lineTo(Position end){
        segments.add(new Line(lastStart, end));
        lastStart = end;
        return this;
    }

    public Path build(){
        return new Path(segments);
    }
}
