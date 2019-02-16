package com.nino.ninoclient.core.tag

interface ITagActor {

  fun offlineSave()

  fun onlineSave()

  fun save()

  fun offlineDelete()

  fun delete()
}