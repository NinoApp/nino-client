package com.maubis.scarlet.base.nino;

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
        Log.i("POLYGON_POINT", "curve");
        bitmapPos = getBitmapPositionInsideImageView(iv);

        double scaledBitWidth = bitmapPos[2];
        double scaledBitHeight = bitmapPos[3];

        double origBitWidth = rgba.getWidth();
        double origBitHeight = rgba.getHeight();

        rWidth = scaledBitWidth / origBitWidth;
        rHeight = scaledBitHeight / origBitHeight;

        List<PointF> pointFs = new ArrayList<>();
        Log.i("P: POLYGON_POINT CORNER", String.valueOf(lp.size()));
        for(Point p : lp){
            Log.i("P: POLYGON_POINT", String.valueOf(p.x) + " @ " + String.valueOf(p.y)
                    + " @ " + rWidth + " @ " + rHeight + " @ " + bitmapPos[0] + " @ " +
                    bitmapPos[1] + " @ " + scaledBitWidth + " @ " + scaledBitHeight + " @ " +
                    origBitWidth + " @ " + origBitHeight);

            float x = (float) (p.x * rWidth + bitmapPos[0]);
            float y = (float) (p.y * rHeight + bitmapPos[1]);

            x = (float) (x - 7 * (1/rWidth));
            y = (float) (y - 14 * (1/rHeight));

            Log.i("P: POLYGON_POINT CORNER", String.valueOf(x) + "-" + String.valueOf(y));
            pointFs.add(new PointF(x, y));
        }

        Map<Integer, PointF> orderedPoints = polygonView.getOrderedPoints(pointFs);
        for (PointF p : orderedPoints.values()){
            Log.i("P: POLYGON_POINT ORD", p.toString());
        }

        if (!polygonView.isValidShape(orderedPoints)) {
            orderedPoints = getOutlinePoints((float) scaledBitWidth, (float) scaledBitHeight);
        }

        for (PointF p : orderedPoints.values()){
            Log.i("P: POLYGON_POINT VAL", p.toString());
        }

        polygonView.setPoints(orderedPoints);
        polygonView.setVisibility(View.VISIBLE);
    }

    public void createPolygonWithRect(Rect rect, Bitmap rgba, ImageView iv){
        bitmapPos = getBitmapPositionInsideImageView(iv);
        Log.i("POLYGON_POINT", "rect");
        double scaledBitWidth = bitmapPos[2];
        double scaledBitHeight = bitmapPos[3];

        double origBitWidth = rgba.getWidth();
        double origBitHeight = rgba.getHeight();

        rWidth = scaledBitWidth / origBitWidth;
        rHeight = scaledBitHeight / origBitHeight;

        Log.i("P: POLYGON_POINT", String.valueOf(rect.x) + " @ " + String.valueOf(rect.y)
        + " @ " + rWidth + " @ " + rHeight + " @ " + bitmapPos[0] + " @ " + bitmapPos[1] + " @ " +
                rect.width + " @ " + rect.height + " @ " + scaledBitWidth + " @ " + scaledBitHeight + " @ " +
                origBitWidth + " @ " + origBitHeight);
        /*
        int x = (int) (rect.x * rWidth + bitmapPos[0]);
        int y = (int) (rect.y * rHeight + bitmapPos[1]);
        int w = (int) (rect.width * rWidth);
        int h = (int) (rect.height * rHeight);

        x = x - (int) (7 * (1/rWidth));
        y = y - (int) (7 * (1/rHeight));
        */

        float x = (float) (rect.x * rWidth + bitmapPos[0]);
        float y = (float) (rect.y * rHeight + bitmapPos[1]);
        float w = (float) (rect.width * rWidth);
        float h = (float) (rect.height * rHeight);

        x = (float) (x - 7 * (1/rWidth));
        y = (float) (y - 14 * (1/rHeight));

        Log.i("P: CONTOUR_POINT", String.valueOf(bitmapPos[0]) + "-" + String.valueOf(bitmapPos[1]));
        Log.i("P: POLYGON_POINT", String.valueOf(x) + "-" + String.valueOf(y));

        Log.i("P: POLYGON_POINT", String.valueOf(w));
        Log.i("P: POLYGON_POINT", String.valueOf(h));

        List<PointF> pointFs = new ArrayList<>();

        pointFs.add(new PointF(x, y));
        pointFs.add(new PointF(x + w, y));
        pointFs.add(new PointF(x,y + h));
        pointFs.add(new PointF(x + w,y + h));

        Map<Integer, PointF> orderedPoints = polygonView.getOrderedPoints(pointFs);

        if (!polygonView.isValidShape(orderedPoints)) {
            orderedPoints = getOutlinePoints((float) scaledBitWidth, (float) scaledBitHeight);
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

        for (float loat: f) {
            Log.i("P: POLYGON_POINT MATRIX", String.valueOf(loat));
        }

        Log.i("P: POLYGON_POINT W/H", f[Matrix.MSCALE_X] + " @ " + f[Matrix.MSCALE_X]);


        // Get the drawable (could also get the bitmap behind the drawable and getWidth/getHeight)
        final Drawable d = imageView.getDrawable();
        final int origW = d.getIntrinsicWidth();
        final int origH = d.getIntrinsicHeight();

        // Calculate the actual dimensions
        final int actW = Math.round(origW * scaleX);
        final int actH = Math.round(origH * scaleY);
        Log.i("P: POLYGON_POINT W/H", origW + " @ " + scaleX + " @ " + origH + " @ " + scaleY);

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

    private Map<Integer, PointF> getOutlinePoints(float origBitWidth, float origBitHeight) {
        Map<Integer, PointF> outlinePoints = new HashMap<>();
        outlinePoints.put(0, new PointF(0, 0));
        outlinePoints.put(1, new PointF(origBitWidth, 0));
        outlinePoints.put(2, new PointF(0, origBitHeight*3/4));
        outlinePoints.put(3, new PointF(origBitWidth, origBitHeight*3/4));
        return outlinePoints;
    }

    public List<Point> getPoints() {
        Map<Integer, PointF> map = polygonView.getPoints();
        List<PointF> list = new ArrayList<PointF>(map.values());
        List<Point> points = new ArrayList<Point>();

        /*
        //list -> tl, tr, bl, br
        PointF tl = list.get(0);
        double x = tl.x - (int) (14 * (1/rWidth));
        //double y = tl.y - (int) (14 * (1/rHeight));
        double y = tl.y;
        x = (x - bitmapPos[0]) / rWidth;
        y = (y - bitmapPos[1]) / rHeight;
        points.add(new Point(Math.ceil(x), Math.ceil(y)));

        PointF tr = list.get(1);
        x = tr.x + (int) (14 * (1/rWidth));
        //y = tr.y - (int) (14 * (1/rHeight));
        y = tr.y;
        x = (x - bitmapPos[0]) / rWidth;
        y = (y - bitmapPos[1]) / rHeight;
        points.add(new Point(Math.ceil(x), Math.ceil(y)));

        PointF bl = list.get(2);
        x = bl.x - (int) (14 * (1/rWidth));
        y = bl.y + (int) (14 * (1/rHeight));
        x = (x - bitmapPos[0]) / rWidth;
        y = (y - bitmapPos[1]) / rHeight;
        points.add(new Point(Math.ceil(x), Math.ceil(y)));

        PointF br = list.get(3);
        x = br.x + (int) (14 * (1/rWidth));
        y = br.y + (int) (14 * (1/rHeight));
        x = (x - bitmapPos[0]) / rWidth;
        y = (y - bitmapPos[1]) / rHeight;
        points.add(new Point(Math.ceil(x), Math.ceil(y)));

        List<Point> sortedPoints = new ArrayList<Point>();
        sortedPoints.add(points.get(0));
        sortedPoints.add(points.get(2));
        sortedPoints.add(points.get(3));
        sortedPoints.add(points.get(1));

        //sp-> tl, bl, br, tr => list -> tl, tr, bl, br

        return sortedPoints;
        */

        /*
        //x*rw+pos=f => x=(f-pos)/rw /// f=x-14/rw => x=f+14/rw
        //x=(f+14/rw - pos)/rw
        for(PointF pf : list){
            double x = pf.x + (int) (7 * (1/rWidth));
            double y = pf.y + (int) (7 * (1/rHeight));
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
        */
        //x*rw+pos=f => x=(f-pos)/rw /// f=x-14/rw => x=f+14/rw
        //x=(f+14/rw - pos)/rw
        for(PointF pf : list){
            double x = pf.x + (int) (7 * (1/rWidth));
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
