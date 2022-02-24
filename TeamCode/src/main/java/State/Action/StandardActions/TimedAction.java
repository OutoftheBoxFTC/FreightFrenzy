package State.Action.StandardActions;

import State.Action.Action;

public abstract class TimedAction implements Action {
    private long timer = 0, delay = 0;

    public TimedAction(long delay){
        this.delay = delay;
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
