package com.github.kr328.kaidl.test

import android.os.Binder
import android.os.Bundle
import androidx.test.ext.junit.runners.AndroidJUnit4
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import java.util.Date
import java.util.Objects
import java.util.UUID
import kotlin.random.Random
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class BinderTest {
  private fun <T> assertEchoEquals(value: T, func: (T) -> T) {
    val echo = func(value)
    assertThat(Objects.deepEquals(value, echo)).isTrue()
  }

  private suspend fun <T> assertEchoEqualsSuspend(value: T, func: suspend (T) -> T) {
    val echo = func(value)

    assertThat(Objects.deepEquals(value, echo)).isTrue()
  }

  @Test
  fun assertion() {
    assertThat(Objects.deepEquals(1, 2)).isFalse()
    assertThat(Objects.deepEquals(1, 1)).isTrue()
  }

  @Test
  fun parcelBasicTypes() {
    val impl = BasicTypeImpl()
    val loopback = LoopbackIBinder(impl.wrap())
    val proxy = loopback.unwrap(BasicTypeInterface::class)
    val random = Random(System.currentTimeMillis())

    assertThat(proxy is BasicTypeImpl).isFalse()

    val notifyValue = random.nextInt()
    proxy.notifyInt(notifyValue)
    assertThat(impl.lastNotifiedValue).isEqualTo(notifyValue)
    assertThat(impl.notifyCount).isEqualTo(1)

    assertEchoEquals(random.nextInt(), proxy::echoInt)
    assertEchoEquals(random.nextLong(), proxy::echoLong)
    assertEchoEquals(random.nextFloat(), proxy::echoFloat)
    assertEchoEquals(random.nextDouble(), proxy::echoDouble)
    assertEchoEquals(random.nextString(), proxy::echoString)
    assertEchoEquals(random.nextBytes(1)[0], proxy::echoByte)
    assertEchoEquals(random.nextBoolean(), proxy::echoBoolean)
    assertEchoEquals(random.nextBytes(64), proxy::echoByteArray)
    assertEchoEquals(byteArrayOf(), proxy::echoByteArray)
    assertEchoEquals(random.nextCharArray(64), proxy::echoCharArray)
    assertEchoEquals(random.nextBooleanArray(64), proxy::echoBooleanArray)
    assertEchoEquals(random.nextIntArray(64), proxy::echoIntArray)
    assertEchoEquals(random.nextLongArray(64), proxy::echoLongArray)
    assertEchoEquals(random.nextFloatArray(64), proxy::echoFloatArray)
    assertEchoEquals(random.nextDoubleArray(64), proxy::echoDoubleArray)
    assertEchoEquals(random.nextSparseBooleanArray(64), proxy::echoSparseBooleanArray)
    assertEchoEquals(Date(random.nextLong()), proxy::echoDate)

    val bundle = Bundle().apply { putLong("key", random.nextLong()) }

    assertThat(bundle.get("key")).isEqualTo(proxy.echoBundle(bundle).get("key"))

    val descriptor = random.nextString()

    val stubBinder =
      object : Binder() {
        override fun getInterfaceDescriptor(): String {
          return descriptor
        }
      }

    assertThat(proxy.echoIBinder(stubBinder).interfaceDescriptor).isEqualTo(descriptor)
  }

  @Test
  fun parcelContainers() {
    val impl = ContainerImpl().wrap()
    val loopback = LoopbackIBinder(impl)
    val proxy = loopback.unwrap(ContainerInterface::class)
    val random = Random(System.currentTimeMillis())

    assertEchoEquals(List(32) { random.nextInt() }, proxy::echoIntList)
    assertEchoEquals(emptyList<Int>(), proxy::echoIntList)
    assertEchoEquals(List(32) { random.nextDouble() }, proxy::echoDoubleList)
    assertEchoEquals(random.nextString() to random.nextInt(), proxy::echoStringIntPair)
    assertEchoEquals(arrayOf(random.nextString(), random.nextString()), proxy::echoStringArray)
    assertEchoEquals(emptyArray<String>(), proxy::echoStringArray)
    assertEchoEquals(
      List(32) { random.nextString() to random.nextLong() }.toMap(),
      proxy::echoStringLongMap,
    )
    assertEchoEquals(emptyMap<String, Long>(), proxy::echoStringLongMap)
    assertEchoEquals(List(32) { List(8) { random.nextLong() }.toSet() }, proxy::echoLongSetList)
    assertEchoEquals(emptyList<Set<Long>>(), proxy::echoLongSetList)
  }

  @Test
  fun parcelNullable() {
    val impl = NullableImpl().wrap()
    val loopback = LoopbackIBinder(impl)
    val proxy = loopback.unwrap(NullableInterface::class)
    val random = Random(System.currentTimeMillis())

    assertEchoEquals(random.nextInt(), proxy::echoInt)
    assertEchoEquals(null, proxy::echoInt)
    assertEchoEquals(random.nextFloat(), proxy::echoFloat)
    assertEchoEquals(null, proxy::echoFloat)
    assertEchoEquals(random.nextString(), proxy::echoString)
    assertEchoEquals(null, proxy::echoString)

    val m = List(10) { it.toString() to random.nextLong().takeIf { random.nextBoolean() } }.toMap()

    assertEchoEquals(m, proxy::echoMap)
    assertEchoEquals(null, proxy::echoMap)
  }

  @Test
  @OptIn(ExperimentalUuidApi::class)
  fun parcelComplexTypes() {
    val impl = ComplexTypesImpl().wrap()
    val loopback = LoopbackIBinder(impl)
    val proxy = loopback.unwrap(ComplexTypeInterface::class)
    val random = Random(System.currentTimeMillis())

    val basic = BasicTypeImpl()

    assertEchoEquals(random.nextParcelable(), proxy::echoParcelable)
    assertEchoEquals(random.nextParcelable(), proxy::echoParcelableNullable)
    assertEchoEquals(null, proxy::echoParcelableNullable)
    assertEchoEquals(List(10) { random.nextParcelable() }, proxy::echoParcelableList)

    assertEchoEquals(random.nextInt()) { proxy.echoBasicInterface(basic).echoInt(it) }
    assertEchoEquals(random.nextInt()) { proxy.echoBasicInterfaceNullable(basic)?.echoInt(it) ?: 0 }
    assertEchoEquals<Int?>(null) { proxy.echoBasicInterfaceNullable(null)?.echoInt(10) }
    assertEchoEquals(List(10) { random.nextInt() }) { values ->
      val interfaces = List(values.size) { basic }
      proxy.echoBasicInterfaceList(interfaces).mapIndexed { index, iface ->
        iface.echoInt(values[index])
      }
    }
    assertEchoEquals(List(10) { random.nextInt() }) { l ->
      l.map { proxy.echoBasicInterface(basic).echoInt(it) }
    }
    assertEchoEquals(UUID.randomUUID(), proxy::echoUUID)
    assertEchoEquals(Uuid.random(), proxy::echoKotlinUuid)
    assertEchoEquals(ExampleEnum.entries[random.nextInt(ExampleEnum.entries.size)], proxy::echoEnum)
    assertEchoEquals(random.nextSerializable(), proxy::echoSerializable)
    assertEchoEquals(random.nextSerializable(), proxy::echoSerializableNullable)
    assertEchoEquals(null, proxy::echoSerializableNullable)
  }

  @Test
  fun suspendInterface() {
    runBlocking {
      val impl = SuspendImpl().wrap()
      val loopback = LoopbackIBinder(impl)
      val proxy = loopback.unwrap(SuspendInterface::class)
      val random = Random(System.currentTimeMillis())

      assertEchoEqualsSuspend(random.nextInt(), proxy::echoInt)
      assertEchoEqualsSuspend(List(10) { random.nextInt() }, proxy::echoIntList)

      val msg = random.nextString()

      try {
        proxy.throwException(msg)
      } catch (e: Exception) {
        assertThat(e.message).isEqualTo(msg)
      }
    }
  }

  @Test
  fun codeAnnotation() {
    val impl = CodeAnnotationImpl()
    val loopback = LoopbackIBinder(impl.wrap())
    val proxy = loopback.unwrap(CodeAnnotationInterface::class)

    // Test getMessage
    assertThat(proxy.getMessage()).isEqualTo("")

    // Test setMessage
    val testMessage = "Hello, Code Annotation!"
    proxy.setMessage(testMessage)
    assertThat(proxy.getMessage()).isEqualTo(testMessage)

    // Test counter operations
    assertThat(proxy.incrementCounter()).isEqualTo(1)
    assertThat(proxy.incrementCounter()).isEqualTo(2)
    assertThat(proxy.incrementCounter()).isEqualTo(3)

    // Test resetCounter
    proxy.resetCounter()
    assertThat(proxy.getMessage()).isEqualTo(testMessage) // Message should be preserved
    assertThat(proxy.incrementCounter()).isEqualTo(1) // Counter should restart from 1
  }

  @Test
  fun codeAnnotationMultipleCalls() {
    val impl = CodeAnnotationImpl()
    val loopback = LoopbackIBinder(impl.wrap())
    val proxy = loopback.unwrap(CodeAnnotationInterface::class)

    // Multiple rapid calls to verify transaction codes work correctly
    repeat(10) {
      proxy.setMessage("Message $it")
      assertThat(proxy.getMessage()).isEqualTo("Message $it")
    }

    // Verify counter persists across multiple calls
    repeat(5) { proxy.incrementCounter() }
    assertThat(proxy.incrementCounter()).isEqualTo(6)
  }
}
