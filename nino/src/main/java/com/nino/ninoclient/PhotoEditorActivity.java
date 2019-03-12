package com.nino.ninoclient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import ly.img.android.pesdk.assets.filter.basic.FilterPackBasic;
import ly.img.android.pesdk.assets.font.basic.FontPackBasic;
import ly.img.android.pesdk.assets.frame.basic.FramePackBasic;
import ly.img.android.pesdk.assets.overlay.basic.OverlayPackBasic;
import ly.img.android.pesdk.assets.sticker.emoticons.StickerPackEmoticons;
import ly.img.android.pesdk.assets.sticker.shapes.StickerPackShapes;
import ly.img.android.pesdk.backend.model.constant.Directory;
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
import ly.img.android.pesdk.ui.utils.PermissionRequest;
import ly.img.android.serializer._3._0._0.PESDKFileReader;
import ly.img.android.serializer._3._0._0.PESDKFileWriter;

public class PhotoEditorActivity extends Activity implements PermissionRequest.Response {

    // Important permission request for Android 6.0 and above, don't forget to add this!
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionRequest.onRequestPermissionsResult(requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void permissionGranted() {}

    @Override
    public void permissionDenied() {
        /* TODO: The Permission was rejected by the user. The Editor was not opened,
         * Show a hint to the user and try again. */
    }

    public static int PESDK_RESULT = 1;
    public static int GALLERY_RESULT = 2;

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
                .setExportDir(Directory.DCIM, "SomeFolderName")
                .setExportPrefix("result_")
                .setSavePolicy(EditorSaveSettings.SavePolicy.RETURN_ALWAYS_ONLY_OUTPUT);

        return settingsList;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        openSystemGalleryToSelectAnImage();
    }

    private void openSystemGalleryToSelectAnImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, GALLERY_RESULT);
        } else {
            Toast.makeText(
                    this,
                    "No Gallery APP installed",
                    Toast.LENGTH_LONG
            ).show();
        }
    }


    private JSONObject getTextJson(String text, double x, double y, double maxWidth) throws JSONException {
        JSONObject jto = new JSONObject();
        jto.put("type", "text");

        JSONObject options = new JSONObject();
        options.put("text", text);
        options.put("fontSize", 0.10000000149011612);
        options.put("fontIdentifier", "imgly_font_open_sans_bold");
        options.put("alignment", "center");

        JSONObject color = new JSONObject();
        JSONArray rgba = new JSONArray();
        for(int i = 0; i < 4; i++){
            rgba.put(1);
        }
        color.put("rgba", rgba);
        options.put("color", color);

        JSONObject backgroundColor = new JSONObject();
        JSONArray rgbaBC = new JSONArray();
        for(int i = 0; i < 4; i++){
            rgbaBC.put(0);
        }
        backgroundColor.put("rgba", rgbaBC);
        options.put("backgroundColor", backgroundColor);

        JSONObject position = new JSONObject();
        position.put("x", x);
        position.put("y", y);
        options.put("position", position);

        options.put("rotation", "0");
        options.put("maxWidth", 0.35370001196861267);//maxWidth);
        options.put("flipHorizontally", false);
        options.put("flipVertically", false);

        jto.put("options", options);
        return jto;
    }

    private JSONObject createJsonTemplate(){
        JSONObject jo = new JSONObject();
        try {
            jo.put("version", "3.0.0");

            JSONObject meta = new JSONObject();
            meta.put("platform", "android");
            meta.put("verison", "6.2.6");
            meta.put("createdAt", "2019-03-13T00:01:02+03:00");
            jo.put("meta", meta);

            JSONObject image = new JSONObject();
            image.put("type", "image/jpeg");
            image.put("width", 3096);
            image.put("height", 5504);
            jo.put("image", image);

            JSONArray operations = new JSONArray();
            //JSONObject transform = new JSONObject();
            //JSONObject orientation = new JSONObject();
            //JSONObject adjustments = new JSONObject();

            JSONObject operations_sprite = new JSONObject();
            operations_sprite.put("type", "sprite");

            JSONObject operations_sprite_options = new JSONObject();

            JSONArray operations_sprite_option_sprites = new JSONArray();

            for(int i = 1; i <= 3; i++) {
                JSONObject jsonTextObject = getTextJson("hi", 0.2 * i, 0.2*i, 0);
                operations_sprite_option_sprites.put(jsonTextObject);
            }

            operations_sprite_options.put("sprites", operations_sprite_option_sprites);
            operations_sprite.put("options", operations_sprite_options);
            operations.put(operations_sprite);

            jo.put("operations", operations);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jo;
    }

    private void writeJson(JSONObject jo, String jsonFileName){
        File root = android.os.Environment.getExternalStorageDirectory();
        File dir = new File (root.getAbsolutePath());
        File file = new File(Environment.getExternalStorageDirectory(), jsonFileName);

        try {
            FileOutputStream f = new FileOutputStream(file);
            PrintWriter pw = new PrintWriter(f);

            pw.println(jo.toString());
            pw.flush();
            pw.close();
            f.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openEditor(Uri inputImage) {
        SettingsList settingsList = createPesdkSettingsList();

        // Set input image
        settingsList.getSettingsModel(EditorLoadSettings.class)
                .setImageSource(inputImage);

        JSONObject jo = createJsonTemplate();
        String jsonFileName = "JSON_TEMPLATE.json";
        writeJson(jo, jsonFileName);

        File file = new File(Environment.getExternalStorageDirectory(), jsonFileName);


        Log.d("CUSTOM_JSON", jo.toString());


        StringBuilder text = new StringBuilder();
        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
        } catch (IOException e) {
            // do exception handling
        } finally {
            try { br.close(); } catch (Exception e) { }
        }
        try {
            JSONObject job = new JSONObject(String.valueOf(text));
            Log.d("JSON_TEMPLATE_***", job.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }


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

        new PhotoEditorBuilder(this)
                .setSettingsList(settingsList)
                .startActivityForResult(this, PESDK_RESULT);
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

        } else if (resultCode == RESULT_CANCELED && requestCode == PESDK_RESULT) {
            // Editor was canceled
            Uri sourceURI = data.getParcelableExtra(ImgLyIntent.SOURCE_IMAGE_URI);
            // TODO: Do something with the source...
        }
    }
}


