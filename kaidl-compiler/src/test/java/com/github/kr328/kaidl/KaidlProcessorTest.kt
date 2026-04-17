package com.github.kr328.kaidl

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.exists
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEqualTo
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.configureKsp
import com.tschuchort.compiletesting.kspSourcesDir
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.Test

@OptIn(ExperimentalCompilerApi::class)
class KaidlProcessorTest {
  @Test
  fun generatesDelegateAndProxyForBinderInterface() {
    val compilation =
      newCompilation(annotationSource, runtimeSource, androidStubsSource, serviceSource)

    val result = compilation.compile()

    assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

    val generated = compilation.kspSourcesDir.resolve("kotlin/com/example/service/TestService.kt")

    assertThat(generated).exists()

    val text = generated.readText()
    assertThat(text)
      .contains(
        "public open class TestServiceDelegate(",
        "public class TestServiceProxy(",
        "public val TRANSACTION_ping: Int = IBinder.FIRST_CALL_TRANSACTION + 0",
        "public val TRANSACTION_autoCode: Int = IBinder.FIRST_CALL_TRANSACTION + 1",
      )
  }

  @Test
  fun generatesAidlBridgeCodeForIInterfaceTypes() {
    val compilation =
      newCompilation(
        annotationSource,
        runtimeSource,
        androidStubsSource,
        aidlTypeSource,
        aidlServiceSource,
      )

    val result = compilation.compile()

    assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

    val generated =
      compilation.kspSourcesDir.resolve("kotlin/com/example/service/AidlBridgeService.kt")

    assertThat(generated).exists()

    val text = generated.readText()
    assertThat(text)
      .contains(
        "val service: LegacyAidl = LegacyAidl.Stub.asInterface(checkNotNull(`data`.readStrongBinder()))",
        "reply.writeStrongBinder(_result.asBinder())",
        "`data`.writeStrongBinder(service.asBinder())",
        "val _result: LegacyAidl = LegacyAidl.Stub.asInterface(checkNotNull(reply.readStrongBinder()))",
      )
  }

  @Test
  fun failsWhenBinderInterfaceIsNotAnInterface() {
    val compilation =
      newCompilation(
        annotationSource,
        runtimeSource,
        androidStubsSource,
        SourceFile.kotlin(
          "InvalidService.kt",
          """
          package com.example.service

          import com.github.kr328.kaidl.BinderInterface

          @BinderInterface
          class InvalidService
          """
            .trimIndent(),
        ),
      )

    val result = compilation.compile()

    assertThat(result.exitCode).isNotEqualTo(KotlinCompilation.ExitCode.OK)
    assertThat(result.messages).contains("@BinderInterface support only interfaces")
  }

  private fun newCompilation(vararg sources: SourceFile): KotlinCompilation {
    return KotlinCompilation().apply {
      inheritClassPath = true
      this.sources = sources.toList()
      configureKsp {
        symbolProcessorProviders.clear()
        symbolProcessorProviders.add(KaidlProcessorProvider())
      }
    }
  }

  private companion object {
    val annotationSource =
      SourceFile.kotlin(
        "BinderAnnotations.kt",
        """
        package com.github.kr328.kaidl

        @Target(AnnotationTarget.CLASS)
        annotation class BinderInterface

        @Target(AnnotationTarget.FUNCTION)
        annotation class Code(val value: Int)
        """
          .trimIndent(),
      )

    val runtimeSource =
      SourceFile.kotlin(
        "SuspendRuntime.kt",
        """
        package com.github.kr328.kaidl

        import android.os.IBinder
        import android.os.Parcel

        fun IBinder.suspendTransact(code: Int, data: Parcel, reply: Parcel) = Unit

        fun suspendTransaction(data: Parcel, reply: Parcel, block: suspend (reply: Parcel) -> Unit) = Unit
        """
          .trimIndent(),
      )

    val androidStubsSource =
      SourceFile.kotlin(
        "AndroidStubs.kt",
        """
        package android.os

        open class Bundle

        interface IInterface {
          fun asBinder(): IBinder
        }

        interface IBinder {
          companion object {
            const val FIRST_CALL_TRANSACTION: Int = 1
          }

          fun transact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean
        }

        open class Binder : IBinder {
          override fun transact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean = true

          open fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean = true

          open fun getInterfaceDescriptor(): String? = null

          fun attachInterface(owner: IInterface, descriptor: String) = Unit

          fun queryLocalInterface(descriptor: String): IInterface? = null
        }

        class Parcel {
          companion object {
            fun obtain(): Parcel = Parcel()
          }

          fun readStrongBinder(): IBinder? = null

          fun writeStrongBinder(value: IBinder?) = Unit

          fun writeInterfaceToken(descriptor: String) = Unit

          fun enforceInterface(descriptor: String) = Unit

          fun writeNoException() = Unit

          fun readException() = Unit

          fun writeInt(value: Int) = Unit

          fun readInt(): Int = 0

          fun recycle() = Unit
        }
        """
          .trimIndent(),
      )

    val serviceSource =
      SourceFile.kotlin(
        "TestService.kt",
        """
        package com.example.service

        import com.github.kr328.kaidl.BinderInterface

        @BinderInterface
        interface TestService {
          fun ping(value: Int): Int

          fun autoCode(): Int
        }
        """
          .trimIndent(),
      )

    val aidlTypeSource =
      SourceFile.kotlin(
        "LegacyAidl.kt",
        """
        package com.example.service

        import android.os.Binder
        import android.os.IBinder
        import android.os.IInterface

        interface LegacyAidl : IInterface {
          fun ping(value: Int): Int

          abstract class Stub : Binder(), LegacyAidl {
            companion object {
              @JvmStatic
              fun asInterface(binder: IBinder): LegacyAidl {
                throw UnsupportedOperationException("compile-fixture")
              }
            }
          }
        }
        """
          .trimIndent(),
      )

    val aidlServiceSource =
      SourceFile.kotlin(
        "AidlBridgeService.kt",
        """
        package com.example.service

        import com.github.kr328.kaidl.BinderInterface

        @BinderInterface
        interface AidlBridgeService {
          fun echoAidl(service: LegacyAidl): LegacyAidl
        }
        """
          .trimIndent(),
      )
  }
}
