package com.github.kr328.kaidl.test

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ExampleParcelable(val int: Int, val long: Long, val string: String) : Parcelable
