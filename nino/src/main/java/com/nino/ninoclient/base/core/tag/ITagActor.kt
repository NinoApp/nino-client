package com.nino.ninoclient.base.core.tag

interface ITagActor {

  fun offlineSave()

  fun onlineSave()

  fun save()

  fun offlineDelete()

  fun delete()
}