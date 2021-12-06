package Utils.PathUtils.Markers;

import State.Action.Action;
import State.Action.ActionController;
import Utils.PathUtils.Path;

public abstract class PathMarkerBase {
    public double pathPos;
    private Action action;

    public PathMarkerBase(Action action){
        this.action = action;
    }

    public abstract void configure(Path path);

    public void call(){
        ActionController.addAction(action);
    }
}
