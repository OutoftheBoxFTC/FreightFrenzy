package Hardware.Pipelines;

import com.acmerobotics.dashboard.config.Config;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvPipeline;

import java.util.ArrayList;
import java.util.List;

@Config
public class LineFinderPipeline extends OpenCvPipeline {
    public static double MIN = 180, MAX = 255, HEIGHT = 15.5;

    private double y = 0;

    private double realY = 0;

    private double pitch = 0;

    public double pitchOffset = -15;

    private long last = 0;
    public double fps = 0;

    private double zoomFactor = 1;

    private Size size;

    @Override
    public void init(Mat mat) {
        size = mat.size();
    }

    @Override
    public Mat processFrame(Mat input) {
        Mat inClone = input.clone();
        Imgproc.cvtColor(input, input, Imgproc.COLOR_RGB2GRAY);
        Imgproc.threshold(input, input, MIN, MAX, Imgproc.THRESH_BINARY);

        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(input, contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        double height = 0;
        Rect bestRect = new Rect(0, 0, 0, 0);
        for(MatOfPoint p : contours){
            Rect r = Imgproc.boundingRect(p);
            if(r.height > height && r.height > 180){
                height = r.height;
                bestRect = r;
            }
        }

        if(height != 0) {
            y = bestRect.x;
            Point centre = getCenter(bestRect);
            pitch = Math.toDegrees(Math.atan2((centre.x) - ((input.width()/2.0) - 0.5), calcPinholeHor(60, input.width(), input.height())));
            realY = HEIGHT * Math.tan(Math.toRadians(pitch - pitchOffset));
        }else{
            y = -1;
            realY = -1;
            pitch = 0;
        }

        Imgproc.cvtColor(input, input, Imgproc.COLOR_GRAY2RGB);
        Imgproc.rectangle(input, bestRect, new Scalar(0, 255, 0), -1);

        long now = System.currentTimeMillis();
        if(last != 0){
            long delta = now - last;
            fps = 1/((delta) / 1000.0);
        }
        last = now;

        inClone = inClone.submat(new Rect(new Point(size.width * zoomFactor, size.height * zoomFactor), new Point(
                size.width - (size.width * zoomFactor), size.height - (size.height * zoomFactor))));
        Imgproc.resize(inClone, inClone, size);

        return inClone;
    }

    public double getY() {
        return y;
    }

    public double getRealY() {
        return realY;
    }

    public double getPitch() {
        return pitch;
    }

    public double getFps() {
        return fps;
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

    public void setZoomFactor(double zoomFactor) {
        this.zoomFactor = zoomFactor;
    }

    public static Point getCenter(Rect rect){
        if (rect == null) {
            return new Point(0, 0);
        }
        return new Point(rect.x + rect.width / 2.0, rect.y + rect.height / 2.0);
    }
}
