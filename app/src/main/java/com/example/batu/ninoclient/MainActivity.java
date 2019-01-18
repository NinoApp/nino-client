package com.example.batu.ninoclient;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;
import com.scanlibrary.PolygonView;

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
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class MainActivity extends AppCompatActivity {

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

    Button warpButton = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainIv = (ImageView) findViewById(R.id.takenPhotoImageView);

        final Button processButton = (Button)findViewById(R.id.process_button);
        processButton.setVisibility(View.INVISIBLE);

        warpButton = (Button) findViewById(R.id.warpButton);
        warpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Point> pp = pvc.getPoints();

                Mat startM = Converters.vector_Point2f_to_Mat(pp);
                Mat result = warp(rgba, startM);
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
            }
        });


        processButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            File finalImageFile = persistImage(finalImage);

            showProgress(true);

            Log.e("TOKEN IN MAIN", token);
            String SERVER_POST_URL = "http://35.227.40.210:8000/api/notes/";
            Ion.with(MainActivity.this)
                    .load("POST", SERVER_POST_URL) //url de query
                    .setHeader("Cache-Control", "No-Cache")//desabilitando cache denovo porque essa parada é bug
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
                            final byte[] decodedBytes = Base64.decode(result.get("image").toString(), Base64.DEFAULT);
                            Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                            mainIv.setImageBitmap(decodedBitmap);
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

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE_SECURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
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
                Bitmap takenImage = rotateBitmapOrientation(mCurrentPhotoPath);//BitmapFactory.decodeFile(mCurrentPhotoPath);
                edgeDetectedTakenImage = detectNote(takenImage);
                warpButton.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(this, "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
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

    public class NoteSendRequest extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String SERVER_POST_URL = "http://35.231.79.120:8000/api/notes/";

            SyncHttpClient client = new SyncHttpClient();
            StringEntity jsonEntity = null;

            //RequestParams params = new RequestParams();

            Log.e("TOKEN IN MAIN", token);
            client.addHeader("Authorization", "Token " + token);
            //client.addHeader("authorization", token);

            JSONObject jsonParams = new JSONObject();
            try {
                jsonParams.put("name", "test");
                jsonParams.put("image", new File(mCurrentPhotoPath));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                jsonEntity = new StringEntity(jsonParams.toString());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            client.post(getApplicationContext(), SERVER_POST_URL, jsonEntity, "application/json",
                    new JsonHttpResponseHandler(){
                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                            super.onFailure(statusCode, headers, responseString, throwable);
                            Log.d("NoteSendFailed: ", ""+statusCode);
                            Log.d("NoteSendError : ", "" + throwable);
                        }

                        @Override
                        public void onSuccess(int i, Header[] headers, String s) {
                            Log.d("BAKALIM NOLDU S", String.valueOf(i));
                        }
                    });

            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.e("POST_EXECUTE", result);
        }

        @Override
        protected void onCancelled() {

        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private Bitmap detectNote(Bitmap bitmap) {
        ImageProcessor ip = new ImageProcessor();
        rgba = new Mat();
        Utils.bitmapToMat(bitmap, rgba);

        Mat edges = new Mat();
        MatOfPoint maxContour = null;
        //for(int c = 4; c <= 4; c++) {
            int threshold = 15;//c * 3;
            edges = ip.detectEdges(bitmap, threshold);
            //Bitmap ed = matToBit(edges);

            maxContour = ip.findContours(edges);

            Rect rect = Imgproc.boundingRect(maxContour);

            MatOfPoint2f approxCurve = ip.findApproxCurve(maxContour);

            Imgproc.cvtColor(edges, edges, Imgproc.COLOR_BayerBG2RGB); //????

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
                        Imgproc.circle(rgba, p, 55, new Scalar(255, 0, 0), 10);
                        source.add(p);
                    }

                    pvc.createPolygonWithCurve(approxCurve, rgbaBit, mainIv);

                    Imgproc.rectangle(rgba, rect.tl(), rect.br(), new Scalar(255, 0, 0), 10);
                    Log.d("CORNER_LOOP", String.valueOf(threshold));
                }else{
                    pvc.createPolygonWithRect(rect, rgbaBit, mainIv);
                }
            }catch(NullPointerException e){
                e.printStackTrace();
            }

        //}

        rgba = new Mat();
        Utils.bitmapToMat(bitmap, rgba);
        return matToBit(rgba);
    }

    public Bitmap matToBit(Mat mat){
        Bitmap resultBitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, resultBitmap);
        return resultBitmap;
    }

    public Mat warp(Mat inputMat, Mat startM) {
        int resultWidth = pvc.getBitmapWidth();
        int resultHeight = pvc.getBitmapHeight();

        Mat outputMat = new Mat(resultWidth, resultHeight, CvType.CV_8UC4);

        Point ocvPOut1 = new Point(0, 0);
        Point ocvPOut2 = new Point(0, resultHeight);
        Point ocvPOut3 = new Point(resultWidth, resultHeight);
        Point ocvPOut4 = new Point(resultWidth, 0);
        List<Point> dest = new ArrayList<Point>();
        dest.add(ocvPOut1);
        dest.add(ocvPOut2);
        dest.add(ocvPOut3);
        dest.add(ocvPOut4);
        Mat endM = Converters.vector_Point2f_to_Mat(dest);

        Mat perspectiveTransform = Imgproc.getPerspectiveTransform(startM, endM);

        Imgproc.warpPerspective(inputMat,
                outputMat,
                perspectiveTransform,
                new Size(resultWidth, resultHeight),
                Imgproc.INTER_CUBIC);

        return outputMat;
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
