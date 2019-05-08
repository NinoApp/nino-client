// Copyright MyScript. All rights reserved.

package com.maubis.scarlet.base.iink;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.maubis.scarlet.base.R;
import com.myscript.iink.Configuration;
import com.myscript.iink.ContentPackage;
import com.myscript.iink.ContentPart;
import com.myscript.iink.ConversionState;
import com.myscript.iink.Editor;
import com.myscript.iink.Engine;
import com.myscript.iink.IEditorListener;
import com.myscript.iink.IImageDrawer;
import com.myscript.iink.MimeType;
import com.myscript.iink.graphics.Rectangle;
import com.myscript.iink.uireferenceimplementation.EditorView;
import com.myscript.iink.uireferenceimplementation.FontUtils;
import com.myscript.iink.uireferenceimplementation.ImageDrawer;
import com.myscript.iink.uireferenceimplementation.InputController;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public class IInkActivity extends AppCompatActivity implements View.OnClickListener
{
  private static final String TAG = "IInkActivity";

  private Engine engine;
  private ContentPackage contentPackage;
  private ContentPart contentPart;
  private EditorView editorView;
  private String iinkType;


  @Override
  public void onBackPressed() {

      Editor editor = editorView.getEditor();
      if (editor == null)
        return;

      editor.waitForIdle();
    if (iinkType.equals("Text")) {
        try {
          String result = editor.export_(editor.getRootBlock(), MimeType.TEXT);
          Intent returnIntent = new Intent();
          returnIntent.putExtra("result", result);
          setResult(RESULT_OK, returnIntent);
        } catch (IOException e) {
          e.printStackTrace();
        }
    } else {
        try {
          String outputFileName = getApplicationContext().getFilesDir().getAbsolutePath() + "/images/" + UUID.randomUUID() + ".jpeg";
          ImageDrawer imageDrawer = new ImageDrawer();
          // imageDrawer.prepareImage(100, 100);
          // imageDrawer.saveImage(outputFileName);
          Log.v("IInkActity", outputFileName);
          Rectangle bbox = editor.getRootBlock().getBox();
          if (!(bbox.height > 0 || bbox.width > 0))
          {
            Log.v("iinkactivity", "bbox " + bbox.height + " " + bbox.width);
            imageDrawer.prepareImage(100, 100);
            imageDrawer.setBackgroundColor(Color.WHITE);
            imageDrawer.saveImage(outputFileName);
          } else {
            editor.export_(editor.getRootBlock(), outputFileName, MimeType.JPEG, imageDrawer);
          }
          editor.waitForIdle();
          editor.close();

          Intent returnIntent = new Intent();
          returnIntent.putExtra("result_uri", outputFileName);
          setResult(RESULT_OK, returnIntent);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      finish();
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    ErrorActivity.installHandler(this);

    engine = IInkApplication.getEngine();
    // configure recognition
    Configuration conf = engine.getConfiguration();
    String confDir = "zip://" + getPackageCodePath() + "!/assets/conf";
    conf.setStringArray("configuration-manager.search-path", new String[]{confDir});
    String tempDir = getFilesDir().getPath() + File.separator + "tmp";
    conf.setString("content-package.temp-folder", tempDir);

    setContentView(R.layout.iink_activity_main);

    editorView = findViewById(R.id.editor_view);

    // load fonts
    AssetManager assetManager = getApplicationContext().getAssets();
    Map<String, Typeface> typefaceMap = FontUtils.loadFontsFromAssets(assetManager);
    editorView.setTypefaces(typefaceMap);

    editorView.setEngine(engine);




    final Editor editor = editorView.getEditor();


    editor.addListener(new IEditorListener()
    {
      @Override
      public void partChanging(Editor editor, ContentPart oldPart, ContentPart newPart)
      {
        // no-op
      }

      @Override
      public void partChanged(Editor editor)
      {
        invalidateOptionsMenu();
        invalidateIconButtons();
      }

      @Override
      public void contentChanged(Editor editor, String[] blockIds)
      {
        invalidateOptionsMenu();
        invalidateIconButtons();
      }

      @Override
      public void onError(Editor editor, String blockId, String message)
      {
        Log.e(TAG, "Failed to edit block \"" + blockId + "\"" + message);
      }
    });

    setInputMode(InputController.INPUT_MODE_FORCE_PEN); // If using an active pen, put INPUT_MODE_AUTO here

    String packageName = UUID.randomUUID() + ".iink";
    File file = new File(getFilesDir(), packageName);
    try
    {
      Intent intent = getIntent();
      iinkType = intent.getStringExtra("iink_type");

      contentPackage = engine.createPackage(file);
      contentPart = contentPackage.createPart(iinkType); // Choose type of content (possible values are: "Text Document", "Text", "Diagram", "Math", and "Drawing")
    }
    catch (IOException e)
    {
      Log.e(TAG, "Failed to open package \"" + packageName + "\"", e);
    }
    catch (IllegalArgumentException e)
    {
      Log.e(TAG, "Failed to open package \"" + packageName + "\"", e);
    }

    setTitle("Type: " + contentPart.getType());

    // wait for view size initialization before setting part
    editorView.post(new Runnable()
    {
      @Override
      public void run()
      {
        editorView.getRenderer().setViewOffset(0, 0);
        editorView.getRenderer().setViewScale(1);
        editorView.setVisibility(View.VISIBLE);
        editor.setPart(contentPart);
      }
    });

    findViewById(R.id.button_input_mode_forcePen).setOnClickListener(this);
    findViewById(R.id.button_input_mode_forceTouch).setOnClickListener(this);
    findViewById(R.id.button_input_mode_auto).setOnClickListener(this);
    findViewById(R.id.button_undo).setOnClickListener(this);
    findViewById(R.id.button_redo).setOnClickListener(this);
    findViewById(R.id.button_clear).setOnClickListener(this);
    findViewById(R.id.iink_convert).setOnClickListener(this);

    invalidateIconButtons();
    String typeText = iinkType.equals("Math") ? iinkType + " Equation" : iinkType;
    String helper = "";

    if (typeText.equals("Text")){
      helper = "You may scratch your text aligned to the guidelines and then click Convert!";
    } else {
      helper = "You may scratch a " + typeText.toLowerCase() + " and then click Convert!";
    }
    if (!helper.isEmpty())
      Toast.makeText(this, helper, Toast.LENGTH_LONG).show();

    Toast.makeText(this, "Press Back button to save " + typeText + ".", Toast.LENGTH_LONG).show();

  }

  @Override
  protected void onDestroy()
  {
    editorView.setOnTouchListener(null);
    editorView.close();

    if (contentPart != null)
    {
      contentPart.close();
      contentPart = null;
    }
    if (contentPackage != null)
    {
      contentPackage.close();
      contentPackage = null;
    }


    // IInkApplication has the ownership, do not close here
    engine = null;

    super.onDestroy();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
      int i = item.getItemId();
      if (i == R.id.menu_convert) {
          Editor editor = editorView.getEditor();
          ConversionState[] supportedStates = editor.getSupportedTargetConversionStates(null);
          if (supportedStates.length > 0)
              editor.convert(null, supportedStates[0]);
          return true;
      } else {
          return super.onOptionsItemSelected(item);
      }
  }

  @Override
  public void onClick(View v)
  {
    int i = v.getId();
    if (i == R.id.button_input_mode_forcePen) {
      setInputMode(InputController.INPUT_MODE_FORCE_PEN);

    } else if (i == R.id.button_input_mode_forceTouch) {
      setInputMode(InputController.INPUT_MODE_FORCE_TOUCH);

    } else if (i == R.id.button_input_mode_auto) {
      setInputMode(InputController.INPUT_MODE_AUTO);

    } else if (i == R.id.button_undo) {
      editorView.getEditor().undo();

    } else if (i == R.id.button_redo) {
      editorView.getEditor().redo();

    } else if (i == R.id.button_clear) {
      editorView.getEditor().clear();
    } else if (i == R.id.iink_convert){
      Editor editor = editorView.getEditor();
      editor.convert(editor.getRootBlock(), ConversionState.DIGITAL_EDIT);
    } else {
      Log.e(TAG, "Failed to handle click event");

    }
  }

  private void setInputMode(int inputMode)
  {
    editorView.setInputMode(inputMode);
    findViewById(R.id.button_input_mode_forcePen).setEnabled(inputMode != InputController.INPUT_MODE_FORCE_PEN);
    findViewById(R.id.button_input_mode_forceTouch).setEnabled(inputMode != InputController.INPUT_MODE_FORCE_TOUCH);
    findViewById(R.id.button_input_mode_auto).setEnabled(inputMode != InputController.INPUT_MODE_AUTO);
  }

  private void invalidateIconButtons()
  {
    Editor editor = editorView.getEditor();
    final boolean canUndo = editor.canUndo();
    final boolean canRedo = editor.canRedo();
    runOnUiThread(new Runnable()
    {
      @Override
      public void run()
      {
        ImageButton imageButtonUndo = (ImageButton) findViewById(R.id.button_undo);
        imageButtonUndo.setEnabled(canUndo);
        ImageButton imageButtonRedo = (ImageButton) findViewById(R.id.button_redo);
        imageButtonRedo.setEnabled(canRedo);
        ImageButton imageButtonClear = (ImageButton) findViewById(R.id.button_clear);
        imageButtonClear.setEnabled(contentPart != null);
      }
    });
  }
}
