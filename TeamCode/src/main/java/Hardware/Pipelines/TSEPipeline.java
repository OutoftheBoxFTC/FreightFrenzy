package Hardware.Pipelines;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.hardware.PIDCoefficients;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvPipeline;

@Config
public class TSEPipeline extends OpenCvPipeline {
    public static Point bl1 = new Point(300, 0), tr1 = new Point(240, 30),
            bl2 = new Point(270, 90), tr2 = new Point(210, 140),
            bl3 = new Point(250, 200), tr3 = new Point(165, 240);

    public static double Hmin = 10, Hmax = 30, Smin = 100, Smax = 255, Vmin = 0, Vmax = 255;

    @Override
    public Mat processFrame(Mat input) {
        Rect r1 = new Rect(bl1, tr1), r2 = new Rect(bl2, tr2), r3 = new Rect(bl3, tr3);

        /**
        Imgproc.rectangle(input, r1, new Scalar(255, 0, 0));
        Imgproc.rectangle(input, r2, new Scalar(0, 255, 0));
        Imgproc.rectangle(input, r3, new Scalar(0, 0, 255));
        */

        Mat hsv = new Mat();
        Imgproc.cvtColor(input, hsv, Imgproc.COLOR_RGB2HSV);

        Mat green = new Mat();
        Core.inRange(hsv, new Scalar(Hmin, Smin, Vmin), new Scalar(Hmax, Smax, Vmax), green);

        Mat subm1 = green.submat(r1);
        Mat subm2 = green.submat(r2);
        Mat subm3 = green.submat(r3);

        Scalar s1 = Core.mean(subm1);
        Scalar s2 = Core.mean(subm2);
        Scalar s3 = Core.mean(subm3);

        int position = -1;
        if(s1.val[0] > s2.val[0] && s1.val[0] > s3.val[0]){
            position = 0;
        }

        if(s2.val[0] > s1.val[0] && s2.val[0] > s3.val[0]){
            position = 1;
        }

        if(s3.val[0] > s2.val[0] && s3.val[0] > s1.val[0]){
            position = 2;
        }

        if(position == 0){
            Imgproc.rectangle(input, r1, new Scalar(0, 255, 0), 3);
            Imgproc.rectangle(input, r2, new Scalar(255, 0, 0), 3);
            Imgproc.rectangle(input, r3, new Scalar(255, 0, 0), 3);
        }
        if(position == 1){
            Imgproc.rectangle(input, r1, new Scalar(255, 0, 0), 3);
            Imgproc.rectangle(input, r2, new Scalar(0, 255, 0), 3);
            Imgproc.rectangle(input, r3, new Scalar(255, 0, 0), 3);
        }
        if(position == 2){
            Imgproc.rectangle(input, r1, new Scalar(255, 0, 0), 3);
            Imgproc.rectangle(input, r2, new Scalar(255, 0, 0), 3);
            Imgproc.rectangle(input, r3, new Scalar(0, 255, 0), 3);
        }
        if(position == -1){
            Imgproc.rectangle(input, r1, new Scalar(255, 0, 0), 3);
            Imgproc.rectangle(input, r2, new Scalar(255, 0, 0), 3);
            Imgproc.rectangle(input, r3, new Scalar(255, 0, 0), 3);
        }

        return input;
    }
}
