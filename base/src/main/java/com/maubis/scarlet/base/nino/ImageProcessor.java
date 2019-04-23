package com.maubis.scarlet.base.nino;

import android.graphics.Bitmap;
import android.util.Log;

import com.scanlibrary.PolygonView;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ImageProcessor {

    public MatOfPoint findContours(Mat edges){
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        hierarchy.release();

        double maxArea = 0;
        int maxAreaIndex = 0;
        for(int i = 0; i < contours.size(); i++){
            MatOfPoint matOfPoint = contours.get(i);
            double contourArea = Imgproc.contourArea(matOfPoint);
            if(contourArea > maxArea){
                maxArea = contourArea;
                maxAreaIndex = i;
            }
        }

        MatOfPoint maxContour = contours.get(maxAreaIndex);
        return maxContour;
    }

    public MatOfPoint2f findApproxCurve(MatOfPoint maxContour){
        MatOfPoint2f approxCurve = new MatOfPoint2f();
        MatOfPoint2f approxMat = new MatOfPoint2f( maxContour.toArray() );
        int contourSize = (int)maxContour.total();
        MatOfPoint2f approxCurve_temp = new MatOfPoint2f();
        Imgproc.approxPolyDP(approxMat, approxCurve_temp, contourSize*0.05, true);

        if (approxCurve_temp.total() <= 4 && approxCurve_temp.total() > 0) {
            approxCurve = approxCurve_temp;
        }

        return approxCurve;
    }


    public Bitmap matToBit(Mat mat){
        Bitmap resultBitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, resultBitmap);
        return resultBitmap;
    }

    public Mat detectEdges(Bitmap bitmap) {
        Mat mat = new Mat();
        Utils.bitmapToMat(bitmap, mat);
        Mat edges = new Mat(mat.size(), CvType.CV_8UC1);
        Imgproc.cvtColor(mat, edges, Imgproc.COLOR_BGR2HSV, 4);

        Imgproc.GaussianBlur(edges, edges, new Size(5, 5), 0);

        Mat cannyMat = new Mat();
        Imgproc.Canny(edges, cannyMat, 75, 200);
        Bitmap bm = matToBit(cannyMat);
        Imgproc.dilate(cannyMat, cannyMat, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 5)));
        bm = matToBit(cannyMat);
        Imgproc.dilate(cannyMat, cannyMat, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 3)));
        bm = matToBit(cannyMat);

        return cannyMat;
    }
}
