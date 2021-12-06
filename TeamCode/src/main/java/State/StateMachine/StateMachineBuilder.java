package State.StateMachine;

import java.util.ArrayList;
import java.util.List;

import State.Action.Action;

public class StateMachineBuilder {
    private List<Action> actions;
    private StateMachine.ENDTYPE endtype;

    public StateMachineBuilder(){
        actions = new ArrayList<>();
        endtype = StateMachine.ENDTYPE.END_ALL;
    }

    public StateMachineBuilder then(Action action) {
        actions.add(action);
        return this;
    }

    public StateMachine loop(){
        this.endtype = StateMachine.ENDTYPE.LOOP;
        return new StateMachine(actions, endtype);
    }

    public StateMachine continueLast(){
        this.endtype = StateMachine.ENDTYPE.CONTINUE_LAST;
        return new StateMachine(actions, endtype);
    }

    public StateMachine stopAll(){
        return new StateMachine(actions, endtype);
    }

}
