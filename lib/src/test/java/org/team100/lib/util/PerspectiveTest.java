package org.team100.lib.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import edu.wpi.first.cscore.CameraServerCvJNI;

class PerspectiveTest {

    public PerspectiveTest() throws IOException {
        CameraServerCvJNI.forceLoad();
    }

    Mat calibrationSquareInWorldCoordinatesMeters() {
       // dst is the floor relative to the robot (robot is zero, x ahead, y to the left)
       // this is a one-meter square centered on zero in Y, centered on 2 in X
       List<Point> dstpts = new ArrayList<Point>();
       dstpts.add(new Point(1.5, 0.5));
       dstpts.add(new Point(1.5, -0.5));
       dstpts.add(new Point(2.5, 0.5));
       dstpts.add(new Point(2.5, -0.5));
       return Converters.vector_Point2f_to_Mat(dstpts);
    }

    Mat calibrationSquareInCameraPixels() {
        // src is camera pixels (upper left is zero, x to the right, y down)
        // say the camera is 832x616
        // this is the image of the calibration square as seen by the camera
        // width is 832 so center is 416
        // height is 616 so center is 308.
        List<Point> srcpts = new ArrayList<Point>();
        //These numbers were gotten from putting getting points on the field from a camera 30 degrees down 
        // You can also calculate this using the getX() and getY() functions
        srcpts.add(new Point(389, 643));
        srcpts.add(new Point(389, 189));
        srcpts.add(new Point(498, 555));
        srcpts.add(new Point(498, 277));
        return Converters.vector_Point2f_to_Mat(srcpts);
    }


    @Test
    void testPerspective() {

        Mat src = calibrationSquareInCameraPixels();
        Mat dst = calibrationSquareInWorldCoordinatesMeters();

        Mat transform = Imgproc.getPerspectiveTransform(src, dst);
        System.out.println(transform.dump());

        // try feeding one of the sample points above into the transform
        int xPixels = 389;
        int yPixels = 643;
        Mat src1 = new Mat(3,1,CvType.CV_64F);
        src1.put(0,0,xPixels);
        src1.put(1,0,yPixels);
        src1.put(2,0,1); // homogeneous coordinates, this is always 1
        Mat dst1 = transform.matMul(src1);
        dst1 = dst1.mul(Mat.ones(3,1,CvType.CV_64F), 1/dst1.get(2,0)[0]);
        // this is the real-world coordinate.
        System.out.println(dst1.dump());
        assertEquals(1.5, dst1.get(0,0)[0], 0.001);
        assertEquals(0.5, dst1.get(1,0)[0], 0.001);
    }

}
