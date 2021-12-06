package Utils;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import java.util.ArrayList;

import State.Action.Action;
import State.Action.ActionController;

public class OpmodeStatus {
    private static final OpmodeStatus instance = new OpmodeStatus();
    private final ArrayList<Action> startActions;
    private LinearOpMode opmode;

    private OpmodeStatus(){
        opmode = null;
        startActions = new ArrayList<>();
    }

    public static void attach(LinearOpMode opmode){
        instance.opmode = opmode;
        instance.startActions.clear();
    }

    public static boolean opmodeActive(){
        if(instance.opmode != null){
            return instance.opmode.isStopRequested() || !instance.opmode.opModeIsActive();
        }
        return false;
    }

    public static LinearOpMode getOpmode() {
        return instance.opmode;
    }

    public static void triggerStartActions(){
        ActionController.addActions(instance.startActions);
    }

    public static void bindOnStart(Action action){
        instance.startActions.add(action);
    }
}
