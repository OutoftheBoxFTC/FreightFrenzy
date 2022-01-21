package Utils.GamepadEx;

import java.util.ArrayList;

import State.Action.Action;
import State.Action.ActionController;

public class Trigger {
    private double value, lastValue, threshold;
    private final ArrayList<GamepadValueCallback> onChangeStates;
    private Button button;

    public Trigger(){
        value = 0;
        lastValue = 0;
        onChangeStates = new ArrayList<>();
        threshold = 0.1;
        button = new Button();
    }

    public void update(double value){
        this.value = value;
        if(Math.abs(this.value - lastValue) > 0.001){
            for(GamepadValueCallback callback : onChangeStates){
                callback.call(this.value);
            }
            lastValue = value;
            button.update(this.value>=threshold);
        }
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public boolean pressed(){
        return value >= threshold;
    }

    public GamepadValueCallback bindOnValueChange(GamepadValueCallback action){
        onChangeStates.add(action);
        return action;
    }

    public GamepadCallback bindOnPress(GamepadCallback action){
        button.bindOnPress(action);
        return action;
    }

    public GamepadCallback bindOnRelease(GamepadCallback action){
        button.bindOnRelease(action);
        return action;
    }
}
