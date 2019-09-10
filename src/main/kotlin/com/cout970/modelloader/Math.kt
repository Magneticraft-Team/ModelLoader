package com.cout970.modelloader

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import net.minecraft.util.Direction
import net.minecraftforge.common.model.ITransformation
import java.lang.reflect.Type
import javax.vecmath.*
import kotlin.math.sqrt

data class TRSTransformation(
    val translation: Vector3d = Vector3d(),
    val rotation: Quat4d = Quat4d(0.0, 0.0, 0.0, 1.0),
    val scale: Vector3d = Vector3d()
) : ITransformation {

    // Gson pls
    private constructor() : this(Vector3d(), Quat4d(0.0, 0.0, 0.0, 1.0), Vector3d())

    override fun getMatrixVec(): Matrix4f {
        val self = this
        return Matrix4f().apply {
            setIdentity()

            // rotation
            setRotation(-self.rotation)

            // translation
            m30 = self.translation.x.toFloat()
            m31 = self.translation.y.toFloat()
            m32 = self.translation.z.toFloat()

            // scale
            m00 *= self.scale.x.toFloat()
            m01 *= self.scale.x.toFloat()
            m02 *= self.scale.x.toFloat()
            m10 *= self.scale.y.toFloat()
            m11 *= self.scale.y.toFloat()
            m12 *= self.scale.y.toFloat()
            m20 *= self.scale.z.toFloat()
            m21 *= self.scale.z.toFloat()
            m22 *= self.scale.z.toFloat()
        }
    }

    operator fun plus(other: TRSTransformation): TRSTransformation {
        return Matrix4d(this.matrixVec * other.matrixVec).toTRS()
    }

    operator fun times(other: TRSTransformation): TRSTransformation {
        return TRSTransformation(
            translation = this.rotation.rotate(other.translation) + this.translation * other.scale,
            rotation = this.rotation * other.rotation,
            scale = this.scale * other.scale
        )
    }

    fun lerp(other: TRSTransformation, step: Float): TRSTransformation {
        return TRSTransformation(
            translation = this.translation.interpolated(other.translation, step.toDouble()),
            rotation = this.rotation.interpolated(other.rotation, step.toDouble()),
            scale = this.scale.interpolated(other.scale, step.toDouble())
        )
    }

    override fun rotate(facing: Direction): Direction = facing

    override fun rotate(facing: Direction, vertexIndex: Int): Int = vertexIndex
}

internal fun Matrix4d.getRotation(): Quat4d {
    return Quat4d().apply { set(this@getRotation) }
}

internal fun Matrix4d.toTRS(): TRSTransformation {
    val translation = Vector3d(this.m30, this.m31, this.m32)
    val rotation = this.getRotation()
    val scale = Vector3d(
        sqrt(this.m00 * this.m00 + this.m01 * this.m01 + this.m02 * this.m02),
        sqrt(this.m10 * this.m10 + this.m11 * this.m11 + this.m12 * this.m12),
        sqrt(this.m20 * this.m20 + this.m21 * this.m21 + this.m22 * this.m22)
    )

    return TRSTransformation(translation, rotation, scale)
}

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