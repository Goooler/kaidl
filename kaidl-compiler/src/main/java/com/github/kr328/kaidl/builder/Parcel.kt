package com.github.kr328.kaidl.builder

import com.github.kr328.kaidl.resolver.PAIR
import com.github.kr328.kaidl.resolver.ParcelableType
import com.github.kr328.kaidl.resolver.canonicalName
import com.github.kr328.kaidl.resolver.packageName
import com.github.kr328.kaidl.resolver.parcelableType
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName

fun CodeBlock.Builder.addReadFromParcel(type: TypeName, parcelName: String): CodeBlock.Builder {
  if (type.isNullable) {
    beginControlFlow("if (%N.readInt() != 0)", parcelName)
  }

  when (type.canonicalName) {
    // internal types
    "kotlin.Int" -> addStatement("%N.readInt()", parcelName)
    "kotlin.Long" -> addStatement("%N.readLong()", parcelName)
    "kotlin.Float" -> addStatement("%N.readFloat()", parcelName)
    "kotlin.Double" -> addStatement("%N.readDouble()", parcelName)
    "kotlin.String" -> addStatement("checkNotNull(%N.readString())", parcelName)
    "kotlin.Byte" -> addStatement("%N.readByte()", parcelName)
    "kotlin.Unit" -> addStatement("Unit")
    "kotlin.Boolean" -> addStatement("%N.readInt() != 0", parcelName)
    "kotlin.ByteArray" -> addStatement("checkNotNull(%N.createByteArray())", parcelName)
    "kotlin.CharArray" -> addStatement("checkNotNull(%N.createCharArray())", parcelName)
    "kotlin.BooleanArray" -> addStatement("checkNotNull(%N.createBooleanArray())", parcelName)
    "kotlin.IntArray" -> addStatement("checkNotNull(%N.createIntArray())", parcelName)
    "kotlin.LongArray" -> addStatement("checkNotNull(%N.createLongArray())", parcelName)
    "kotlin.FloatArray" -> addStatement("checkNotNull(%N.createFloatArray())", parcelName)
    "kotlin.DoubleArray" -> addStatement("checkNotNull(%N.createDoubleArray())", parcelName)
    "android.os.IBinder" -> addStatement("checkNotNull(%N.readStrongBinder())", parcelName)
    "android.os.Bundle" -> addStatement("checkNotNull(%N.readBundle())", parcelName)
    "android.util.SparseBooleanArray" ->
      addStatement("checkNotNull(%N.readSparseBooleanArray())", parcelName)
    "java.util.Date" -> addStatement("%T(%N.readLong())", type.copy(nullable = false), parcelName)
    "java.util.UUID" ->
      addStatement(
        "%T.fromString(checkNotNull(%N.readString()))",
        type.copy(nullable = false),
        parcelName,
      )
    "kotlin.uuid.Uuid" ->
      addStatement("%T.parse(checkNotNull(%N.readString()))", type.copy(nullable = false), parcelName)

    // collections
    "kotlin.Pair" -> {
      type as ParameterizedTypeName

      beginControlFlow("run")

      addReadFromParcel("first", type.typeArguments[0], parcelName)
      addReadFromParcel("second", type.typeArguments[1], parcelName)

      addStatement("first to second")

      endControlFlow()
    }
    "kotlin.collections.List" -> {
      type as ParameterizedTypeName

      beginControlFlow("%T(%N.readInt())", type, parcelName)

      addReadFromParcel(type.typeArguments[0], parcelName)

      endControlFlow()
    }
    "kotlin.Array" -> {
      type as ParameterizedTypeName

      beginControlFlow("%T(%N.readInt())", type, parcelName)

      addReadFromParcel(type.typeArguments[0], parcelName)

      endControlFlow()
    }
    "kotlin.collections.Set" -> {
      type as ParameterizedTypeName

      beginControlFlow("%T(%N.readInt())", LIST.parameterizedBy(type.typeArguments[0]), parcelName)

      addReadFromParcel(type.typeArguments[0], parcelName)

      endControlFlow()

      add(".%M()", MemberName("kotlin.collections", "toSet"))
    }
    "kotlin.collections.Map" -> {
      type as ParameterizedTypeName

      beginControlFlow(
        "%T(%N.readInt())",
        LIST.parameterizedBy(PAIR.parameterizedBy(type.typeArguments[0], type.typeArguments[1])),
        parcelName,
      )

      addReadFromParcel(
        PAIR.parameterizedBy(type.typeArguments[0], type.typeArguments[1]),
        parcelName,
      )

      endControlFlow()

      addStatement(".%M()", MemberName("kotlin.collections", "toMap"))
    }

    // parcelables
    else -> {
      when (type.parcelableType) {
        ParcelableType.BinderInterface ->
          addStatement(
            "checkNotNull(%N.readStrongBinder()).%M(%T::class)",
            parcelName,
            MemberName(type.packageName, "unwrap"),
            type.copy(nullable = false),
          )
        ParcelableType.AidlInterface -> {
          addStatement(
            "%T.asInterface(checkNotNull(%N.readStrongBinder()))",
            (type.copy(nullable = false) as ClassName).nestedClass("Stub"),
            parcelName,
          )
        }
        ParcelableType.Parcelable -> {
          addStatement(
            "checkNotNull(%M<%T>().createFromParcel(%N))",
            MemberName("kotlinx.parcelize", "parcelableCreator"),
            type.copy(nullable = false),
            parcelName,
          )
        }
        ParcelableType.Serializable -> {
          addStatement("%N.readSerializable() as %T", parcelName, type.copy(nullable = false))
        }
        ParcelableType.Enum -> {
          addStatement("%T.values()[%N.readInt()]", type.copy(nullable = false), parcelName)
        }
      }
    }
  }

  if (type.isNullable) {
    nextControlFlow("else")

    addStatement("null")

    endControlFlow()
  }

  return this
}

fun CodeBlock.Builder.addReadFromParcel(
  valName: String,
  type: TypeName,
  parcelName: String,
): CodeBlock.Builder {
  return add("val %N: %T = ", valName, type).addReadFromParcel(type, parcelName)
}

fun CodeBlock.Builder.addWriteToParcel(
  valName: String,
  type: TypeName,
  parcelName: String,
): CodeBlock.Builder {
  if (type.isNullable) {
    beginControlFlow("if (%N != null)", valName)

    addStatement("%N.writeInt(%L)", parcelName, 1)
  }

  when (type.canonicalName) {
    // internal types
    "kotlin.Int" -> addStatement("%N.writeInt(%N)", parcelName, valName)
    "kotlin.Long" -> addStatement("%N.writeLong(%N)", parcelName, valName)
    "kotlin.Float" -> addStatement("%N.writeFloat(%N)", parcelName, valName)
    "kotlin.Double" -> addStatement("%N.writeDouble(%N)", parcelName, valName)
    "kotlin.String" -> addStatement("%N.writeString(%N)", parcelName, valName)
    "kotlin.Byte" -> addStatement("%N.writeByte(%N)", parcelName, valName)
    "kotlin.Unit" -> Unit
    "kotlin.Boolean" -> addStatement("%N.writeInt(if (%N) 1 else 0)", parcelName, valName)
    "kotlin.ByteArray" -> addStatement("%N.writeByteArray(%N)", parcelName, valName)
    "kotlin.CharArray" -> addStatement("%N.writeCharArray(%N)", parcelName, valName)
    "kotlin.BooleanArray" -> addStatement("%N.writeBooleanArray(%N)", parcelName, valName)
    "kotlin.IntArray" -> addStatement("%N.writeIntArray(%N)", parcelName, valName)
    "kotlin.LongArray" -> addStatement("%N.writeLongArray(%N)", parcelName, valName)
    "kotlin.FloatArray" -> addStatement("%N.writeFloatArray(%N)", parcelName, valName)
    "kotlin.DoubleArray" -> addStatement("%N.writeDoubleArray(%N)", parcelName, valName)
    "android.os.IBinder" -> addStatement("%N.writeStrongBinder(%N)", parcelName, valName)
    "android.os.Bundle" -> addStatement("%N.writeBundle(%N)", parcelName, valName)
    "android.util.SparseBooleanArray" ->
      addStatement("%N.writeSparseBooleanArray(%N)", parcelName, valName)
    "java.util.Date" -> addStatement("%N.writeLong(%N.time)", parcelName, valName)
    "java.util.UUID" -> addStatement("%N.writeString(%N.toString())", parcelName, valName)
    "kotlin.uuid.Uuid" -> addStatement("%N.writeString(%N.toString())", parcelName, valName)

    // collections
    "kotlin.Pair" -> {
      type as ParameterizedTypeName

      addStatement("val first = %N.first", valName)
      addStatement("val second = %N.second", valName)

      addWriteToParcel("first", type.typeArguments[0], parcelName)
      addWriteToParcel("second", type.typeArguments[1], parcelName)
    }
    "kotlin.collections.List",
    "kotlin.collections.Set",
    "kotlin.Array" -> {
      type as ParameterizedTypeName

      addStatement("%N.writeInt(%N.size)", parcelName, valName)

      beginControlFlow("%N.%M", valName, MemberName("kotlin.collections", "forEach"))

      addWriteToParcel("it", type.typeArguments[0], parcelName)

      endControlFlow()
    }
    "kotlin.collections.Map" -> {
      type as ParameterizedTypeName

      addStatement("val list = %N.%M()", valName, MemberName("kotlin.collections", "toList"))

      addWriteToParcel(
        "list",
        LIST.parameterizedBy(PAIR.parameterizedBy(type.typeArguments[0], type.typeArguments[1])),
        parcelName,
      )
    }

    else -> {
      when (type.parcelableType) {
        ParcelableType.BinderInterface -> {
          addStatement(
            "%N.writeStrongBinder(%N.%M())",
            parcelName,
            valName,
            MemberName(type.packageName, "wrap"),
          )
        }
        ParcelableType.AidlInterface -> {
          addStatement("%N.writeStrongBinder(%N.asBinder())", parcelName, valName)
        }
        ParcelableType.Parcelable -> {
          addStatement("%N.writeToParcel(%N, 0)", valName, parcelName)
        }
        ParcelableType.Serializable -> {
          addStatement("%N.writeSerializable(%N)", parcelName, valName)
        }
        ParcelableType.Enum -> {
          addStatement("%N.writeInt(%N.ordinal)", parcelName, valName)
        }
      }
    }
  }

  if (type.isNullable) {
    nextControlFlow("else")

    addStatement("%N.writeInt(%L)", parcelName, 0)

    endControlFlow()
  }

  return this
}
