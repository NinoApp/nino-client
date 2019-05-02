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
    //int[] bitmapPos;

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

    private void createPolygon(MODE mode, Object dataHolder, Bitmap rgba, ImageView iv, double ivScale){
        Log.i("POLYGON_POINT", "curve v2");

        float origBitWidth = rgba.getWidth();
        float origBitHeight = rgba.getHeight();

        ivWidth = iv.getLayoutParams().width;
        ivHeight = iv.getLayoutParams().height;

        rWidth = ivWidth / origBitWidth;
        rHeight = ivHeight / origBitHeight;

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
    }

    public void createPolygonWithCurve(MatOfPoint2f approxCurve, Bitmap rgba, ImageView iv, double ivScale){
        createPolygon(MODE.CURVE, approxCurve, rgba, iv, ivScale);
    }

    public void createPolygonWithRect(Rect rect, Bitmap rgba, ImageView iv, double ivScale){
        createPolygon(MODE.RECT, rect, rgba, iv, ivScale);
    }

    /*public void createPolygonWithCurve(MatOfPoint2f approxCurve, Bitmap rgba, ImageView iv, double ivScale){
        List<Point> lp = approxCurve.toList();
        Log.i("POLYGON_POINT", "curve v2");

        float origBitWidth = rgba.getWidth();
        float origBitHeight = rgba.getHeight();

        ivWidth = iv.getLayoutParams().width;
        ivHeight = iv.getLayoutParams().height;

        rWidth = ivWidth / origBitWidth;
        rHeight = ivHeight / origBitHeight;

        Log.i("POINT IV H", String.valueOf(iv.getLayoutParams().height));
        Log.i("POINT IV W", String.valueOf(iv.getLayoutParams().width));

        // ivWidth / ivScale  = X / ((1-ivScale) / 2)
        widthMargin = (float) (ivWidth * ((1.0 - ivScale) / 2) / ivScale);
        heightMargin = (float) (ivHeight * ((1.0 - ivScale) / 2) / ivScale);

        List<PointF> pointFs = new ArrayList<>();
        Log.i("P: POLYGON_POINT CORNER", String.valueOf(lp.size()));
        for(Point p : lp){
            Log.i("P: POLYGON_POINT", p.x + " @ " + p.y + " @ " + rWidth + " @ " +
                    rHeight + " @ " + ivWidth + " @ " + ivHeight + " @ " + origBitWidth + " @ " + origBitHeight);

            float x = (float) ((p.x * rWidth) + widthMargin - POLYGON_CIRCLE_RADIUS);
            float y = (float) ((p.y * rHeight) + heightMargin - POLYGON_CIRCLE_RADIUS);

            Log.i("P: POLYGON_POINT CORNER", x + "-" + y);
            pointFs.add(new PointF(x, y));
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

        orderedPoints = configureOutOfScreenPoints(orderedPoints, (float) ivWidth, (float) ivHeight);

        polygonView.setPoints(orderedPoints);
        polygonView.setVisibility(View.VISIBLE);
    }*/

    /*public void createPolygonWithRect(Rect rect, Bitmap rgba, ImageView iv, double ivScale){
        Log.i("POLYGON_POINT", "rect v2");
        float origBitWidth = rgba.getWidth();
        float origBitHeight = rgba.getHeight();

        ivWidth = iv.getLayoutParams().width;
        ivHeight = iv.getLayoutParams().height;

        rWidth = ivWidth / origBitWidth;
        rHeight = ivHeight / origBitHeight;

        //rHeight = rWidth;
        //ivHeight = (int) (rHeight * origBitHeight);

        Log.i("POINT IV H", String.valueOf(iv.getLayoutParams().height));
        Log.i("POINT IV W", String.valueOf(iv.getLayoutParams().width));

        // ivWidth / ivScale  = X / ((1-ivScale) / 2)
        widthMargin = (int) (ivWidth * ((1.0 - ivScale) / 2) / ivScale);
        heightMargin = (int) (ivHeight * ((1.0 - ivScale) / 2) / ivScale);

        List<PointF> pPrint = new ArrayList<>();

        pPrint.add(new PointF(rect.x, rect.y));
        pPrint.add(new PointF(rect.x + rect.width, rect.y));
        pPrint.add(new PointF(rect.x,rect.y + rect.height));
        pPrint.add(new PointF(rect.x + rect.width,rect.y + rect.height));

        for (PointF p: pPrint) {
            Log.i("POINT BEFORE", p.toString());
        }

        Log.i("P: POLYGON_POINT", rect.x + " @ " + rect.y
                + " @ " + rWidth + " @ " + rHeight + " @ " + widthMargin + " @ " + heightMargin + " @ " +
                rect.width + " @ " + rect.height + " @ " + ivWidth + " @ " + ivHeight + " @ " +
                origBitWidth + " @ " + origBitHeight);

        float x = (rect.x * rWidth) + widthMargin - POLYGON_CIRCLE_RADIUS;
        float y = (rect.y * rHeight) + heightMargin - POLYGON_CIRCLE_RADIUS;
        float w = (rect.width * rWidth);
        float h = (rect.height * rHeight);

        List<PointF> pointFs = new ArrayList<>();

        pointFs.add(new PointF(x, y));
        pointFs.add(new PointF(x + w, y));
        pointFs.add(new PointF(x,y + h));
        pointFs.add(new PointF(x + w,y + h));

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
    }*/

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
}
