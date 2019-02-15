package com.example.batu.ninoclient.core.folder

interface IFolderActor {

  fun offlineSave()

  fun onlineSave()

  fun save()

  fun offlineDelete()

  fun delete()
}