package com.nino.ninoclient.base.export.support

import android.support.v4.content.FileProvider

class GenericFileProvider : FileProvider() {
  companion object {
    var PROVIDER = "com.example.batu.ninoclient.export.support.GenericFileProvider"
  }
}
