package com.nino.ninoclient.core.folder

interface IFolderActor {

  fun offlineSave()

  fun onlineSave()

  fun save()

  fun offlineDelete()

  fun delete()
}