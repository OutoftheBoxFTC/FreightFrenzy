package State.Action;

import Utils.OpmodeStatus;

public interface Action {

    default void initialize(){}

    void update();

    default void submit(){
        ActionController.addAction(this);
    }

    default void runBlocking(){
        while(OpmodeStatus.getOpmode().opModeIsActive() && !shouldDeactivate()) {
            update();
        }
    }

    default void onEnd(){}

    default boolean shouldDeactivate(){return false;}
}
