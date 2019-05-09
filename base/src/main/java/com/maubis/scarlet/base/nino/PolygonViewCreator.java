package com.maubis.scarlet.base.nino;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.maubis.scarlet.base.R;
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

    private float rWidth;
    private float rHeight;
    private int ivWidth;
    private int ivHeight;

    private int[] bitmapPos;

    private float POLYGON_CIRCLE_RADIUS;
    private Context context;

    private enum MODE{
        CURVE,
        RECT
    }

    public PolygonViewCreator(Context context, PolygonView polygonView, float POLYGON_CIRCLE_RADIUS) {
        this.polygonView = polygonView;
        this.POLYGON_CIRCLE_RADIUS = POLYGON_CIRCLE_RADIUS;
        this.context = context;
    }

    public void createPolygonForMarker(Bitmap rgba, ImageView iv) {
        createPolygon(rgba, iv);
    }

    private void createPolygon(Bitmap rgba, ImageView iv) {
        bitmapPos = getBitmapPositionInsideImageView(iv);

        float x = bitmapPos[0];
        float y = bitmapPos[1];
        float w = bitmapPos[2];
        float h = bitmapPos[3];

        ivWidth = iv.getWidth();
        ivHeight = iv.getHeight();

        Log.i("POINT IV H", String.valueOf(ivHeight));
        Log.i("POINT IV W", String.valueOf(ivWidth));

        x = x + iv.getLeft() - POLYGON_CIRCLE_RADIUS;
        y = y + iv.getTop() - POLYGON_CIRCLE_RADIUS;

        List<PointF> pointFs = new ArrayList<>();
        pointFs.add(new PointF(x, y));
        pointFs.add(new PointF(x + w, y));
        pointFs.add(new PointF(x + w,y + h));
        pointFs.add(new PointF(x,y + h));

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

    private void createPolygon(MODE mode, Object dataHolder, Bitmap rgba, ImageView iv){
        bitmapPos = getBitmapPositionInsideImageView(iv);

        float x = bitmapPos[0];
        float y = bitmapPos[1];
        float w = bitmapPos[2];
        float h = bitmapPos[3];

        float origBitWidth = rgba.getWidth();
        float origBitHeight = rgba.getHeight();
        ivWidth = iv.getWidth();
        ivHeight = iv.getHeight();

        rWidth = ivWidth / origBitWidth;
        rHeight = ivHeight / origBitHeight;

        rHeight = rWidth;
        Log.i("POINT IV H", String.valueOf(ivHeight));
        Log.i("POINT IV W", String.valueOf(ivWidth));

        List<PointF> pointFs = new ArrayList<>();
        if (mode == MODE.CURVE){
            Log.i("POLYGON_POINT", "curve v2");
            MatOfPoint2f approxCurve = (MatOfPoint2f) dataHolder;
            List<Point> lp = approxCurve.toList();
            Log.i("P: POLYGON_POINT CORNER", String.valueOf(lp.size()));
            for(Point p : lp){
                Log.i("P: POLYGON_POINT", p.x + " @ " + p.y + " @ " + rWidth + " @ " +
                        rHeight + " @ " + ivWidth + " @ " + ivHeight + " @ " + origBitWidth + " @ " + origBitHeight);

                x = (float) ((p.x * rWidth) - POLYGON_CIRCLE_RADIUS);
                y = (float) ((p.y * rHeight) - POLYGON_CIRCLE_RADIUS);

                Log.i("P: POLYGON_POINT CORNER", x + "-" + y);
                pointFs.add(new PointF(x, y));
            }
        }else{
            Log.i("POLYGON_POINT", "rect v2");
            Rect rect = (Rect) dataHolder;
            x = (rect.x * rWidth) - POLYGON_CIRCLE_RADIUS;
            y = (rect.y * rHeight) - POLYGON_CIRCLE_RADIUS;
            w = (rect.width * rWidth);
            h = (rect.height * rHeight);

            if(x <= 0 || y <= 0){
                pointFs.add(new PointF(x, y));
                pointFs.add(new PointF(x + w, y));
                pointFs.add(new PointF(x + w,y + h));
                pointFs.add(new PointF(x,y + h));
            }else{
                pointFs.add(new PointF((float) (w/4.0), (float) (y/4.0)));
                pointFs.add(new PointF((float) ((x+w)/4.0), (float) (y/4.0)));
                pointFs.add(new PointF((float) ((x+w)/4.0), (float) ((y+h)/4.0)));
                pointFs.add(new PointF((float) (x/4.0), (float) ((y+h)/4.0)));
            }


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

        for (PointF p : orderedPoints.values()){
            Log.i("P: POLYGON_POINT CONF", p.toString());
        }

        polygonView.setPoints(orderedPoints);
        polygonView.setVisibility(View.VISIBLE);
    }

    public void createPolygonWithCurve(MatOfPoint2f approxCurve, Bitmap rgba, ImageView iv){
        createPolygon(MODE.CURVE, approxCurve, rgba, iv);
    }

    public void createPolygonWithRect(Rect rect, Bitmap rgba, ImageView iv){
        createPolygon(MODE.RECT, rect, rgba, iv);
    }

    private Map<Integer, PointF> configureOutOfScreenPoints(Map<Integer, PointF> orderedPoints,
                                                            float width, float height) {
        Map<Integer, PointF> configuredPoints = new HashMap<>();
        float margin = (float) 0.8;
        int count  = 0;
        for (PointF p: orderedPoints.values()) {
            PointF newP = new PointF(p.x, p.y);
            if((p.x >= width * margin) || (p.y >= height * margin)){
                if(p.x > width * margin){
                    newP.x = width * margin;
                }
                if(p.y > height * margin){
                    newP.y = height * margin;
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
            double x = (pf.x + POLYGON_CIRCLE_RADIUS) / rWidth;
            double y = (pf.y + POLYGON_CIRCLE_RADIUS) / rHeight;
            points.add(new Point(Math.floor(x), Math.floor(y)));
        }
        List<Point> sortedPoints = new ArrayList<Point>();
        sortedPoints.add(points.get(0));
        /*
        if(context.getResources().getBoolean(R.bool.is_tablet)){
            sortedPoints.add(points.get(3));
            sortedPoints.add(points.get(2));
        }else{
            sortedPoints.add(points.get(2));
            sortedPoints.add(points.get(3));
        }*/
        sortedPoints.add(points.get(2));
        sortedPoints.add(points.get(3));

        sortedPoints.add(points.get(1));

        return sortedPoints;
    }

    public List<PointF> getActualPoints(){
        Map<Integer, PointF> map = polygonView.getPoints();
        return new ArrayList<PointF>(map.values());
    }

    public int[] getMarkerPoints(ImageView iv, Bitmap bm) {
        Map<Integer, PointF> map = polygonView.getPoints();
        List<PointF> list = new ArrayList<PointF>(map.values());

        for (PointF p : list){
            Log.i("P: P GETMARK", p.toString());
        }

        float x = list.get(0).x - bitmapPos[0] - iv.getLeft() + POLYGON_CIRCLE_RADIUS;
        float y = list.get(0).y - bitmapPos[1] - iv.getTop() + POLYGON_CIRCLE_RADIUS;
        if(x < 0){x = 0;} if(y < 0){y = 0;}

        float x2 = list.get(1).x - bitmapPos[0] - iv.getLeft() + POLYGON_CIRCLE_RADIUS;
        float y2 = list.get(2).y - bitmapPos[1] - iv.getTop() + POLYGON_CIRCLE_RADIUS;
        if(x2 < 0){x2 = 0;} if(y2 < 0){y2 = 0;}
        float w = x2 - x;
        float h = y2 - y;

        double rX = bm.getWidth() / ((double) bitmapPos[2]);
        double rY = bm.getHeight() / ((double) bitmapPos[3]);

        int[] arr = {(int) (x*rX), (int) (y*rY), (int) (w*rX), (int) (h*rY)};
        return arr;
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
