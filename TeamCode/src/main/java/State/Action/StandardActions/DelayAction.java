package State.Action.StandardActions;

import State.Action.Action;

public class DelayAction implements Action {
    private long delay, timer;

    public DelayAction(long delay){
        this.delay = delay;
    }

    @Override
    public void update() {

    }

    @Override
    public void initialize() {
        this.timer = System.currentTimeMillis() + delay;
    }

    @Override
    public boolean shouldDeactivate() {
        return System.currentTimeMillis() > timer;
    }
}
