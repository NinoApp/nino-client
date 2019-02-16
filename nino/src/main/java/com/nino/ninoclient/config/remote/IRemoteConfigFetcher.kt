package com.nino.ninoclient.config.remote

import android.content.Context

interface IRemoteConfigFetcher {
  fun setup(context: Context)

  fun isLatestVersion(): Boolean
}