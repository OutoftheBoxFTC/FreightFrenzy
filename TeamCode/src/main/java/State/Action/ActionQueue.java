package State.Action;

import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

public class ActionQueue implements Action{
    private Queue<Action> actions = new LinkedList<>();
    private Action currentAction = null;

    @Override
    public void update() {
        if(currentAction != null){
            if(currentAction.shouldDeactivate()){
                currentAction = actions.poll();
                if(currentAction != null){
                    ActionController.addAction(currentAction);
                }
            }
        }else{
            if(!actions.isEmpty()){
                currentAction = actions.poll();
                ActionController.addAction(currentAction);
            }
        }
    }

    @Override
    public boolean shouldDeactivate() {
        return actions.isEmpty();
    }

    @Override
    public void onEnd() {
        if(currentAction != null){
            //ActionController.getInstance().terminateAction(currentAction);
        }
    }

    public void submitAction(Action action){
        actions.offer(action);
    }
}
