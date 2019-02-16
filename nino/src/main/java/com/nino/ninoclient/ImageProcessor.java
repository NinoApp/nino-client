package com.nino.ninoclient;

import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class ImageProcessor {
    public Mat detectEdges(Bitmap bitmap, int threshold) {
        Mat mat = new Mat();
        Utils.bitmapToMat(bitmap, mat);
        Mat edges = new Mat(mat.size(), CvType.CV_8UC1);
        Imgproc.cvtColor(mat, edges, Imgproc.COLOR_RGB2GRAY, 4);

        Imgproc.GaussianBlur(edges, edges, new Size(15, 15), 16);

        Imgproc.Canny(edges, edges, threshold, threshold*2);

        //Imgproc.dilate(edges, edges, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 12)));
        //Imgproc.dilate(edges, edges, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(12, 5)));
        Imgproc.dilate(edges, edges, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(20, 20)));


        return edges;
    }

    public MatOfPoint findContours(Mat edges){
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        double maxArea = 0;
        int maxAreaIndex = 0;
        for(int i = 0; i < contours.size(); i++){
            MatOfPoint matOfPoint = contours.get(i);
            double contourArea = Imgproc.contourArea(matOfPoint);
            //Rect rect = Imgproc.boundingRect(matOfPoint);
            //Imgproc.rectangle(orig, rect.tl(), rect.br(), new Scalar(255, 255, 255), 10);
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

        //approxCurve = approxCurve_temp;

        return approxCurve;
    }
}
