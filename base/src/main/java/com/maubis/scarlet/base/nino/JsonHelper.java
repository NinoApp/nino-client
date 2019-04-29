package com.maubis.scarlet.base.nino;

import android.os.Environment;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JsonHelper {

    private static int multip = 1;
    private JSONObject getTextJson(String text, double x, double y, double maxWidth) throws JSONException {
        JSONObject jto = new JSONObject();
        jto.put("type", "text");

        JSONObject options = new JSONObject();
        options.put("text", text);
        //options.put("fontSize", 0.001 * multip);//0.10000000149011612);
        multip++;
        options.put("fontSize", 0.01);
        options.put("fontIdentifier", "imgly_font_open_sans_bold");
        options.put("alignment", "left");

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
        options.put("maxWidth", maxWidth);//0.35370001196861267);
        options.put("flipHorizontally", false);
        options.put("flipVertically", false);

        jto.put("options", options);
        return jto;
    }

    private JSONObject getImageJson(String identifier, double x, double y, double dimX, double dimY) throws JSONException {
        JSONObject jto = new JSONObject();
        jto.put("type", "sticker");

        JSONObject options = new JSONObject();
        options.put("identifier", identifier);

        JSONObject dimensions = new JSONObject();
        dimensions.put("x", dimX);
        dimensions.put("y", dimY);
        options.put("dimensions", dimensions);


        JSONObject position = new JSONObject();
        position.put("x", x);
        position.put("y", y);
        options.put("position", position);

        options.put("flipHorizontally", false);
        options.put("flipVertically", false);

        JSONObject tintColor = new JSONObject();
        JSONArray rgba = new JSONArray();
        for(int i = 0; i < 4; i++){
            rgba.put(0.0);
        }
        tintColor.put("rgba", rgba);
        options.put("tintColor", tintColor);

        options.put("rotation", "0");

        jto.put("options", options);
        return jto;
    }

    public JSONObject createJsonTemplate(JSONArray lines, JSONArray images, int imgWidth, int imgHeight) throws JSONException {
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
            image.put("width", imgWidth);
            image.put("height", imgHeight);
            jo.put("image", image);

            Log.i("JSON_HELPER W", String.valueOf(imgWidth));
            Log.i("JSON_HELPER H", String.valueOf(imgHeight));

            JSONArray operations = new JSONArray();
            //JSONObject transform = new JSONObject();
            //JSONObject orientation = new JSONObject();
            //JSONObject adjustments = new JSONObject();

            JSONObject operations_sprite = new JSONObject();
            operations_sprite.put("type", "sprite");

            JSONObject operations_sprite_options = new JSONObject();

            JSONArray operations_sprite_option_sprites = new JSONArray();

            //handleTexts(operations_sprite_option_sprites, lines);
            //handleImages(operations_sprite_option_sprites, images);

            ArrayList<Double> rightArr = new ArrayList<Double>();
            ArrayList<Double> topArr = new ArrayList<Double>();
            for(int i = 0; i < lines.length(); i++) {
                JSONObject entry = lines.getJSONObject(i);
                rightArr.add(entry.getDouble("right"));
                topArr.add(entry.getDouble("top"));
            }
            double maxRight = rightArr.get(rightArr.indexOf(Collections.max(rightArr)));
            double maxTop = topArr.get(topArr.indexOf(Collections.max(topArr)));

            //left:0 --> 0.1, right: maxRight --> 0.9
            double lowerBound = 0.3;
            double upperBound = 0.7;
            double shiftPerUnit = (upperBound - lowerBound) / maxRight;
            for(int i = 0; i < lines.length(); i++) {
                JSONObject entry = lines.getJSONObject(i);
                String text = entry.getString("text");
                double x = entry.getDouble("left");
                double y = entry.getDouble("bottom");

                x = lowerBound + x * shiftPerUnit;
                y = lowerBound + y * ((upperBound - lowerBound) / maxTop);

                double r = lowerBound + entry.getDouble("right") * shiftPerUnit;
                double width = r - x;

                Log.d("ATTEMPTING: TEXT", "text: " + text + " x: " + x + " y: " + y + " r: " + r + " mW: " + width);
                JSONObject jsonTextObject = getTextJson(text, x, y, width);
                operations_sprite_option_sprites.put(jsonTextObject);
            }

            for(int i = 0; i < images.length(); i++) {
                JSONObject entry = images.getJSONObject(i);
                double x = entry.getDouble("left");
                double y = entry.getDouble("bottom");

                x = lowerBound + x * shiftPerUnit;
                y = lowerBound + y * ((upperBound - lowerBound) / maxTop);

                double r = lowerBound + entry.getDouble("right") * shiftPerUnit;
                double dimX = r - x;

                double t = lowerBound + entry.getDouble("top") * ((upperBound - lowerBound) / maxTop);
                double dimY = t - y;

                String identifier = "image" + i;
                Log.d("ATTEMPTING: IMAGE", "identifier: " + identifier + " x: " + x +
                        " y: " + y + " dimX: " + dimX + " dimY: " + dimY);
                JSONObject jsonImageObject = getImageJson(identifier, x, y, dimX, dimY);
                Log.i("JSON_IMAGE_OBJECT", jsonImageObject.toString());
                operations_sprite_option_sprites.put(jsonImageObject);
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

    private void handleTexts(JSONArray operations_sprite_option_sprites, JSONArray lines) throws JSONException {
        ArrayList<Double> rightArr = new ArrayList<Double>();
        ArrayList<Double> topArr = new ArrayList<Double>();
        for(int i = 0; i < lines.length(); i++) {
            JSONObject entry = lines.getJSONObject(i);
            rightArr.add(entry.getDouble("right"));
            topArr.add(entry.getDouble("top"));
        }
        double maxRight = rightArr.get(rightArr.indexOf(Collections.max(rightArr)));
        double maxTop = topArr.get(topArr.indexOf(Collections.max(topArr)));

        //left:0 --> 0.1, right: maxRight --> 0.9
        double lowerBound = 0.3;
        double upperBound = 0.7;
        double shiftPerUnit = (upperBound - lowerBound) / maxRight;
        for(int i = 0; i < lines.length(); i++) {
            JSONObject entry = lines.getJSONObject(i);
            String text = entry.getString("text");
            double x = entry.getDouble("left");
            double y = entry.getDouble("bottom");

            x = lowerBound + x * shiftPerUnit;
            y = lowerBound + y * ((upperBound - lowerBound) / maxTop);
            //y = (y - minTop)/(maxTop - minTop);
            //y = y / maxTop; // change this to be same with x because why not;

            double r = lowerBound + entry.getDouble("right") * shiftPerUnit;
            double width = r - x;

            Log.d("ATTEMPTING: TEXT", "text: " + text + " x: " + x + " y: " + y + " r: " + r + " mW: " + width);
            JSONObject jsonTextObject = getTextJson(text, x, y, width);
            operations_sprite_option_sprites.put(jsonTextObject);
        }
    }

    private void handleImages(JSONArray operations_sprite_option_sprites, JSONArray images){

    }

    public void writeJson(JSONObject jo, String jsonFileName){
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
        //Log.d("CUSTOM_JSON", jo.toString());
    }

    public void readNprintJson(String jsonFileName){
        File file = new File(Environment.getExternalStorageDirectory(), jsonFileName);
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
            Log.i("JSON_TEMPLATE_***", job.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
