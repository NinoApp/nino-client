package com.maubis.scarlet.base.nino;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.maubis.scarlet.base.R;
import com.scanlibrary.PolygonView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
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
import org.opencv.utils.Converters;

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
    String mCurrentPhotoPath;

    private Mat mainMat;
    private Mat rgba;

    private ImageView mainIv;
    private PolygonViewCreator pvc;
    private View progressView;
    private View cameraView;

    public static String token = "";

    private Bitmap edgeDetectedTakenImage;
    private Bitmap finalImage;

    private float rotation;
    Button rotateButton = null;
    Button warpButton = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        rotation = 0;

        mainIv = (ImageView) findViewById(R.id.takenPhotoImageView);

        final Button processButton = (Button)findViewById(R.id.process_button);
        processButton.setVisibility(View.INVISIBLE);

        warpButton = (Button) findViewById(R.id.warpButton);
        warpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Mat result = warp(rgba);
                finalImage = matToBit(result);
                mainIv.setImageBitmap(finalImage);
                findViewById(R.id.polygonView).setVisibility(View.INVISIBLE);
                warpButton.setVisibility(View.INVISIBLE);
                processButton.setVisibility(View.VISIBLE);
            }
        });

        ((Button)findViewById(R.id.cameraButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
                //startActivity(new Intent(getApplicationContext(), PhotoEditorCameraActivity.class));
                //startActivity(new Intent(getApplicationContext(), PhotoEditorActivity.class));
            }
        });

        rotateButton = (Button) findViewById(R.id.rotate_button);
        rotateButton.setVisibility(View.INVISIBLE);
        rotateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotation = (rotation + 90) % 360;
                mainIv.setRotation(rotation);
            }
        });

        Button webButton = (Button) findViewById(R.id.web_button);
        webButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(CameraActivity.this);
                alert.setTitle("TITLE");

                WebView wv = new WebView(CameraActivity.this);
                wv.getSettings().setBuiltInZoomControls(true);
                wv.getSettings().setUseWideViewPort(true);
                wv.getSettings().setLoadWithOverviewMode(true);

                final ProgressDialog progressDialog;
                progressDialog = new ProgressDialog(CameraActivity.this);
                progressDialog.setMessage("Loading...");
                progressDialog.show();

                wv.loadUrl("http://www.google.com");
                wv.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        view.loadUrl(url);
                        return true;
                    }

                    @Override
                    public void onPageFinished(WebView view, String url) {
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                    }
                });

                alert.setView(wv);
                alert.setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                alert.show();
            }
        });

        processButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            finalImage = rotateBitmap(finalImage, rotation);
            File finalImageFile = persistImage(finalImage);

            showProgress(true);

            Log.e("TOKEN IN MAIN", token);
            String SERVER_POST_URL = "http://35.237.158.162:8000/api/notes/";
            Ion.with(CameraActivity.this)
                    .load("POST", SERVER_POST_URL)
                    .setHeader("Cache-Control", "No-Cache")
                    .setHeader("Authorization", "Token " + token)//getIntent().getExtras().getString("token"))//token de acesso
                    .noCache()//desabilitando cache
                    //.setLogging("LOG",Log.VERBOSE)//para debug
                    //.setMultipartParameter("application/json",dadosFoto.toString())
                    .setMultipartParameter("name", "test")
                    .setMultipartFile("image","multipart/form-data", saveBitmapToFile(finalImageFile))//saveBitmapToFile(new File(mCurrentPhotoPath)))
                    .asJsonObject() //array recebida
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            // do stuff with the result or error
                            Log.v("R Foto: ", "" + result);

                            /*
                            final byte[] decodedBytes = Base64.decode(result.get("image").toString(), Base64.DEFAULT);
                            Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                            mainIv.setImageBitmap(decodedBitmap);
                            */

                            Intent editorIntent = new Intent(getApplicationContext(), PhotoEditorActivity.class);
                            //editorIntent.putExtra("lines", result.toString());
                            try{
                                editorIntent.putExtra("result", result.toString());
                                startActivity(editorIntent);
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
        });
        progressView = findViewById(R.id.process_progress);
        cameraView = findViewById(R.id.camera_view);
    }

    public static Bitmap rotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    private Uri photoURI;
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE_SECURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
            }
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,
                        //"com.maubis.scarlet.base.export.support.GenericFileProvider", photoFile);
                        "com.example.android.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO) {
            if (resultCode == RESULT_OK) {
                //CropImage.activity(photoURI).start(this);
                Bitmap takenImage = rotateBitmapOrientation(mCurrentPhotoPath);//BitmapFactory.decodeFile(mCurrentPhotoPath);
                edgeDetectedTakenImage = detectNote(takenImage);
                warpButton.setVisibility(View.VISIBLE);
                rotateButton.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(this, "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
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

        pvc = new PolygonViewCreator((PolygonView) findViewById(R.id.polygonView));

        Bitmap rgbaBit = matToBit(rgba);
        mainIv.setImageBitmap(rgbaBit);

        double[] temp_double;
        Point p;
        List<Point> source = new ArrayList<Point>();
        try {
            Log.d("APPROX_TOTAL", String.valueOf(approxCurve.total()));
            if(approxCurve.total() > 0) {
                for (int i = 0; i < approxCurve.total(); i++) {
                    temp_double = approxCurve.get(i, 0);
                    p = new Point(temp_double[0], temp_double[1]);
                    source.add(p);
                }
                pvc.createPolygonWithCurve(approxCurve, rgbaBit, mainIv);
            }else{
                pvc.createPolygonWithRect(rect, rgbaBit, mainIv);
            }
        }catch(NullPointerException e){
            e.printStackTrace();
        }

        rgba = new Mat();
        Utils.bitmapToMat(bitmap, rgba);
        return matToBit(rgba);
    }

    public Bitmap matToBit(Mat mat){
        Bitmap resultBitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, resultBitmap);
        return resultBitmap;
    }

    public Mat warp(Mat inputMat) {
        List<Point> pp = pvc.getPoints();

        for (Point p: pp) {
            Log.i("warp pp", p.toString());
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
        /*
        Imgproc.warpPerspective(inputMat, outputMat, perspectiveTransform,
                new Size(resultWidth, resultHeight),
                Imgproc.INTER_LINEAR | Imgproc.WARP_INVERSE_MAP);
        */

        return outputMat;
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

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
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

    private void changeViewVisibility(final boolean show){
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
        cameraView.setVisibility(show ? View.GONE : View.VISIBLE);
        cameraView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                cameraView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });
    }

    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("OpenCV", "OpenCV loaded successfully");
                    //mainMat = new Mat();
                    //Log.d("HMMMMMMM", "I AM HERE");
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
}