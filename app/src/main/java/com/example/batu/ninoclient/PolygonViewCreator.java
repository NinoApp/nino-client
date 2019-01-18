package com.example.batu.ninoclient;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.scanlibrary.PolygonView;

import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PolygonViewCreator {

    private PolygonView polygonView;
    double rWidth;
    double rHeight;
    int[] bitmapPos;

    public PolygonViewCreator(PolygonView polygonView) {
        this.polygonView = polygonView;
    }

    public void createPolygonWithCurve(MatOfPoint2f approxCurve, Bitmap rgba, ImageView iv){
        List<Point> lp = approxCurve.toList();

        bitmapPos = getBitmapPositionInsideImageView(iv);

        double scaledBitWidth = bitmapPos[2];
        double scaledBitHeight = bitmapPos[3];

        double origBitWidth = rgba.getWidth();
        double origBitHeight = rgba.getHeight();

        rWidth = scaledBitWidth / origBitWidth;
        rHeight = scaledBitHeight / origBitHeight;

        List<PointF> pointFs = new ArrayList<>();
        for(Point p : lp){
            int x = (int) (p.x * rWidth + bitmapPos[0]);
            int y = (int) (p.y * rHeight + bitmapPos[1]);
            x = x - (int) (14 * (1/rWidth));
            y = y - (int) (14 * (1/rHeight));
            Log.d("P: CORNER_POINT", String.valueOf(x) + "-" + String.valueOf(y));
            pointFs.add(new PointF(x, y));
        }

        /*
        for(int i = 0; i < 4; i++)
            Log.d("P: POLYGON_POINT", pointFs.get(i).toString());*/

        Map<Integer, PointF> orderedPoints = polygonView.getOrderedPoints(pointFs);

        if (!polygonView.isValidShape(orderedPoints)) {
            orderedPoints = getOutlinePoints(rgba);
        }

        polygonView.setPoints(orderedPoints);
        polygonView.setVisibility(View.VISIBLE);
    }

    public void createPolygonWithRect(Rect rect, Bitmap rgba, ImageView iv){
        bitmapPos = getBitmapPositionInsideImageView(iv);

        double scaledBitWidth = bitmapPos[2];
        double scaledBitHeight = bitmapPos[3];

        double origBitWidth = rgba.getWidth();
        double origBitHeight = rgba.getHeight();

        rWidth = scaledBitWidth / origBitWidth;
        rHeight = scaledBitHeight / origBitHeight;

        int x = (int) (rect.x * rWidth + bitmapPos[0]);
        int y = (int) (rect.y * rHeight + bitmapPos[1]);
        int w = (int) (rect.width * rWidth);
        int h = (int) (rect.height * rHeight);

        x = x - (int) (14 * (1/rWidth));
        y = y - (int) (14 * (1/rHeight));

        Log.d("P: CONTOUR_POINT", String.valueOf(bitmapPos[0]) + "-" + String.valueOf(bitmapPos[1]));
        Log.d("P: POLYGON_POINT", String.valueOf(x) + "-" + String.valueOf(y));

        Log.d("P: POLYGON_POINT", String.valueOf(w));
        Log.d("P: POLYGON_POINT", String.valueOf(h));

        List<PointF> pointFs = new ArrayList<>();

        pointFs.add(new PointF(x, y));
        pointFs.add(new PointF(x + w, y));
        pointFs.add(new PointF(x,y + h));
        pointFs.add(new PointF(x + w,y + h));

        for(int i = 0; i < 4; i++)
            Log.d("P: POLYGON_POINT", pointFs.get(i).toString());

        Map<Integer, PointF> orderedPoints = polygonView.getOrderedPoints(pointFs);

        if (!polygonView.isValidShape(orderedPoints)) {
            orderedPoints = getOutlinePoints(rgba);
        }

        polygonView.setPoints(orderedPoints);
        polygonView.setVisibility(View.VISIBLE);
    }

    /**
     * Returns the bitmap position inside an imageView.
     * @param imageView source ImageView
     * @return 0: left, 1: top, 2: width, 3: height
     */
    public static int[] getBitmapPositionInsideImageView(ImageView imageView) {
        int[] ret = new int[4];

        if (imageView == null || imageView.getDrawable() == null)
            return ret;

        // Get image dimensions
        // Get image matrix values and place them in an array
        float[] f = new float[9];
        imageView.getImageMatrix().getValues(f);

        // Extract the scale values using the constants (if aspect ratio maintained, scaleX == scaleY)
        final float scaleX = f[Matrix.MSCALE_X];
        final float scaleY = f[Matrix.MSCALE_Y];

        // Get the drawable (could also get the bitmap behind the drawable and getWidth/getHeight)
        final Drawable d = imageView.getDrawable();
        final int origW = d.getIntrinsicWidth();
        final int origH = d.getIntrinsicHeight();

        // Calculate the actual dimensions
        final int actW = Math.round(origW * scaleX);
        final int actH = Math.round(origH * scaleY);

        ret[2] = actW;
        ret[3] = actH;

        // Get image position
        // We assume that the image is centered into ImageView
        int imgViewW = imageView.getWidth();
        int imgViewH = imageView.getHeight();

        int top = (int) (imgViewH - actH)/2;
        int left = (int) (imgViewW - actW)/2;

        ret[0] = left;
        ret[1] = top;

        return ret;
    }

    private Map<Integer, PointF> getOutlinePoints(Bitmap tempBitmap) {
        Map<Integer, PointF> outlinePoints = new HashMap<>();
        outlinePoints.put(0, new PointF(0, 0));
        outlinePoints.put(1, new PointF(tempBitmap.getWidth(), 0));
        outlinePoints.put(2, new PointF(0, tempBitmap.getHeight()));
        outlinePoints.put(3, new PointF(tempBitmap.getWidth(), tempBitmap.getHeight()));
        return outlinePoints;
    }

    public List<Point> getPoints() {
        Map<Integer, PointF> map = polygonView.getPoints();
        List<PointF> list = new ArrayList<PointF>(map.values());
        List<Point> points = new ArrayList<Point>();

        //x*rw+pos=f => x=(f-pos)/rw /// f=x-14/rw => x=f+14/rw
        //x=(f+14/rw - pos)/rw
        for(PointF pf : list){
            double x = pf.x + (int) (14 * (1/rWidth));
            double y = pf.y + (int) (14 * (1/rHeight));
            x = (x - bitmapPos[0]) / rWidth;
            y = (y - bitmapPos[1]) / rHeight;

            points.add(new Point(Math.ceil(x), Math.ceil(y)));
        }
        List<Point> sortedPoints = new ArrayList<Point>();
        sortedPoints.add(points.get(0));
        sortedPoints.add(points.get(2));
        sortedPoints.add(points.get(3));
        sortedPoints.add(points.get(1));

        return sortedPoints;
    }

    public int getBitmapWidth(){
        return bitmapPos[2];
    }

    public int getBitmapHeight(){
        return bitmapPos[3];
    }
}
