package com.maubis.scarlet.base.nino;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.maubis.scarlet.base.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.Console;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import ly.img.android.pesdk.assets.filter.basic.FilterPackBasic;
import ly.img.android.pesdk.assets.font.basic.FontPackBasic;
import ly.img.android.pesdk.assets.frame.basic.FramePackBasic;
import ly.img.android.pesdk.assets.overlay.basic.OverlayPackBasic;
import ly.img.android.pesdk.assets.sticker.emoticons.StickerPackEmoticons;
import ly.img.android.pesdk.assets.sticker.shapes.StickerPackShapes;
import ly.img.android.pesdk.backend.decoder.ImageSource;
import ly.img.android.pesdk.backend.model.config.ImageStickerAsset;
import ly.img.android.pesdk.backend.model.constant.Directory;
import ly.img.android.pesdk.backend.model.state.AssetConfig;
import ly.img.android.pesdk.backend.model.state.EditorLoadSettings;
import ly.img.android.pesdk.backend.model.state.EditorSaveSettings;
import ly.img.android.pesdk.backend.model.state.manager.SettingsList;
import ly.img.android.pesdk.ui.activity.ImgLyIntent;
import ly.img.android.pesdk.ui.activity.PhotoEditorBuilder;
import ly.img.android.pesdk.ui.model.state.UiConfigFilter;
import ly.img.android.pesdk.ui.model.state.UiConfigFrame;
import ly.img.android.pesdk.ui.model.state.UiConfigOverlay;
import ly.img.android.pesdk.ui.model.state.UiConfigSticker;
import ly.img.android.pesdk.ui.model.state.UiConfigText;
import ly.img.android.pesdk.ui.panels.item.ImageStickerItem;
import ly.img.android.pesdk.ui.panels.item.StickerCategoryItem;
import ly.img.android.pesdk.ui.utils.DataSourceIdItemList;
import ly.img.android.pesdk.ui.utils.PermissionRequest;
import ly.img.android.serializer._3._0._0.PESDKFileReader;
import ly.img.android.serializer._3._0._0.PESDKFileWriter;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class PhotoEditorActivity extends Activity implements PermissionRequest.Response {
    private static final int PERMISSION_REQUEST_CODE = 200;
    public static int PESDK_RESULT = 1;
    public static int GALLERY_RESULT = 2;
    private Uri uri;

    private String ninoText = "";

    private String jsonFileName = "JSON_TEMPLATE.json";
    private Bitmap origBitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Uri imageUri = getIntent().getData();
        try {
            origBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        uri = Uri.parse("android.resource://" + getPackageName() + "/drawable/bbg");
        requestPermissionAndOpenEditor(uri);
    }

    private SettingsList settingsList;
    private void openEditor(Uri inputImage) {
        settingsList = createPesdkSettingsList();
        settingsList.getSettingsModel(EditorLoadSettings.class).setImageSource(inputImage);

        addImagesToPesdk(settingsList);
        createJson();
        readJson(settingsList);

        new PhotoEditorBuilder(this)
                .setSettingsList(settingsList)
                .startActivityForResult(this, PESDK_RESULT);
    }

    private void createJson(){
        JsonHelper jh = null;
        JSONObject jo = null;
        try {
            JSONObject result =  new JSONObject(getIntent().getStringExtra("result"));
            JSONArray linesJson = result.getJSONArray("paragraphs");
            JSONArray imagesJson = result.getJSONArray("images");

            JSONObject pageJSon = result.getJSONObject("page");
            jh = new JsonHelper(pageJSon.getInt("width"), pageJSon.getInt("height"));
            jo = jh.createJsonTemplate(linesJson, imagesJson);
            Log.i("JSON_RESULT", jo.toString());
            jh.writeJson(jo, jsonFileName);
            for(int i = 0; i < linesJson.length(); i++) {
                JSONObject entry = linesJson.getJSONObject(i);
                String text = entry.getString("text");
                this.ninoText += text + "\n";
            }
            } catch (JSONException e1) {
            e1.printStackTrace();
        }
    }

    private ArrayList<ImageStickerItem> createImageArray(JSONArray imagesJson, AssetConfig assetConfig,
                                                         double widthRatio, double heightRatio){
        ArrayList<ImageStickerItem> imageStickers = new ArrayList<ImageStickerItem>();
        for (int i = 0; i < imagesJson.length(); i++){
            try {
                JSONObject entry = imagesJson.getJSONObject(i);
                double left = entry.getDouble("left") * widthRatio;
                double top = entry.getDouble("top") * heightRatio;
                double right = entry.getDouble("right") * widthRatio;
                double bottom = entry.getDouble("bottom") * heightRatio;

                double width = right - left;
                double height = bottom - top;

                String name = "image" + i;
                Bitmap newBitmap = Bitmap.createBitmap(origBitmap, (int)left , (int)top, (int)width, (int)height);
                Uri imgUri = bitToUri(newBitmap);
                ImageStickerItem isi = new ImageStickerItem(name, name, ImageSource.create(
                        imgUri
                ));
                assetConfig.addAsset(new ImageStickerAsset(name, ImageSource.create(imgUri)));
                imageStickers.add(isi);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return imageStickers;
    }

    private void addImagesToPesdk(SettingsList settingsList) {
        AssetConfig assetConfig = settingsList.getConfig();
        ArrayList<ImageStickerItem> imageStickers = null;
        try {
            JSONObject result = new JSONObject(getIntent().getStringExtra("result"));
            JSONArray imagesJson = result.getJSONArray("images");

            JSONObject pageJSon = result.getJSONObject("page");
            int widthAbbyy = pageJSon.getInt("width");
            int heightAbbyy = pageJSon.getInt("height");
            Intent intent = getIntent();
            int imgWidth = intent.getIntExtra("img_width", widthAbbyy);
            int imgHeight = intent.getIntExtra("img_height", heightAbbyy);

            double widthRatio = ((double)imgWidth/widthAbbyy);
            double heightRatio = ((double)imgHeight/heightAbbyy);

            imageStickers = createImageArray(imagesJson, assetConfig, widthRatio, heightRatio);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(imageStickers != null) {
            UiConfigSticker uiConfigSticker = settingsList.getSettingsModel(UiConfigSticker.class);
            DataSourceIdItemList stickerList = uiConfigSticker.getStickerLists();
            stickerList.add(new StickerCategoryItem(
                    "det_im",
                    "Detected Images",
                    ImageSource.create(R.drawable.imgly_sticker_shapes_badge_13),
                    imageStickers
            ));
        }
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

    private void readJson(SettingsList settingsList){
        File file = new File(Environment.getExternalStorageDirectory(), jsonFileName);
        if (file.exists()) {
            PESDKFileReader reader = new PESDKFileReader(settingsList);
            try {
                reader.readJson(file);
            } catch (IOException e) {
                Toast.makeText(this, "Error while opening json.", Toast.LENGTH_LONG).show();
                e.printStackTrace();
                return;
            }
        } else {
            Toast.makeText(this, "No save state found.", Toast.LENGTH_LONG).show();
            return;
        }
    }

    @Override
    public void permissionGranted() {}

    @Override
    public void permissionDenied() {}

    private SettingsList createPesdkSettingsList() {
        // Create a empty new SettingsList and apply the changes on this referance.
        SettingsList settingsList = new SettingsList();

        // If you include our asset Packs and you use our UI you also need to add them to the UI,
        // otherwise they are only available for the backend
        // See the specific feature sections of our guides if you want to know how to add our own Assets.

        settingsList.getSettingsModel(UiConfigFilter.class).setFilterList(
                FilterPackBasic.getFilterPack()
        );

        settingsList.getSettingsModel(UiConfigText.class).setFontList(
                FontPackBasic.getFontPack()
        );

        settingsList.getSettingsModel(UiConfigFrame.class).setFrameList(
                FramePackBasic.getFramePack()
        );

        settingsList.getSettingsModel(UiConfigOverlay.class).setOverlayList(
                OverlayPackBasic.getOverlayPack()
        );

        settingsList.getSettingsModel(UiConfigSticker.class).setStickerLists(
                StickerPackEmoticons.getStickerCategory(),
                StickerPackShapes.getStickerCategory()
        );

        // Set custom editor image export settings
        settingsList.getSettingsModel(EditorSaveSettings.class)
                .setExportDir(Directory.DCIM, "pesdk_results")
                .setExportPrefix("result_")
                .setSavePolicy(EditorSaveSettings.SavePolicy.RETURN_ALWAYS_ONLY_OUTPUT);

        return settingsList;
    }

    private void requestPermissionAndOpenEditor(Uri uri) {
        if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, WRITE_EXTERNAL_STORAGE)
                    && ActivityCompat.shouldShowRequestPermissionRationale(this, READ_EXTERNAL_STORAGE)) {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
                alertBuilder.setCancelable(true);
                alertBuilder.setTitle("Need Permissions");
                alertBuilder.setMessage("Nino requires read and write permissions");
                alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(PhotoEditorActivity.this, new String[]{WRITE_EXTERNAL_STORAGE
                                , READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                    }
                });
                AlertDialog alert = alertBuilder.create();
                alert.show();
                Log.e("", "permission denied, show dialog");
            } else {
                ActivityCompat.requestPermissions(PhotoEditorActivity.this, new String[]{WRITE_EXTERNAL_STORAGE,
                        READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            }
        } else {
            openEditor(uri);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (permissions.length > 0 && grantResults.length > 0) {

                boolean flag = true;
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        flag = false;
                    }
                }
                if (flag) {
                    openEditor(uri);
                } else {
                    finish();
                }

            } else {
                finish();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == GALLERY_RESULT) {
            // Open Editor with some uri in this case with an image selected from the system gallery.
            Uri selectedImage = data.getData();
            openEditor(selectedImage);

        } else if (resultCode == RESULT_OK && requestCode == PESDK_RESULT) {
            // Editor has saved an Image.
            Uri resultURI = data.getParcelableExtra(ImgLyIntent.RESULT_IMAGE_URI);
            Uri sourceURI = data.getParcelableExtra(ImgLyIntent.SOURCE_IMAGE_URI);

            // Scan result uri to show it up in the Gallery
            if (resultURI != null) {
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).setData(resultURI));
            }

            // Scan source uri to show it up in the Gallery
            if (sourceURI != null) {
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).setData(sourceURI));
            }

            Log.i("PESDK", "Source image is located here " + sourceURI);
            Log.i("PESDK", "Result image is located here " + resultURI);

            // TODO: Do something with the result image

            // OPTIONAL: read the latest state to save it as a serialisation
            SettingsList lastState = data.getParcelableExtra(ImgLyIntent.SETTINGS_LIST);
            try {
                new PESDKFileWriter(lastState).writeJson(new File(
                        Environment.getExternalStorageDirectory(),
                        "serialisationReadyToReadWithPESDKFileReader.json"
                ));
            } catch (IOException e) { e.printStackTrace(); }

            Log.i("PESDK EDITOR RH", String.valueOf(settingsList.getSettingsModel(EditorLoadSettings.class).getRealImageSourceHeight()));
            Log.i("PESDK EDITOR RW", String.valueOf(settingsList.getSettingsModel(EditorLoadSettings.class).getRealImageSourceWidth()));
            Log.i("PESDK EDITOR SH", String.valueOf(settingsList.getSettingsModel(EditorLoadSettings.class).getImageSourceHeight()));
            Log.i("PESDK EDITOR SW", String.valueOf(settingsList.getSettingsModel(EditorLoadSettings.class).getImageSourceWidth()));

            Intent resIntent = getIntent();
            resIntent.putExtra("result_uri", resultURI.toString());
            resIntent.putExtra("text", "text goes here"); // TODO: add final text here
            resIntent.setData(resultURI);
            setResult(RESULT_OK, resIntent);
            finish();

        } else if (resultCode == RESULT_CANCELED && requestCode == PESDK_RESULT) {
            // Editor was canceled
            Uri sourceURI = data.getParcelableExtra(ImgLyIntent.SOURCE_IMAGE_URI);
            // TODO: Do something with the source...
        }
    }
}


