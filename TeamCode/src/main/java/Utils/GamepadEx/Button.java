package Utils.GamepadEx;

import java.util.ArrayList;

import State.Action.Action;
import State.Action.ActionController;

public class Button {
    private boolean state, toggle, lastState, pressed, released;
    private ArrayList<GamepadCallback> pressedStates, releasedStates;

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
            if(state){
                toggle = !toggle;
                pressed = true;
                for(GamepadCallback callback : pressedStates){
                    callback.call();
                }
            }else{
                released = true;
                for(GamepadCallback callback : releasedStates){
                    callback.call();
                }
            }
            lastState = state;
        }
    }
    
    public GamepadCallback bindOnPress(GamepadCallback action){
        this.pressedStates.add(action);
        return action;
    }
    
    public GamepadCallback bindOnRelease(GamepadCallback action){
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
