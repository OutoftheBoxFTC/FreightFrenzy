package Hardware.Pipelines;

import com.acmerobotics.dashboard.config.Config;

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
public class LineFinderPipeline extends OpenCvPipeline {
    public static double MIN = 180, MAX = 255, HEIGHT = 16;

    private double y = 0;

    private double realY = 0;

    public double pitchOffset = -15;

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
            Point centre = getCenter(bestRect);
            double pitch = Math.toDegrees(Math.atan2((centre.x) - ((input.width()/2.0) - 0.5), calcPinholeHor(60, input.width(), input.height())));
            realY = HEIGHT * Math.tan(Math.toRadians(pitch - pitchOffset));
        }else{
            y = -1;
            realY = -1;
        }

        Imgproc.cvtColor(input, input, Imgproc.COLOR_GRAY2RGB);
        Imgproc.rectangle(input, bestRect, new Scalar(0, 255, 0), -1);

        return input;
    }

    public double getY() {
        return y;
    }

    private static double calcPinholeHor(double fov, double imageWidth, double imageHeight){
        double diagonalView = Math.toRadians(fov);
        Fraction aspectFraction = new Fraction(imageWidth, imageHeight);
        int horizontalRatio = aspectFraction.getNumerator();
        int verticalRatio = aspectFraction.getDenominator();
        double diagonalAspect = Math.hypot(horizontalRatio, verticalRatio);
        double horizontalView = Math.atan(Math.tan(diagonalView / 2) * (horizontalRatio / diagonalAspect)) * 2;

        return imageWidth / (2 * Math.tan(horizontalView / 2));
    }

    private static Point getCenter(Rect rect){
        if (rect == null) {
            return new Point(0, 0);
        }
        return new Point(rect.x + rect.width / 2.0, rect.y + rect.height / 2.0);
    }
}
