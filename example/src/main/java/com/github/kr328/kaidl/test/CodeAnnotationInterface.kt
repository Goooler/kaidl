package com.github.kr328.kaidl.test

import com.github.kr328.kaidl.BinderInterface
import com.github.kr328.kaidl.Code

/**
 * Example interface demonstrating the use of @Code annotation to explicitly assign code values
 * to functions. These values are used to derive the generated Binder transaction codes and are
 * useful when you need to maintain stable mappings for backwards compatibility with existing
 * Binder protocol versions.
 */
@BinderInterface
interface CodeAnnotationInterface {
  /** Returns the message using explicit @Code value 100 */
  @Code(100) fun getMessage(): String

  /** Sets the message using explicit @Code value 101 */
  @Code(101) fun setMessage(message: String)

  /** Increments and returns a counter using explicit @Code value 102 */
  @Code(102) fun incrementCounter(): Int

  /** Resets the counter using explicit @Code value 103 */
  @Code(103) fun resetCounter()
}
