package com.nino.ninoclient;

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

    private JSONObject getTextJson(String text, double x, double y, double maxWidth) throws JSONException {
        JSONObject jto = new JSONObject();
        jto.put("type", "text");

        JSONObject options = new JSONObject();
        options.put("text", text);
        options.put("fontSize", 0.01);//0.10000000149011612);
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

    public JSONObject createJsonTemplate(JSONArray lines) throws JSONException {
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

            ArrayList<Double> leftArr = new ArrayList<Double>();
            ArrayList<Double> topArr = new ArrayList<Double>();
            ArrayList<Double> rightArr = new ArrayList<Double>();
            ArrayList<Double> horArr = new ArrayList<Double>();
            for(int i = 0; i < lines.length(); i++) {
                JSONObject entry = lines.getJSONObject(i);
                double left = entry.getDouble("left");
                double top = entry.getDouble("top");
                double right = entry.getDouble("right");
                leftArr.add(left);
                topArr.add(top);
                rightArr.add(right);
                horArr.add(left);
                horArr.add(right);
            }

            int idxHor = horArr.indexOf(Collections.min(horArr));
            int idxLeft = leftArr.indexOf(Collections.min(leftArr));
            int idxTop = topArr.indexOf(Collections.min(topArr));
            int idxRight = rightArr.indexOf(Collections.min(rightArr));
            double minHor = horArr.get(idxHor);
            double minLeft = leftArr.get(idxLeft);
            double minTop = topArr.get(idxTop);
            double minRight = rightArr.get(idxRight);
            idxHor = horArr.indexOf(Collections.max(horArr));
            idxLeft = leftArr.indexOf(Collections.max(leftArr));
            idxTop = topArr.indexOf(Collections.max(topArr));
            idxRight = rightArr.indexOf(Collections.max(rightArr));
            double maxHor = horArr.get(idxHor);
            double maxLeft = leftArr.get(idxLeft);
            double maxTop = topArr.get(idxTop);
            double maxRight = rightArr.get(idxRight);

            double leftAvg = calculateAverage(leftArr);
            double rightAvg = calculateAverage(rightArr);
            double topAvg = calculateAverage(topArr);
            double horAvg = calculateAverage(horArr);

            double widthPerChar = (0.8 / 60);
            for(int i = 0; i < lines.length(); i++) {
                JSONObject entry = lines.getJSONObject(i);
                String text = entry.getString("text");
                int charCount = text.length();
                double x = entry.getDouble("left");
                double y = entry.getDouble("top");
                //x = (x - minLeft)/(maxLeft - minLeft);
                //x = (x - minHor)/(maxHor - minHor) + 0.3;
                x = x * (0.5 / leftAvg);
                //y = (y - minTop)/(maxTop - minTop);
                y = y * (0.5 / topAvg);

                double r = entry.getDouble("right");
                //r = (r - minRight)/(maxRight - minRight);
                //r = (r - minHor)/(maxHor - minHor) - 0.1;
                //r = r * (0.5 / rightAvg);


                //double maxWidth = r - x;
                //double maxWidth = (r - entry.getDouble("left")) * (0.5 / horAvg);
                double maxWidth = charCount * widthPerChar;
                Log.d("ATTEMPTING: TEXT", "text: " + text + " x: " + x + " y: " + y + " r: " + r + " mW: " + maxWidth);
                JSONObject jsonTextObject = getTextJson(text, x, y, maxWidth);
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

    private double calculateAverage(List<Double> arr) {
        double sum = 0;
        if(!arr.isEmpty()) {
            for (Double mark : arr) {
                sum += mark;
            }
            return sum / arr.size();
        }
        return sum;
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

    private void readNprintJson(String jsonFileName){
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
            Log.d("JSON_TEMPLATE_***", job.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
