package Utils.GamepadEx;

import java.util.ArrayList;

import State.Action.Action;
import State.Action.ActionController;

public class Button {
    private boolean state, toggle, lastState, pressed, released;
    private ArrayList<Action> pressedStates, releasedStates;

    public Button(){
        this.state = false;
        this.toggle = false;
        this.lastState = false;
        this.pressed = false;
        this.released = false;
        pressedStates = new ArrayList<>();
        releasedStates = new ArrayList<>();
    }

    public void update(boolean state){
        this.state = state;
        this.pressed = false;
        if(state != lastState){
            toggle = !toggle;
            if(state){
                pressed = true;
                ActionController.addActions(pressedStates);
            }else{
                released = true;
                ActionController.addActions(releasedStates);
            }
            lastState = state;
        }
    }
    
    public Action bindOnPress(Action action){
        this.pressedStates.add(action);
        return action;
    }
    
    public Action bindOnRelease(Action action){
        this.releasedStates.add(action);
        return action;
    }
    
    public boolean pressed(){
        return state;
    }
    
    public boolean toggled(){
        return toggle;
    }
    
    public boolean justPressed(){
        return pressed;
    }
    
    public boolean released(){
        return released;
    }
}
