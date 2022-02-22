package Hardware.Pipelines;

import com.acmerobotics.dashboard.config.Config;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvPipeline;

import java.util.ArrayList;
import java.util.List;

@Config
public class LineFinderPipeline extends OpenCvPipeline {
    public static double MIN = 180, MAX = 255;

    private double y = 0;

    @Override
    public Mat processFrame(Mat input) {
        Imgproc.cvtColor(input, input, Imgproc.COLOR_RGB2GRAY);
        Imgproc.threshold(input, input, MIN, MAX, Imgproc.THRESH_BINARY);

        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(input, contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        double height = 0;
        Rect bestRect = new Rect(0, 0, 0, 0);
        for(MatOfPoint p : contours){
            Rect r = Imgproc.boundingRect(p);
            if(r.height > height && r.height > 200){
                height = r.height;
                bestRect = r;
            }
        }

        if(height != 0) {
            y = bestRect.x;
        }else{
            y = -1;
        }

        Imgproc.cvtColor(input, input, Imgproc.COLOR_GRAY2RGB);
        Imgproc.rectangle(input, bestRect, new Scalar(0, 255, 0), -1);

        return input;
    }

    public double getY() {
        return y;
    }
}
