// Copyright MyScript. All rights reserved.

package com.maubis.scarlet.base.iink;

import android.app.Application;

import com.myscript.iink.Engine;

public class IInkApplication extends Application
{
  private static Engine engine;

  public static synchronized Engine getEngine()
  {
    if (engine == null)
    {
      engine = Engine.create(MyCertificate.getBytes());
    }

    return engine;
  }

}
