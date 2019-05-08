package com.maubis.scarlet.base.nino;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import androidx.appcompat.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.maubis.scarlet.base.R;
import com.scanlibrary.PolygonView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CameraActivity extends AppCompatActivity {

    static final int REQUEST_TAKE_PHOTO = 1;
    private static final int PESDK_REQUEST = 18312;
    String mCurrentPhotoPath;

    private Mat rgba;

    private ImageView mainIv;
    private PolygonViewCreator pvc;
    private View progressView;
    private View cameraView;

    public static String token = "";

    private Bitmap edgeDetectedTakenImage;
    private Bitmap finalImage;
    private Bitmap rgbaBit;
    //double ivScale = 1.0;

    private float rotation;

    MaterialButton rotateButton = null;
    MaterialButton warpButton = null;
    MaterialButton imageMarker = null;
    MaterialButton eqMarker = null;
    MaterialButton selectButton = null;

    private enum MARKER{
        IMAGE,
        EQUATION
    }

    private enum CPB_STATE{
        CAMERA,
        WARP,
        PROCESS;
    }
    CPB_STATE cpbState = CPB_STATE.CAMERA;
    //CircularProgressButton cpb;

    final static float POLYGON_CIRCLE_RADIUS_IN_DP = 17f;
    float POLYGON_CIRCLE_RADIUS;


    MARKER marker = null;
    ArrayList<Bitmap> markedImages = null;
    ArrayList<Bitmap> markedEquations = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        getWindow().getDecorView().setBackgroundColor(getResources().getColor(R.color.material_grey_850));

        rotation = 0;

        mainIv = (ImageView) findViewById(R.id.takenPhotoImageView);

        Resources r = getResources();
        POLYGON_CIRCLE_RADIUS = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, POLYGON_CIRCLE_RADIUS_IN_DP,
                r.getDisplayMetrics());

        final Button processButton = (MaterialButton)findViewById(R.id.process_button);
        processButton.setVisibility(View.INVISIBLE);

        warpButton = (MaterialButton) findViewById(R.id.warpButton);
        warpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                for (int i = 0; i < rotation / 90; i++){
                    Core.flip(rgba.t(), rgba, 1);
                }
                Mat result = warp(rgba);
                finalImage = matToBit(result);

                mainIv.setImageBitmap(finalImage);
                findViewById(R.id.polygonView).setVisibility(View.INVISIBLE);
                warpButton.setVisibility(View.INVISIBLE);
                processButton.setVisibility(View.VISIBLE);
                imageMarker.setVisibility(View.VISIBLE);
                eqMarker.setVisibility(View.VISIBLE);
            }
        });

        rotateButton = (MaterialButton) findViewById(R.id.rotate_button);
        rotateButton.setVisibility(View.INVISIBLE);
        rotateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotation = (rotation + 90) % 360;
                mainIv.setRotation(rotation);
            }
        });

        processButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                process();
            }
        });

        imageMarker = (MaterialButton) findViewById(R.id.mark_image);
        imageMarker.setVisibility(View.GONE);
        imageMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                marker = MARKER.IMAGE;
                //findViewById(R.id.polygonView).setVisibility(View.VISIBLE);

                //pvc.createPolygonForMarker(finalImage, mainIv, ivScale);
                pvc.createPolygonForMarker(finalImage, mainIv);

                selectButton.setVisibility(View.VISIBLE);

                processButton.setVisibility(View.GONE);
                imageMarker.setVisibility(View.GONE);
                eqMarker.setVisibility(View.GONE);
            }
        });

        eqMarker = (MaterialButton) findViewById(R.id.mark_equation);
        eqMarker.setVisibility(View.GONE);
        eqMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                marker = MARKER.EQUATION;
                //findViewById(R.id.polygonView).setVisibility(View.VISIBLE);
//                pvc.createPolygonForMarker(finalImage, mainIv, ivScale);
                pvc.createPolygonForMarker(finalImage, mainIv);
                selectButton.setVisibility(View.VISIBLE);

                processButton.setVisibility(View.GONE);
                imageMarker.setVisibility(View.GONE);
                eqMarker.setVisibility(View.GONE);
            }
        });

        selectButton = (MaterialButton) findViewById(R.id.select_button);
        selectButton.setVisibility(View.GONE);
        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.polygonView).setVisibility(View.GONE);

                for (PointF p : pvc.getActualPoints()){
                    Log.i("P: POINT USER_SET", p.toString());
                }

                /*
                Mat warpedMat = new Mat();
                Utils.bitmapToMat(finalImage, warpedMat);
                Bitmap img = finalImage;
                Mat result = markerWarp(warpedMat);
                final Bitmap resultBitmap = matToBit(result);
*/
                float[] arr = pvc.getMarkerPoints(mainIv);
                final Bitmap resultBitmap = Bitmap.createBitmap(finalImage, (int)arr[0] , (int)arr[1], (int)arr[2], (int)arr[3]);
                AlertDialog.Builder builder = new AlertDialog.Builder(CameraActivity.this);
                LayoutInflater inflater = getLayoutInflater();
                View dialogLayout = inflater.inflate(R.layout.marker_dialog, null);

                ImageView markerIv = dialogLayout.findViewById(R.id.marker_iv);
                markerIv.setImageBitmap(resultBitmap);

                builder.setView(dialogLayout);
                builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(), "Adding Image", Toast.LENGTH_LONG).show();
                        if(marker == MARKER.IMAGE){
                            markedImages.add(resultBitmap);
                        }else{
                            markedEquations.add(resultBitmap);
                        }
                        selectButton.setVisibility(View.GONE);
                        processButton.setVisibility(View.VISIBLE);
                        imageMarker.setVisibility(View.VISIBLE);
                        eqMarker.setVisibility(View.VISIBLE);
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("Discard", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(), "Image discarded", Toast.LENGTH_LONG).show();
                        selectButton.setVisibility(View.GONE);
                        processButton.setVisibility(View.VISIBLE);
                        imageMarker.setVisibility(View.VISIBLE);
                        eqMarker.setVisibility(View.VISIBLE);
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        });

        progressView = findViewById(R.id.process_progress);
        cameraView = findViewById(R.id.camera_view);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }


    private void process(){
        finalImage = rotateBitmap(finalImage, rotation);
        File finalImageFile = persistImage(finalImage);

        showProgress(true);

        Log.e("TOKEN IN MAIN", token);
        String SERVER_POST_URL = "http://35.237.158.162:8000/api/notes/";
        Ion.with(CameraActivity.this)
            .load("POST", SERVER_POST_URL)
            .setHeader("Cache-Control", "No-Cache")
            //.setHeader("Authorization", "Token " + token)
            .noCache()
            .setMultipartParameter("name", "test")
            .setMultipartFile("image","multipart/form-data", saveBitmapToFile(finalImageFile))
            .asJsonObject()
            .setCallback(new FutureCallback<JsonObject>() {
                @Override
                public void onCompleted(Exception e, JsonObject result) {
                    Log.v("R Foto: ", "" + result);

                    Intent editorIntent = new Intent(getApplicationContext(), PhotoEditorActivity.class);
                    try{
                        editorIntent.putExtra("result", result.toString());
                        editorIntent.putExtra("img_width", finalImage.getWidth());
                        editorIntent.putExtra("img_height", finalImage.getHeight());

                        editorIntent.setData(bitToUri(finalImage));
                        startActivityForResult(editorIntent, PESDK_REQUEST);
                        //cpb.setProgress(100);

                    }catch (NullPointerException npe){
                        Toast.makeText(getApplicationContext(),
                                "There was a problem connecting to the server, please try again", Toast.LENGTH_SHORT).show();
                        Log.v("Response Error: ", "" + e.getMessage()); //DEBUG
                    }

                    showProgress(false);
                    if (e != null) {
                        Log.v("Query Error: ", "" + e.getMessage()); //DEBUG
                    }
                }
            });
    }

    private Uri bitToUri(Bitmap bitmap){
        File tempDir= Environment.getExternalStorageDirectory();
        tempDir=new File(tempDir.getAbsolutePath()+"/.temp/");
        tempDir.mkdir();
        File tempFile = null;
        FileOutputStream fos = null;
        try {
            tempFile = File.createTempFile("temp_bitToUri", ".jpg", tempDir);

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, bytes);
            byte[] bitmapData = bytes.toByteArray();

            fos = new FileOutputStream(tempFile);
            fos.write(bitmapData);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Uri.fromFile(tempFile);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO) {
            if (resultCode == RESULT_OK) {
                afterImageTaken();
            } else {
                Toast.makeText(this, "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == PESDK_REQUEST) {
            if (resultCode == RESULT_OK) {
                Intent resIntent = getIntent();
                resIntent.putExtra("result_uri", data.getStringExtra("result_uri"));
                resIntent.putExtra("text", data.getStringExtra("text"));
                resIntent.setData(data.getData());
                setResult(RESULT_OK, resIntent);
            }
        }
        finish();
    }

    public void afterImageTaken(){
        Bitmap takenImage = rotateBitmapOrientation(mCurrentPhotoPath);//BitmapFactory.decodeFile(mCurrentPhotoPath);
        showProgress(true);
        edgeDetectedTakenImage = detectNote(takenImage);
        showProgress(false);
        warpButton.setVisibility(View.VISIBLE);
        rotateButton.setVisibility(View.VISIBLE);
    }

    public Bitmap matToBit(Mat mat){
        Bitmap resultBitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, resultBitmap);
        return resultBitmap;
    }

    @SuppressLint("ClickableViewAccessibility")
    private Bitmap detectNote(Bitmap bitmap) {
        ImageProcessor ip = new ImageProcessor();
        rgba = new Mat();
        Utils.bitmapToMat(bitmap, rgba);

        Mat edges = ip.detectEdges(bitmap);
        MatOfPoint maxContour = ip.findContours(edges);

        Rect rect = Imgproc.boundingRect(maxContour);

        MatOfPoint2f approxCurve = ip.findApproxCurve(maxContour);

        pvc = new PolygonViewCreator((PolygonView) findViewById(R.id.polygonView), POLYGON_CIRCLE_RADIUS);

        rgbaBit = matToBit(rgba);
        mainIv.setImageBitmap(rgbaBit);

        DisplayMetrics metrics = this.getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        mainIv.getLayoutParams().width = width;//(int) (width * ivScale);

        double[] temp_double;
        Point p;
        List<Point> source = new ArrayList<Point>();
        int ivNewHeight = width;
        try {
            Log.d("APPROX_TOTAL", String.valueOf(approxCurve.total()));
            if(approxCurve.total() > 0) {
                for (int i = 0; i < approxCurve.total(); i++) {
                    temp_double = approxCurve.get(i, 0);
                    p = new Point(temp_double[0], temp_double[1]);
                    source.add(p);
                    //Imgproc.circle (rgba, p,10, new Scalar(255, 0, 0),10);
                }
                //ivNewHeight = pvc.createPolygonWithCurve(approxCurve, rgbaBit, mainIv, ivScale);
                ivNewHeight = pvc.createPolygonWithCurve(approxCurve, rgbaBit, mainIv);

            }else{
                //ivNewHeight = pvc.createPolygonWithRect(rect, rgbaBit, mainIv, ivScale);
                ivNewHeight = pvc.createPolygonWithRect(rect, rgbaBit, mainIv);

            }
        }catch(NullPointerException e){
            e.printStackTrace();
        }

        mainIv.getLayoutParams().height = (int) (ivNewHeight);
        mainIv.requestLayout();

        rgbaBit = matToBit(rgba);
        mainIv.setImageBitmap(rgbaBit);

        rgba = new Mat();
        Utils.bitmapToMat(bitmap, rgba);
        return matToBit(rgba);
    }

    public Mat warp(Mat inputMat) {
        List<Point> pp = pvc.getPoints();

        for (Point p: pp) {
            Log.i("POINT WARP", p.toString());
        }

        Mat startM = Converters.vector_Point2f_to_Mat(pp);

        //pp-> tl, bl, br, tr
        Point tl = pp.get(0);
        Point bl = pp.get(1);
        Point br = pp.get(2);
        Point tr = pp.get(3);
        double widthA = Math.sqrt(Math.pow((br.x - bl.x), 2) + Math.pow((br.y - bl.y), 2));
        double widthB = Math.sqrt(Math.pow((tr.x - tl.x), 2) + Math.pow((tr.y - tl.y), 2));
        int resultWidth = Math.max((int) widthA, (int) widthB);

        double heightA = Math.sqrt(Math.pow((tr.x - br.x), 2) + Math.pow((tr.y - br.y), 2));
        double heightB = Math.sqrt(Math.pow((tl.x - tl.x), 2) + Math.pow((tl.y - bl.y), 2));
        int resultHeight = Math.max((int) heightA, (int) heightB);

        Mat outputMat = new Mat(resultWidth, resultHeight, CvType.CV_8UC4);

        Point ocvPOut1 = new Point(0, 0);
        Point ocvPOut2 = new Point(0, resultHeight - 1);
        Point ocvPOut3 = new Point(resultWidth - 1, resultHeight - 1);
        Point ocvPOut4 = new Point(resultWidth - 1, 0);
        List<Point> dest = new ArrayList<Point>();
        dest.add(ocvPOut1);
        dest.add(ocvPOut2);
        dest.add(ocvPOut3);
        dest.add(ocvPOut4);
        Mat endM = Converters.vector_Point2f_to_Mat(dest);

        Mat perspectiveTransform = Imgproc.getPerspectiveTransform(startM, endM);


        Imgproc.warpPerspective(inputMat, outputMat, perspectiveTransform,
                new Size(resultWidth, resultHeight),
                Imgproc.INTER_CUBIC);

        return outputMat;
    }

    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

        Intent intent = getIntent();
        if (intent.hasExtra("img_path")) {
            mCurrentPhotoPath = intent.getExtras().getString("img_path");
            afterImageTaken();
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            cameraView.setVisibility(show ? View.GONE : View.VISIBLE);
            cameraView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    cameraView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            progressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    progressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            cameraView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private File persistImage(Bitmap bitmap) {
        File filesDir = getApplicationContext().getFilesDir();;
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File imageFile = new File(filesDir, imageFileName + ".jpg");

        OutputStream os;
        try {
            os = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
            os.close();
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "Error writing bitmap", e);
        }
        return imageFile;
    }

    public File saveBitmapToFile(File file){
        try {

            // BitmapFactory options to downsize the image
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            o.inSampleSize = 6;
            // factor of downsizing the image

            FileInputStream inputStream = new FileInputStream(file);
            //Bitmap selectedBitmap = null;
            BitmapFactory.decodeStream(inputStream, null, o);
            inputStream.close();

            // The new size we want to scale to
            final int REQUIRED_SIZE=75;

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while(o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                    o.outHeight / scale / 2 >= REQUIRED_SIZE) {
                scale *= 2;
            }

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            inputStream = new FileInputStream(file);

            Bitmap selectedBitmap = BitmapFactory.decodeStream(inputStream, null, o2);
            inputStream.close();

            file.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(file);

            selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 100 , outputStream);

            return file;
        } catch (Exception e) {
            return null;
        }
    }

    public Bitmap rotateBitmapOrientation(String photoFilePath) {
        // Create and configure BitmapFactory
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoFilePath, bounds);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        Bitmap bm = BitmapFactory.decodeFile(photoFilePath, opts);
        // Read EXIF Data
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(photoFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
        int orientation = orientString != null ? Integer.parseInt(orientString) : ExifInterface.ORIENTATION_NORMAL;
        int rotationAngle = 0;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;
        // Rotate Bitmap
        Matrix matrix = new Matrix();
        matrix.setRotate(rotationAngle, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bm, 0, 0, bounds.outWidth, bounds.outHeight, matrix, true);
        // Return result
        return rotatedBitmap;
    }

    public static Bitmap rotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("OpenCV", "OpenCV loaded successfully");
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
}
