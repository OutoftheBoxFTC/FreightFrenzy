package Hardware.SmartDevices.SmartCV;

import org.opencv.core.Mat;
import org.openftc.easyopencv.OpenCvPipeline;

import java.util.ArrayList;

import State.Action.Action;
import State.Action.ActionController;

public abstract class SmartEOCVPipeline extends OpenCvPipeline {
    private final ArrayList<Action> pipelineActions;

    public SmartEOCVPipeline(){
        pipelineActions = new ArrayList<>();
    }

    @Override
    public Mat processFrame(Mat input) {
        Mat result = process(input);
        ActionController.addActions(pipelineActions);
        return result;
    }

    public abstract Mat process(Mat input);

    public void bind(Action action){
        pipelineActions.add(action);
    }
}
