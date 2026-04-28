package com.github.kr328.kaidl.test

import com.github.kr328.kaidl.BinderInterface

@BinderInterface
interface SerializableInterface {
  fun echoSerializable(data: SerializableData): SerializableData
}
