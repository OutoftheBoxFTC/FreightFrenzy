package State.Action;

import java.util.ArrayList;
import java.util.List;

public class ActionController {
    private static final ActionController instance = new ActionController();
    private final ArrayList<Action> activeActions, queuedActions, deactivatedActions, toInitialize;

    public static ActionController getInstance() {
        return instance;
    }

    public ActionController(){
        activeActions = new ArrayList<>();
        queuedActions = new ArrayList<>();
        deactivatedActions = new ArrayList<>();
        toInitialize = new ArrayList<>();
    }

    public static void initialize(){
        instance.activeActions.clear();
        instance.queuedActions.clear();
        instance.deactivatedActions.clear();
        instance.toInitialize.clear();
    }

    public static void update(){
        instance.internalUpdate();
    }

    public void internalUpdate(){
        synchronized (queuedActions) {
            toInitialize.addAll(queuedActions);
            queuedActions.clear();
        }

        for(Action action : toInitialize){
            if(!activeActions.contains(action) && !deactivatedActions.contains(action)) {
                action.initialize();
                activeActions.add(action);
            }
        }

        toInitialize.clear();
        synchronized (deactivatedActions) {
            for (Action action : activeActions) {
                action.update();
                if (action.shouldDeactivate()) {
                    deactivatedActions.add(action);
                }
            }

            for (Action action : deactivatedActions) {
                action.onEnd();
            }

            activeActions.removeAll(deactivatedActions);

            deactivatedActions.clear();
        }
    }

    public static void addAction(Action action){
        synchronized (instance.queuedActions){
            instance.queuedActions.add(action);
        }
    }

    public static void addActions(List<Action> actions){
        synchronized (instance.queuedActions) {
            instance.queuedActions.addAll(actions);
        }
    }

    public void terminateAction(Action action){
        synchronized (instance.deactivatedActions) {
            deactivatedActions.add(action);
        }
    }
}
