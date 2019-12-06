package com.cout970.modelloader.api

import com.cout970.modelloader.*
import com.cout970.modelloader.interpolated
import com.cout970.modelloader.mutable.MutableTRSTransformation
import com.cout970.modelloader.rotate
import com.cout970.modelloader.toTRS
import net.minecraft.util.Direction
import net.minecraftforge.client.ForgeHooksClient
import net.minecraftforge.common.model.ITransformation
import javax.vecmath.*

/**
 * Class that represents a transformation that consists in translation, rotation and scale,
 * can be converted to a 4x4 matrix.
 */
data class TRSTransformation(
    val translationX: Float,
    val translationY: Float,
    val translationZ: Float,
    val rotationX: Float,
    val rotationY: Float,
    val rotationZ: Float,
    val rotationW: Float,
    val scaleX: Float,
    val scaleY: Float,
    val scaleZ: Float
) : ITransformation {

    val translation: Vector3d get() = Vector3d(translationX.toDouble(), translationY.toDouble(), translationZ.toDouble())
    val rotation: Quat4d get() = Quat4d(rotationX.toDouble(), rotationY.toDouble(), rotationZ.toDouble(), rotationW.toDouble())
    val scale: Vector3d get() = Vector3d(scaleX.toDouble(), scaleY.toDouble(), scaleZ.toDouble())

    constructor(translation: Vector3d = Vector3d(), rotation: Quat4d = Quat4d(), scale: Vector3d = Vector3d(1.0, 1.0, 1.0)) : this(
        translation.x.toFloat(), translation.y.toFloat(), translation.z.toFloat(),
        rotation.x.toFloat(), rotation.y.toFloat(), rotation.z.toFloat(), rotation.w.toFloat(),
        scale.x.toFloat(), scale.y.toFloat(), scale.z.toFloat()
    )

    constructor(translation: Vector3f, rotation: Quat4f, scale: Vector3f) : this(
        translation.x, translation.y, translation.z,
        rotation.x, rotation.y, rotation.z, rotation.w,
        scale.x, scale.y, scale.z
    )

    // Gson pls
    @Suppress("unused")
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
            if (rotationW != 0f) {
                this.setRotation(rotation.apply { inverse() })
            }

            // translation
            m30 = self.translationX
            m31 = self.translationY
            m32 = self.translationZ

            // scale
            m00 *= self.scaleX
            m01 *= self.scaleX
            m02 *= self.scaleX
            m10 *= self.scaleY
            m11 *= self.scaleY
            m12 *= self.scaleY
            m20 *= self.scaleZ
            m21 *= self.scaleZ
            m22 *= self.scaleZ
        }
    }

    /**
     * Applies the translation, rotation, scale to OpenGL,
     * Equivalent to Gl11.glTranslatef(...), Gl11.glRotatef(...), Gl11.glScalef(...) but faster
     */
    fun glMultiply() {
        val matrix = matrixVec
        matrix.transpose()
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
     * Creates a mutable version of this transformation
     */
    fun toMutable(): MutableTRSTransformation {
        return MutableTRSTransformation(
            translation = Vector3f(translationX, translationY, translationZ),
            rotation = Quat4f(rotationX, rotationY, rotationZ, rotationW),
            scale = Vector3f(scaleX, scaleY, scaleZ)
        )
    }

    /**
     * Method required by ITransformation, does nothing
     */
    override fun rotateTransform(facing: Direction): Direction = facing

    /**
     * Method required by ITransformation, does nothing
     */
    override fun rotate(facing: Direction, vertexIndex: Int): Int = vertexIndex
    
    companion object{
        val IDENTITY: TRSTransformation = TRSTransformation() 
    }
}