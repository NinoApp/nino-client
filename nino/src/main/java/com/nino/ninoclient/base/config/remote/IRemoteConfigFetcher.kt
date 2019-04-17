package com.nino.ninoclient.base.config.remote

import android.content.Context

interface IRemoteConfigFetcher {
  fun setup(context: Context)

  fun isLatestVersion(): Boolean
}