package com.github.kr328.kaidl.test

import com.github.kr328.kaidl.BinderInterface
import com.github.kr328.kaidl.Code

/**
 * Example interface demonstrating the use of @Code annotation to explicitly assign transaction
 * codes to functions. This is useful when you need to maintain specific codes for backwards
 * compatibility with existing Binder protocol versions.
 */
@BinderInterface
interface CodeAnnotationInterface {
  /** Returns the message with explicit transaction code 100 */
  @Code(100) fun getMessage(): String

  /** Sets the message with explicit transaction code 101 */
  @Code(101) fun setMessage(message: String)

  /** Increments and returns a counter with explicit transaction code 102 */
  @Code(102) fun incrementCounter(): Int

  /** Resets the counter with explicit transaction code 103 */
  @Code(103) fun resetCounter()
}
