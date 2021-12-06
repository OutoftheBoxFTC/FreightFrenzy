package Utils.PathUtils.Markers;

import State.Action.Action;
import Utils.PathUtils.Path;

public class BasicPathMarker extends PathMarkerBase{
    public BasicPathMarker(double progress, Action action) {
        super(action);
        this.pathPos = progress;
    }

    @Override
    public void configure(Path path) {

    }
}
