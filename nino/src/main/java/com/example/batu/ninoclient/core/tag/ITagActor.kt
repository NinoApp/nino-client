package com.example.batu.ninoclient.core.tag

interface ITagActor {

  fun offlineSave()

  fun onlineSave()

  fun save()

  fun offlineDelete()

  fun delete()
}