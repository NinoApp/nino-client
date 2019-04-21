// Copyright MyScript. All rights reserved.

package com.maubis.scarlet.base.iink;

import android.app.Application;

import com.maubis.scarlet.base.iink.Certificate;
import com.myscript.iink.Engine;

public class IInkApplication extends Application
{
  private static Engine engine;

  public static synchronized Engine getEngine()
  {
    if (engine == null)
    {
      engine = Engine.create(Certificate.getBytes());
    }

    return engine;
  }

}
