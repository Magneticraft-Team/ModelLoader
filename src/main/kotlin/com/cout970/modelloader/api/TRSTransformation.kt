package com.cout970.modelloader.api

import com.cout970.modelloader.*
import net.minecraft.util.Direction
import net.minecraftforge.client.ForgeHooksClient
import net.minecraftforge.common.model.ITransformation
import javax.vecmath.*

/**
 * Class that represents a transformation that consists in translation, rotation and scale,
 * can be converted to a 4x4 matrix.
 */
data class TRSTransformation(
    val translation: Vector3d = Vector3d(),
    val rotation: Quat4d = Quat4d(0.0, 0.0, 0.0, 1.0),
    val scale: Vector3d = Vector3d(1.0, 1.0, 1.0)
) : ITransformation {

    // Gson pls
    private constructor() : this(Vector3d(), Quat4d(0.0, 0.0, 0.0, 1.0), Vector3d(1.0, 1.0, 1.0))

    /**
     * Converts this transformation into a Matrix
     * val matrix = getTransform(node, time).matrixVec.apply { transpose() }
    ForgeHooksClient.multiplyCurrentGlMatrix(matrix)
     */
    override fun getMatrixVec(): Matrix4f {
        val self = this
        return Matrix4f().apply {
            setIdentity()

            // rotation
            if (rotation.w != 0.0) {
                this.setRotation(Quat4f(rotation).apply { inverse() })
            }

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

    /**
     * Applies the translation, rotation, scale to OpenGL,
     * Equivalent to Gl11.glTranslatef(...), Gl11.glRotatef(...), Gl11.glScalef(...) but faster
     */
    fun glMultiply() {
        val matrix = matrixVec.apply { transpose() }
        ForgeHooksClient.multiplyCurrentGlMatrix(matrix)
    }

    /**
     * Converts this transformation into a Matrix
     */
    fun getMatrix4f(): Matrix4f = matrixVec

    /**
     * Convenient double version of the matrix
     */
    fun getMatrix4d(): Matrix4d = Matrix4d(matrixVec)

    /**
     * Combines two transformations using matrix multiplication
     */
    operator fun plus(other: TRSTransformation): TRSTransformation {
        return Matrix4d(this.matrixVec * other.matrixVec).toTRS()
    }

    /**
     * Alternative combination of two transformations
     */
    operator fun times(other: TRSTransformation): TRSTransformation {
        return TRSTransformation(
            translation = this.rotation.rotate(other.translation) + this.translation * other.scale,
            rotation = this.rotation * other.rotation,
            scale = this.scale * other.scale
        )
    }

    /**
     * Linear interpolation of two transformations
     * @param step must be a value between 0.0 and 1.0 (both inclusive)
     */
    fun lerp(other: TRSTransformation, step: Float): TRSTransformation {
        return TRSTransformation(
            translation = this.translation.interpolated(other.translation, step.toDouble()),
            rotation = this.rotation.interpolated(other.rotation, step.toDouble()),
            scale = this.scale.interpolated(other.scale, step.toDouble())
        )
    }

    /**
     * Method required by ITransformation, does nothing
     */
    override fun rotate(facing: Direction): Direction = facing

    /**
     * Method required by ITransformation, does nothing
     */
    override fun rotate(facing: Direction, vertexIndex: Int): Int = vertexIndex
}