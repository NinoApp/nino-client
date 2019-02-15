package com.example.batu.ninoclient.support.option

class SimpleOptionsItem(
    val title: Int,
    val selected: Boolean = false,
    val listener: () -> Unit)