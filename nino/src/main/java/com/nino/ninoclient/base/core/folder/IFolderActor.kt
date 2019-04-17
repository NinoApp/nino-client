package com.nino.ninoclient.base.core.folder

interface IFolderActor {

  fun offlineSave()

  fun onlineSave()

  fun save()

  fun offlineDelete()

  fun delete()
}