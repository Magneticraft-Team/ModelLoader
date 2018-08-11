package com.cout970.modelloader.api.util

import com.cout970.modelloader.api.formats.gltf.IMatrix4
import com.cout970.vector.api.IQuaternion
import com.cout970.vector.api.IVector3
import com.cout970.vector.extensions.*
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.model.ITransformation
import javax.vecmath.Matrix4f
import javax.vecmath.Quat4f

data class TRSTransformation(
        val translation: IVector3 = Vector3.ORIGIN,
        val rotation: IQuaternion = Quaternion.IDENTITY,
        val scale: IVector3 = Vector3.ONE
) : ITransformation {

    // Gson pls
    private constructor() : this(Vector3.ORIGIN, Quaternion.IDENTITY, Vector3.ONE)

    companion object {

        val IDENTITY = TRSTransformation(Vector3.ORIGIN, Quaternion.IDENTITY, Vector3.ONE)

        fun fromMatrix(mat: IMatrix4): TRSTransformation {
            val translation = vec3Of(mat.m30, mat.m31, mat.m32)
            val rotation = IQuaternion().setFromUnnormalized(mat)
            val scale = vec3Of(
                    Math.sqrt(mat.m00 * mat.m00 + mat.m01 * mat.m01 + mat.m02 * mat.m02.toDouble()),
                    Math.sqrt(mat.m10 * mat.m10 + mat.m11 * mat.m11 + mat.m12 * mat.m12.toDouble()),
                    Math.sqrt(mat.m20 * mat.m20 + mat.m21 * mat.m21 + mat.m22 * mat.m22.toDouble())
            )

            return TRSTransformation(translation, rotation, scale)
        }
    }

    override fun getMatrix(): Matrix4f = Matrix4f().apply {
        setIdentity()

        // rotation
        this.setRotation(Quat4f(rotation).apply { inverse() })

        // translation
        m30 = translation.xf
        m31 = translation.yf
        m32 = translation.zf

        // scale
        m00 *= this@TRSTransformation.scale.xf
        m01 *= this@TRSTransformation.scale.xf
        m02 *= this@TRSTransformation.scale.xf
        m10 *= this@TRSTransformation.scale.yf
        m11 *= this@TRSTransformation.scale.yf
        m12 *= this@TRSTransformation.scale.yf
        m20 *= this@TRSTransformation.scale.zf
        m21 *= this@TRSTransformation.scale.zf
        m22 *= this@TRSTransformation.scale.zf

        transpose()
    }

    operator fun plus(other: TRSTransformation): TRSTransformation {
        return TRSTransformation.fromMatrix(this.matrix * other.matrix)
    }

    operator fun times(other: TRSTransformation): TRSTransformation {
        return TRSTransformation(
                translation = this.rotation.rotate(other.translation) + this.translation * other.scale,
                rotation = Quat4f(this.rotation).also { it.mul(other.rotation) },
                scale = this.scale * other.scale
        )
    }

    fun lerp(other: TRSTransformation, step: Float): TRSTransformation {
        return TRSTransformation(
                translation = this.translation.interpolate(other.translation, step.toDouble()),
                rotation = Quat4f().apply { interpolate(rotation, other.rotation, step) },
                scale = this.scale.interpolate(other.scale, step.toDouble())
        )
    }

    override fun rotate(facing: EnumFacing): EnumFacing {
        return facing
    }

    override fun rotate(facing: EnumFacing, vertexIndex: Int): Int = vertexIndex
}