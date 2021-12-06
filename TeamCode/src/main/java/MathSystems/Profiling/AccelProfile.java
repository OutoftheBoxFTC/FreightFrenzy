package MathSystems.Profiling;

import java.util.ArrayList;
import java.util.Comparator;

public class AccelProfile {
    private ArrayList<AccelTimeState> states;
    private AccelTimeState state;

    public AccelProfile(AccelTimeState state){
        this.states = new ArrayList<>();
        states.add(state);
        this.state = state;
    }

    public void addState(AccelTimeState state){
        AccelTimeState tmp = this.state.getAt(this.state.dTime);
        tmp.accel = state.accel;
        tmp.time = this.state.time + this.state.dTime;
        tmp.dTime = state.dTime;
        this.state = tmp;
        states.add(tmp);
    }

    public AccelTimeState getAt(double time){
        AccelTimeState state = states.get(0);
        AccelTimeState prev = null;
        for(AccelTimeState tmp : states){
            if(tmp.time > time){
                break;
            }
            state = tmp;
        }
        return state.getAt((time - state.time));
    }

    public double getTimeLength(){
        return states.get(states.size()-1).time + states.get(states.size()-1).dTime;
    }

    public void addProfile(AccelProfile profile){
        for(AccelTimeState state : profile.getStates()){
            addState(state);
        }
    }

    public AccelProfile reversed(){
        AccelTimeState last = states.get(states.size()-1).getAt(0);
        last.accel = 0;
        last.vel = 0;
        last.pos = 0;
        last.time = 0;
        last.dTime = states.get(states.size()-1).dTime;
        AccelProfile tmp = new AccelProfile(last);
        for(int i = states.size()-2; i >= 0; i --){
            tmp.addState(states.get(i));
        }
        return tmp;
    }

    public AccelProfile flipped(){
        AccelTimeState first = start();
        AccelTimeState last = end();
        first.accel = last.accel;
        first.pos = last.pos;
        first.dTime = states.get(0).dTime;
        AccelProfile profileTmp = new AccelProfile(first);
        for(int i = states.size()-2; i >= 0; i --){
            AccelTimeState tmp = states.get(i).getAt(0);
            tmp.dTime = states.get(i).dTime;
            profileTmp.addState(tmp);
        }
        return profileTmp;
    }

    public AccelTimeState start(){
        return getAt(0);
    }

    public AccelTimeState end(){
        return getAt(getTimeLength());
    }

    public AccelProfile clone(){
        AccelProfile tmp = new AccelProfile(states.get(0));
        for(int i = 1; i < states.size(); i ++){
            tmp.addState(states.get(i));
        }
        return tmp;
    }

    public ArrayList<AccelTimeState> getStates() {
        states.sort(Comparator.comparingDouble(o -> o.time));
        return states;
    }
}
