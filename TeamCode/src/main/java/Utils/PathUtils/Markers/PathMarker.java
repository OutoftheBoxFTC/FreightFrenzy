package Utils.PathUtils.Markers;

import State.Action.Action;
import Utils.PathUtils.Path;

public class PathMarker extends PathMarkerBase {
    double position;

    public PathMarker(double position, Action action) {
        super(action);
        this.position = position;
    }

    @Override
    public void configure(Path path) {
        this.pathPos = position / path.getSegments().size();
    }
}
