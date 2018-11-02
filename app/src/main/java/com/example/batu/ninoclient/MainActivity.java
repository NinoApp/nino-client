package com.example.batu.ninoclient;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cz.msebera.android.httpclient.entity.StringEntity;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_TAKE_PHOTO = 1;
    String mCurrentPhotoPath;

    private Mat rgba;

    private ImageView mainIv;
    private PolygonViewCreator pvc;

    public static String token = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainIv = (ImageView) findViewById(R.id.takenPhotoImageView);

        final Button warpButton = (Button) findViewById(R.id.warpButton);
        warpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Point> pp = pvc.getPoints();

                Mat startM = Converters.vector_Point2f_to_Mat(pp);
                Mat result = warp(rgba,startM);
                Bitmap rgbaBit = matToBit(result);
                mainIv.setImageBitmap(rgbaBit);
            }
        });

        ((Button)findViewById(R.id.cameraButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
                warpButton.setVisibility(View.VISIBLE);
            }
        });

        ((Button) findViewById(R.id.registerButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intent);
            }
        });
        /*
        mainIv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN){
                    Toast.makeText(getApplicationContext(), "X: " + String.valueOf(event.getX() + "Y: " +
                            String.valueOf(event.getY())), Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });
        */
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
                Bitmap edgeDetectedTakenImage = detectNote(takenImage);

                if(!token.equals("")) {
                    AsyncHttpClient client = new AsyncHttpClient();
                    StringEntity jsonEntity = null;

                    //RequestParams params = new RequestParams();

                    client.addHeader("Authorization", "Token " + token);
                    //client.addHeader("Content-Type", "multipart/form-data");

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

                    client.post(getApplicationContext(), "http://35.231.79.120:8000/api/notes/", jsonEntity, "application/json",
                            new JsonHttpResponseHandler(){
                                @Override
                                public void onFailure(int i, cz.msebera.android.httpclient.Header[] headers, String s, Throwable throwable) {
                                    Log.d("BAKALIM NOLDU", String.valueOf(i));
                                }

                                @Override
                                public void onSuccess(int i, cz.msebera.android.httpclient.Header[] headers, String s) {
                                    Log.d("BAKALIM NOLDU", String.valueOf(i));
                                }
                            });

                    /*
                    client.post("http://35.231.79.120:8000/api/notes", params, new TextHttpResponseHandler() {
                        @Override
                        public void onFailure(int i, cz.msebera.android.httpclient.Header[] headers, String s, Throwable throwable) {
                            Log.d("BAKALIM NOLDU", String.valueOf(i));
                        }

                        @Override
                        public void onSuccess(int i, cz.msebera.android.httpclient.Header[] headers, String s) {
                            Log.d("BAKALIM NOLDU", String.valueOf(i));
                        }
                    });
                    */
                }
                //ImageView ivPreview = (ImageView) findViewById(R.id.takenPhotoImageView);
                //ivPreview.setImageBitmap(edgeDetectedTakenImage);
            } else {
                Toast.makeText(this, "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private Bitmap detectNote(Bitmap bitmap) {
        ImageProcessor ip = new ImageProcessor();
        Utils.bitmapToMat(bitmap, rgba);

        Mat edges = new Mat();
        MatOfPoint maxContour = null;
        for(int c = 4; c <= 4; c++) {
            int threshold = c * 3;
            edges = ip.detectEdges(bitmap, threshold);
            Bitmap ed = matToBit(edges);

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

                    /*
                    Mat startM = Converters.vector_Point2f_to_Mat(source);
                    Mat result = warp(rgba,startM);
                    mainIv.setImageBitmap(matToBit(result));
                    */
                }else{
                    pvc.createPolygonWithRect(rect, rgbaBit, mainIv);
                }
            }catch(NullPointerException e){
                e.printStackTrace();
            }

        }

        return matToBit(rgba);
    }

    public Bitmap matToBit(Mat mat){
        Bitmap resultBitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, resultBitmap);
        return resultBitmap;
    }

    public Mat warp(Mat inputMat, Mat startM) {
        int resultWidth = 1000;
        int resultHeight = 1000;

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
                    rgba = new Mat();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
}
