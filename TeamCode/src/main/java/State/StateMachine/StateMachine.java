package State.StateMachine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

import State.Action.Action;
import State.Action.ActionController;

public class StateMachine implements Action {
    int currState = -1;
    private ENDTYPE endtype;
    private List<Action> actions;
    private Action activeAction;
    private boolean deactivate = false;

    public StateMachine(List<Action> actions, ENDTYPE endtype){
        this.actions = actions;
        this.endtype = endtype;
    }

    @Override
    public void initialize() {

    }

    @Override
    public void update() {
        if(currState < 0){
            currState = 0;
            activeAction = actions.get(0);
            activeAction.submit();
        }else{
            if(activeAction.shouldDeactivate()){
                currState ++;
                if(currState >= actions.size()){
                    if(endtype == ENDTYPE.CONTINUE_LAST){
                        currState --;
                        return;
                    }else if(endtype == ENDTYPE.END_ALL){
                        deactivate = true;
                    }else{
                        currState = 0;
                    }
                }
                activeAction = actions.get(currState);
                activeAction.submit();
            }
        }
    }

    @Override
    public boolean shouldDeactivate() {
        return deactivate;
    }

    public enum ENDTYPE{
        LOOP,
        CONTINUE_LAST,
        END_ALL
    }
}