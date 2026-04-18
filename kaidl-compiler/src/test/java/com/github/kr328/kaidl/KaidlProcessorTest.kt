package com.github.kr328.kaidl

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.doesNotContain
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
  fun generatesParcelableReaderUsingParcelableCreator() {
    val compilation =
      newCompilation(
        annotationSource,
        runtimeSource,
        androidStubsSource,
        parcelizeStubsSource,
        parcelableTypeSource,
        parcelableServiceSource,
      )

    val result = compilation.compile()

    assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

    val generated =
      compilation.kspSourcesDir.resolve("kotlin/com/example/service/ParcelableBridgeService.kt")

    assertThat(generated).exists()

    val text = generated.readText()
    assertThat(text)
      .contains(
        "import kotlinx.parcelize.parcelableCreator",
        "checkNotNull(parcelableCreator<ConfigurationOverride>().createFromParcel(`data`))",
      )
    assertThat(text).doesNotContain("ConfigurationOverride.CREATOR.createFromParcel")
  }

  @Test
  fun generatesDateSerializationUsingEpochMillis() {
    val compilation =
      newCompilation(annotationSource, runtimeSource, androidStubsSource, dateServiceSource)

    val result = compilation.compile()

    assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

    val generated =
      compilation.kspSourcesDir.resolve("kotlin/com/example/service/DateBridgeService.kt")

    assertThat(generated).exists()

    val text = generated.readText()
    assertThat(text)
      .contains(
        "val `value`: Date = Date(`data`.readLong())",
        "`data`.writeLong(`value`.time)",
        "val _result: Date = Date(reply.readLong())",
        "reply.writeLong(_result.time)",
      )
  }

  @Test
  fun generatesUuidSerializationUsingStringEncoding() {
    val compilation =
      newCompilation(annotationSource, runtimeSource, androidStubsSource, uuidServiceSource)

    val result = compilation.compile()

    assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

    val generated =
      compilation.kspSourcesDir.resolve("kotlin/com/example/service/UuidBridgeService.kt")

    assertThat(generated).exists()

    val text = generated.readText()
    assertThat(text)
      .contains(
        "val `javaUuid`: UUID = UUID.fromString(checkNotNull(`data`.readString()))",
        "`data`.writeString(`javaUuid`.toString())",
        "val `kotlinUuid`: Uuid = Uuid.parse(checkNotNull(`data`.readString()))",
        "`data`.writeString(`kotlinUuid`.toString())",
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

  @Test
  fun usesExplicitCodeForSingleFunction() {
    val compilation =
      newCompilation(
        annotationSource,
        runtimeSource,
        androidStubsSource,
        SourceFile.kotlin(
          "CodeMultiService.kt",
          """
          package com.example.service

          import com.github.kr328.kaidl.BinderInterface
          import com.github.kr328.kaidl.Code

          @BinderInterface
          interface CodeMultiService {
            @Code(100)
            fun first(): Int
          }
          """
            .trimIndent(),
        ),
      )

    val result = compilation.compile()

    assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

    val generated =
      compilation.kspSourcesDir.resolve("kotlin/com/example/service/CodeMultiService.kt")

    assertThat(generated).exists()

    val text = generated.readText()
    assertThat(text)
      .contains("public val TRANSACTION_first: Int = IBinder.FIRST_CALL_TRANSACTION + 100")
  }

  @Test
  fun codeAnnotationGeneratesCorrectTransactionCodes() {
    val compilation =
      newCompilation(
        annotationSource,
        runtimeSource,
        androidStubsSource,
        SourceFile.kotlin(
          "CodeTransactionService.kt",
          """
          package com.example.service

          import com.github.kr328.kaidl.BinderInterface
          import com.github.kr328.kaidl.Code

          @BinderInterface
          interface CodeTransactionService {
            @Code(50)
            fun methodA(value: Int): Int
          }
          """
            .trimIndent(),
        ),
      )

    val result = compilation.compile()

    assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

    val generated =
      compilation.kspSourcesDir.resolve("kotlin/com/example/service/CodeTransactionService.kt")

    assertThat(generated).exists()

    val text = generated.readText()
    // Verify delegate contains transaction code property
    assertThat(text)
      .contains("public val TRANSACTION_methodA: Int = IBinder.FIRST_CALL_TRANSACTION + 50")
    // Verify delegate uses the transaction code in onTransact
    assertThat(text).contains("TRANSACTION_methodA ->")
    // Verify proxy uses the transaction code
    assertThat(text).contains("remote.transact(CodeTransactionServiceDelegate.TRANSACTION_methodA,")
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

        interface Parcelable {
          fun writeToParcel(parcel: Parcel, flags: Int)

          fun describeContents(): Int

          interface Creator<T> {
            fun createFromParcel(parcel: Parcel): T
          }
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

          fun writeLong(value: Long) = Unit

          fun readLong(): Long = 0L

          fun writeString(value: String?) = Unit

          fun readString(): String? = null

          fun recycle() = Unit
        }
        """
          .trimIndent(),
      )

    val parcelizeStubsSource =
      SourceFile.kotlin(
        "ParcelizeStubs.kt",
        """
        package kotlinx.parcelize

        import android.os.Parcelable

        inline fun <reified T> parcelableCreator(): Parcelable.Creator<T> {
          throw UnsupportedOperationException("compile-fixture")
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

    val parcelableTypeSource =
      SourceFile.kotlin(
        "ConfigurationOverride.kt",
        """
        package com.example.service

        import android.os.Parcel
        import android.os.Parcelable

        class ConfigurationOverride : Parcelable {
          override fun writeToParcel(parcel: Parcel, flags: Int) = Unit

          override fun describeContents(): Int = 0
        }
        """
          .trimIndent(),
      )

    val parcelableServiceSource =
      SourceFile.kotlin(
        "ParcelableBridgeService.kt",
        """
        package com.example.service

        import com.github.kr328.kaidl.BinderInterface

        @BinderInterface
        interface ParcelableBridgeService {
          enum class OverrideSlot {
            SlotA,
            SlotB,
          }

          fun patchOverride(slot: OverrideSlot, configuration: ConfigurationOverride)
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

    val dateServiceSource =
      SourceFile.kotlin(
        "DateBridgeService.kt",
        """
        package com.example.service

        import com.github.kr328.kaidl.BinderInterface
        import java.util.Date

        @BinderInterface
        interface DateBridgeService {
          fun echoDate(value: Date): Date
        }
        """
          .trimIndent(),
      )

    val uuidServiceSource =
      SourceFile.kotlin(
        "UuidBridgeService.kt",
        """
        package com.example.service

        import com.github.kr328.kaidl.BinderInterface
        import java.util.UUID
        import kotlin.uuid.Uuid

        @OptIn(ExperimentalUuidApi::class)
        @BinderInterface
        interface UuidBridgeService {
          fun echoJavaUuid(javaUuid: UUID): UUID

          @OptIn(kotlin.uuid.ExperimentalUuidApi::class)
          fun echoKotlinUuid(kotlinUuid: Uuid): Uuid
        }
        """
          .trimIndent(),
      )
  }
}
