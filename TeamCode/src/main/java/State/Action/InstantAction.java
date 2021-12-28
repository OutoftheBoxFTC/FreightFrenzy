package State.Action;

public abstract class InstantAction implements Action {
    @Override
    public boolean shouldDeactivate() {
        return true;
    }
}
