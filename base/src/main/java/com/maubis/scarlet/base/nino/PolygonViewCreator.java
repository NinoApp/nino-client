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

    float rWidth;
    float rHeight;
    int[] bitmapPos;

    int ivWidth;
    int ivHeight;

    float widthMargin;
    float heightMargin;

    //final static int POLYGON_CIRCLE_RADIUS = (32 + 2) / 2; //WIDTH + STROKE
    float POLYGON_CIRCLE_RADIUS;

    private enum MODE{
        CURVE,
        RECT
    }

    public PolygonViewCreator(PolygonView polygonView, float POLYGON_CIRCLE_RADIUS) {
        this.polygonView = polygonView;
        this.POLYGON_CIRCLE_RADIUS = POLYGON_CIRCLE_RADIUS;
    }

    private int createPolygon(MODE mode, Object dataHolder, Bitmap rgba, ImageView iv, double ivScale){
        Log.i("POLYGON_POINT", "curve v2");

        float origBitWidth = rgba.getWidth();
        float origBitHeight = rgba.getHeight();
        ivWidth = iv.getLayoutParams().width;
        ivHeight = iv.getLayoutParams().height;
        rWidth = ivWidth / origBitWidth;
        rHeight = ivHeight / origBitHeight;

        rHeight = rWidth;
        ivHeight = (int) (rHeight * origBitHeight);
        Log.i("POINT IV H", String.valueOf(iv.getLayoutParams().height));
        Log.i("POINT IV W", String.valueOf(iv.getLayoutParams().width));

        // ivWidth / ivScale  = X / ((1-ivScale) / 2)
        widthMargin = (float) (ivWidth * ((1.0 - ivScale) / 2) / ivScale);
        heightMargin = (float) (ivHeight * ((1.0 - ivScale) / 2) / ivScale);

        List<PointF> pointFs = new ArrayList<>();
        if (mode == MODE.CURVE){
            Log.i("POLYGON_POINT", "curve v2");
            MatOfPoint2f approxCurve = (MatOfPoint2f) dataHolder;
            List<Point> lp = approxCurve.toList();
            Log.i("P: POLYGON_POINT CORNER", String.valueOf(lp.size()));
            for(Point p : lp){
                Log.i("P: POLYGON_POINT", p.x + " @ " + p.y + " @ " + rWidth + " @ " +
                        rHeight + " @ " + ivWidth + " @ " + ivHeight + " @ " + origBitWidth + " @ " + origBitHeight);

                float x = (float) ((p.x * rWidth) + widthMargin - POLYGON_CIRCLE_RADIUS);
                float y = (float) ((p.y * rHeight) + heightMargin - POLYGON_CIRCLE_RADIUS);

                Log.i("P: POLYGON_POINT CORNER", x + "-" + y);
                pointFs.add(new PointF(x, y));
            }
        }else{
            Log.i("POLYGON_POINT", "rect v2");
            Rect rect = (Rect) dataHolder;
            float x = (rect.x * rWidth) + widthMargin - POLYGON_CIRCLE_RADIUS;
            float y = (rect.y * rHeight) + heightMargin - POLYGON_CIRCLE_RADIUS;
            float w = (rect.width * rWidth);
            float h = (rect.height * rHeight);

            pointFs.add(new PointF(x, y));
            pointFs.add(new PointF(x + w, y));
            pointFs.add(new PointF(x,y + h));
            pointFs.add(new PointF(x + w,y + h));
        }

        Map<Integer, PointF> orderedPoints = polygonView.getOrderedPoints(pointFs);
        for (PointF p : orderedPoints.values()){
            Log.i("P: POLYGON_POINT ORD", p.toString());
        }

        if (!polygonView.isValidShape(orderedPoints)) {
            orderedPoints = getOutlinePoints((float) ivWidth, (float) ivHeight);
        }

        for (PointF p : orderedPoints.values()){
            Log.i("P: POLYGON_POINT VAL", p.toString());
        }

        orderedPoints = configureOutOfScreenPoints(orderedPoints, ivWidth, ivHeight);

        polygonView.setPoints(orderedPoints);
        polygonView.setVisibility(View.VISIBLE);

        return (int) (rHeight * origBitHeight);
    }

    //0: left, 1: top, 2: width, 3: height
    private void createPolygon(Bitmap rgba, ImageView iv, double ivScale) {
        bitmapPos = getBitmapPositionInsideImageView(iv);

        float x = bitmapPos[0];
        float y = bitmapPos[1];
        float w = bitmapPos[2];
        float h = bitmapPos[3];

        ivWidth = iv.getLayoutParams().width;
        ivHeight = iv.getLayoutParams().height;

        // ivWidth / ivScale  = X / ((1-ivScale) / 2)
        widthMargin = (float) (ivWidth * ((1.0 - ivScale) / 2) / ivScale);
        heightMargin = (float) (ivHeight * ((1.0 - ivScale) / 2) / ivScale);

        x = x + iv.getLeft() + widthMargin - POLYGON_CIRCLE_RADIUS;
        y = y + iv.getTop() + heightMargin - POLYGON_CIRCLE_RADIUS;

        List<PointF> pointFs = new ArrayList<>();
        pointFs.add(new PointF(x, y));
        pointFs.add(new PointF(x + w, y));
        pointFs.add(new PointF(x,y + h));
        pointFs.add(new PointF(x + w,y + h));

        for (PointF p : pointFs){
            Log.i("P: POLYGON_POINT Fs", p.toString());
        }

        Map<Integer, PointF> orderedPoints = polygonView.getOrderedPoints(pointFs);
        for (PointF p : orderedPoints.values()){
            Log.i("P: POLYGON_POINT ORD", p.toString());
        }

        if (!polygonView.isValidShape(orderedPoints)) {
            orderedPoints = getOutlinePoints((float) ivWidth, (float) ivHeight);
        }

        for (PointF p : orderedPoints.values()){
            Log.i("P: POLYGON_POINT VAL", p.toString());
        }

        orderedPoints = configureOutOfScreenPoints(orderedPoints, ivWidth, ivHeight);

        polygonView.setPoints(orderedPoints);
        polygonView.setVisibility(View.VISIBLE);
    }

    public int createPolygonWithCurve(MatOfPoint2f approxCurve, Bitmap rgba, ImageView iv, double ivScale){
        return createPolygon(MODE.CURVE, approxCurve, rgba, iv, ivScale);
    }

    public int createPolygonWithRect(Rect rect, Bitmap rgba, ImageView iv, double ivScale){
        return createPolygon(MODE.RECT, rect, rgba, iv, ivScale);
    }

    public void createPolygonForMarker(Bitmap rgba, ImageView iv, double ivScale){
        createPolygon(rgba, iv, ivScale);
    }

    private Map<Integer, PointF> configureOutOfScreenPoints(Map<Integer, PointF> orderedPoints,
                                                            float width, float height) {
        Map<Integer, PointF> configuredPoints = new HashMap<>();
        float margin = (float) 0.9;
        int count  = 0;
        for (PointF p: orderedPoints.values()) {
            PointF newP = new PointF(p.x, p.y);
            if((p.x >= width * margin) || (p.y >= height * margin)){
                if(p.x > width * margin){
                    newP.x = p.x * margin;
                }
                if(p.y > height * margin){
                    newP.y = p.y * margin;
                }
            }
            configuredPoints.put(count, newP);
            count++;
        }
        return configuredPoints;
    }

    private Map<Integer, PointF> getOutlinePoints(float origBitWidth, float origBitHeight) {
        Map<Integer, PointF> outlinePoints = new HashMap<>();
        outlinePoints.put(0, new PointF(0, 0));
        outlinePoints.put(1, new PointF(origBitWidth*3/4, 0));
        outlinePoints.put(2, new PointF(0, origBitHeight*3/4));
        outlinePoints.put(3, new PointF(origBitWidth*3/4, origBitHeight*3/4));
        return outlinePoints;
    }

    public List<Point> getPoints() {
        Map<Integer, PointF> map = polygonView.getPoints();
        List<PointF> list = new ArrayList<PointF>(map.values());
        List<Point> points = new ArrayList<Point>();

        for(PointF pf : list){
            double x = (pf.x - widthMargin + POLYGON_CIRCLE_RADIUS) / rWidth;
            double y = (pf.y - heightMargin + POLYGON_CIRCLE_RADIUS) / rHeight;
            points.add(new Point(Math.floor(x), Math.floor(y)));
        }
        List<Point> sortedPoints = new ArrayList<Point>();
        sortedPoints.add(points.get(0));
        sortedPoints.add(points.get(2));
        sortedPoints.add(points.get(3));
        sortedPoints.add(points.get(1));

        return sortedPoints;
    }

    public List<PointF> getActualPoints(){
        Map<Integer, PointF> map = polygonView.getPoints();
        return new ArrayList<PointF>(map.values());

    }

    public float[] getMarkerPoints(ImageView iv) {
        Map<Integer, PointF> map = polygonView.getPoints();
        List<PointF> list = new ArrayList<PointF>(map.values());
        List<Point> points = new ArrayList<Point>();

        //+ iv.getLeft() + widthMargin - POLYGON_CIRCLE_RADIUS;
        float x = list.get(0).x - iv.getLeft() - widthMargin + POLYGON_CIRCLE_RADIUS;
        float y = list.get(0).y - iv.getTop() - heightMargin + POLYGON_CIRCLE_RADIUS;
        float w = list.get(1).x - x;
        float h = list.get(1).y - y;

        float[] arr = {x, y, w, h};
        return arr;

        /*
        Map<Integer, PointF> map = polygonView.getPoints();
        List<PointF> list = new ArrayList<PointF>(map.values());
        List<Point> points = new ArrayList<Point>();

        for(PointF pf : list){
            double x = (pf.x - iv.getLeft() - widthMargin + POLYGON_CIRCLE_RADIUS) / rWidth;
            double y = (pf.y - iv.getTop() - heightMargin + POLYGON_CIRCLE_RADIUS) / rHeight;
            points.add(new Point(Math.floor(x), Math.floor(y)));
        }
        List<Point> sortedPoints = new ArrayList<Point>();
        sortedPoints.add(points.get(0));
        sortedPoints.add(points.get(2));
        sortedPoints.add(points.get(3));
        sortedPoints.add(points.get(1));

        return sortedPoints;
        */
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
}
