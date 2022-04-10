package Hardware.Pipelines;

import com.acmerobotics.dashboard.config.Config;

import org.checkerframework.checker.units.qual.A;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvPipeline;

import java.util.ArrayList;
import java.util.List;

@Config
public class DuckPipeline extends OpenCvPipeline {

    public static double Hmin = 0, Hmax = 50, Smin = 160, Smax = 255, Vmin = 0, Vmax = 255;

    private double position = -1;

    @Override
    public Mat processFrame(Mat input) {

        /**
        Imgproc.rectangle(input, r1, new Scalar(255, 0, 0));
        Imgproc.rectangle(input, r2, new Scalar(0, 255, 0));
        Imgproc.rectangle(input, r3, new Scalar(0, 0, 255));
        */

        Mat hsv = new Mat();
        Imgproc.cvtColor(input, hsv, Imgproc.COLOR_RGB2HSV);

        Mat green = new Mat();
        Core.inRange(hsv, new Scalar(Hmin, Smin, Vmin), new Scalar(Hmax, Smax, Vmax), green);

        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(green, contours, new Mat(), Imgproc.RETR_TREE, Imgproc.RETR_TREE);

        double area = 0;
        MatOfPoint contour = new MatOfPoint();
        for(MatOfPoint c : contours){
            double tmp = Imgproc.contourArea(c);
            if(tmp > area){
                area = tmp;
                contour = c;
            }
        }

        Imgproc.cvtColor(green, green, Imgproc.COLOR_GRAY2RGB);

        if(area != 0){
            Rect r = Imgproc.boundingRect(contour);
            if(r.area() > 30) {
                Imgproc.rectangle(green, r, new Scalar(255, 0, 0));
                position = LineFinderPipeline.getCenter(r).x;
            }
        }

        return green;
    }

    public double getPosition() {
        return position;
    }
}
