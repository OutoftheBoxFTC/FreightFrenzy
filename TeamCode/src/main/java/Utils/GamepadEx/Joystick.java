package Utils.GamepadEx;

import java.util.ArrayList;

import State.Action.Action;
import State.Action.ActionController;

public class Joystick {
    private double lastX, lastY, x, y, deadzone;
    private final ArrayList<GamepadValueCallback> onXChange, onYChange;

    public Joystick(){
        this.onXChange = new ArrayList<>();
        this.onYChange = new ArrayList<>();
        this.lastX = 0;
        this.lastY = 0;
        this.x = 0;
        this.y = 0;
        deadzone = 0.1;
    }

    public void update(double x, double y){
        if(Math.abs(x) < deadzone){
            x = 0;
        }
        if(Math.abs(y) < deadzone){
            y = 0;
        }
        this.x = x;
        this.y = y;
        if(Math.abs(this.x - this.lastX) < 0.001){
            for(GamepadValueCallback callback : onXChange){
                callback.call(this.x);
            }
        }
        if(Math.abs(this.y - this.lastY) < 0.001){
            for(GamepadValueCallback callback : onYChange){
                callback.call(this.y);
            }
        }
    }

    public void setDeadzone(double deadzone){
        this.deadzone = deadzone;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public GamepadValueCallback bindOnXChange(GamepadValueCallback action){
        this.onXChange.add(action);
        return action;
    }

    public GamepadValueCallback bindOnYChange(GamepadValueCallback action){
        this.onYChange.add(action);
        return action;
    }
}
