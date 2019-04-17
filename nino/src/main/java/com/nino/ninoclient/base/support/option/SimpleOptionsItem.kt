package com.nino.ninoclient.base.support.option

class SimpleOptionsItem(
    val title: Int,
    val selected: Boolean = false,
    val listener: () -> Unit)