package Utils.PathUtils.Markers;

import MathSystems.Position;
import State.Action.Action;
import Utils.PathUtils.Path;
import Utils.PathUtils.PathUtil;

public class ClosestPointMarker extends PathMarkerBase {
    private Position position;

    public ClosestPointMarker(Position position, Action action) {
        super(action);
        this.position = position;
    }

    @Override
    public void configure(Path path) {
        this.pathPos = PathUtil.projectPosNew(position, path);
    }
}
