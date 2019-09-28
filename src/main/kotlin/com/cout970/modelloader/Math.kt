package com.cout970.modelloader

import com.cout970.modelloader.api.TRSTransformation
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type
import javax.vecmath.*
import kotlin.math.sqrt

// Internal math utilities
// They are internal to avoid global environment pollution

internal fun Matrix4d.getRotation(): Quat4d {
    return Quat4d().apply { set(this@getRotation) }
}

internal fun Matrix4d.toTRS(): TRSTransformation {
    val translation = Vector3d(this.m30, this.m31, this.m32)
    val rotation = Quat4d().setFromUnnormalized(this)
    val scale = Vector3d(
        sqrt(this.m00 * this.m00 + this.m01 * this.m01 + this.m02 * this.m02),
        sqrt(this.m10 * this.m10 + this.m11 * this.m11 + this.m12 * this.m12),
        sqrt(this.m20 * this.m20 + this.m21 * this.m21 + this.m22 * this.m22)
    )

    return TRSTransformation(translation, rotation, scale)
}

internal fun Matrix4f.toTRS(): TRSTransformation = Matrix4d(this).toTRS()

internal operator fun Matrix4f.times(other: Matrix4f): Matrix4f {
    return Matrix4f(this).apply { mul(other) }
}

internal operator fun Vector3d.plus(other: Vector3d): Vector3d {
    return Vector3d(this).apply { add(other) }
}

internal operator fun Vector3d.minus(other: Vector3d): Vector3d {
    return Vector3d(this).apply { sub(other) }
}

internal operator fun Vector3d.times(other: Vector3d): Vector3d {
    return Vector3d(this).apply { scale(1.0, other) }
}

internal operator fun Vector3d.div(other: Vector3d): Vector3d {
    return Vector3d(this.x / other.x, this.y / other.y, this.z / other.z)
}

internal fun Vector3d.norm(): Vector3d {
    return Vector3d(this).apply { normalize() }
}

internal fun Quat4d.rotate(vec: Vector3d): Vector3d {
    val x2 = x + x
    val y2 = y + y
    val z2 = z + z
    val x2sq = x * x2
    val y2sq = y * y2
    val z2sq = z * z2
    val xy2 = x * y2
    val xz2 = x * z2
    val yz2 = y * z2
    val wx2 = w * x2
    val wy2 = w * y2
    val wz2 = w * z2
    return Vector3d(
        (1.0 - (y2sq + z2sq)) * vec.x + (xy2 - wz2) * vec.y + (xz2 + wy2) * vec.z,
        (xy2 + wz2) * vec.x + (1.0 - (x2sq + z2sq)) * vec.y + (yz2 - wx2) * vec.z,
        (xz2 - wy2) * vec.x + (yz2 + wx2) * vec.y + (1.0 - (x2sq + y2sq)) * vec.z)
}

internal operator fun Quat4d.times(other: Quat4d): Quat4d {
    return Quat4d(this).apply { mul(other) }
}

internal fun Quat4d.interpolated(other: Quat4d, step: Double): Quat4d {
    return Quat4d(this).apply { interpolate(other, step) }
}

internal fun Vector3d.interpolated(other: Vector3d, step: Double): Vector3d {
    return Vector3d(this).apply { interpolate(other, step) }
}

internal operator fun Quat4d.unaryMinus(): Quat4d {
    return Quat4d(this).apply { inverse() }
}

internal operator fun Vector3d.unaryMinus(): Vector3d {
    return Vector3d(-this.x, -this.y, -this.z)
}

internal infix fun Vector3d.cross(other: Vector3d): Vector3d {
    return Vector3d().also { it.cross(this, other) }
}

internal fun Vector4d.asQuaternion(): Quat4d {
    return Quat4d(this)
}

internal fun Quat4d.setFromUnnormalized(mat: Matrix4d): Quat4d {
    var nm00 = mat.m00
    var nm01 = mat.m01
    var nm02 = mat.m02
    var nm10 = mat.m10
    var nm11 = mat.m11
    var nm12 = mat.m12
    var nm20 = mat.m20
    var nm21 = mat.m21
    var nm22 = mat.m22
    val lenX = 1.0f / sqrt(mat.m00 * mat.m00 + mat.m01 * mat.m01 + mat.m02 * mat.m02)
    val lenY = 1.0f / sqrt(mat.m10 * mat.m10 + mat.m11 * mat.m11 + mat.m12 * mat.m12)
    val lenZ = 1.0f / sqrt(mat.m20 * mat.m20 + mat.m21 * mat.m21 + mat.m22 * mat.m22)
    nm00 *= lenX
    nm01 *= lenX
    nm02 *= lenX
    nm10 *= lenY
    nm11 *= lenY
    nm12 *= lenY
    nm20 *= lenZ
    nm21 *= lenZ
    nm22 *= lenZ
    setFromNormalized(nm00, nm01, nm02, nm10, nm11, nm12, nm20, nm21, nm22)
    return this
}

private fun Quat4d.setFromNormalized(m00: Double, m01: Double, m02: Double,
                                     m10: Double, m11: Double, m12: Double,
                                     m20: Double, m21: Double, m22: Double) {
    var t: Double
    val tr = m00 + m11 + m22
    if (tr >= 0.0f) {
        t = sqrt(tr + 1.0)
        w = t * 0.5
        t = 0.5f / t
        x = (m12 - m21) * t
        y = (m20 - m02) * t
        z = (m01 - m10) * t
    } else {
        if (m00 >= m11 && m00 >= m22) {
            t = sqrt(m00 - (m11 + m22) + 1.0)
            x = t * 0.5
            t = 0.5f / t
            y = (m10 + m01) * t
            z = (m02 + m20) * t
            w = (m12 - m21) * t
        } else if (m11 > m22) {
            t = sqrt(m11 - (m22 + m00) + 1.0)
            y = t * 0.5
            t = 0.5f / t
            z = (m21 + m12) * t
            x = (m10 + m01) * t
            w = (m20 - m02) * t
        } else {
            t = sqrt(m22 - (m00 + m11) + 1.0)
            z = t * 0.5
            t = 0.5f / t
            x = (m02 + m20) * t
            y = (m21 + m12) * t
            w = (m01 - m10) * t
        }
    }
}

internal class Vector4Deserializer : JsonDeserializer<Vector4d> {
    override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext?): Vector4d {
        val arr = json.asJsonArray
        return Vector4d(arr[0].asDouble, arr[1].asDouble, arr[2].asDouble, arr[3].asDouble)
    }
}

internal class Vector3Deserializer : JsonDeserializer<Vector3d> {
    override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext?): Vector3d {
        val arr = json.asJsonArray
        return Vector3d(arr[0].asDouble, arr[1].asDouble, arr[2].asDouble)
    }
}

internal class Vector2Deserializer : JsonDeserializer<Vector2d> {
    override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext?): Vector2d {
        val arr = json.asJsonArray
        return Vector2d(arr[0].asDouble, arr[1].asDouble)
    }
}

internal class QuaternionDeserializer : JsonDeserializer<Quat4d> {
    override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext?): Quat4d {
        val arr = json.asJsonArray
        return Quat4d(arr[0].asDouble, arr[1].asDouble, arr[2].asDouble, arr[3].asDouble)
    }
}

internal class Matrix4Deserializer : JsonDeserializer<Matrix4d> {
    override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext?): Matrix4d {
        val arr = json.asJsonArray
        return Matrix4d(
            arr[0].asDouble, arr[1].asDouble, arr[2].asDouble, arr[3].asDouble,
            arr[4].asDouble, arr[5].asDouble, arr[6].asDouble, arr[7].asDouble,
            arr[8].asDouble, arr[9].asDouble, arr[10].asDouble, arr[11].asDouble,
            arr[12].asDouble, arr[13].asDouble, arr[14].asDouble, arr[15].asDouble
        )
    }
}