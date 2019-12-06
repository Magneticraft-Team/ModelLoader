package com.cout970.modelloader.mutable

import com.cout970.modelloader.api.TRSTransformation
import net.minecraftforge.client.ForgeHooksClient
import javax.vecmath.*

/**
 * Encodes translation, rotation and scale of a 3D object
 * It's mutable to avoid unnecessary memory allocations while rendering
 */
class MutableTRSTransformation(
    val translation: Vector3f = Vector3f(),
    val rotation: Quat4f = Quat4f(),
    val scale: Vector3f = Vector3f(1f, 1f, 1f)
) {
    /**
     * Resets the transformation to the default state
     */
    fun reset() {
        resetTranslation()
        resetRotation()
        resetScale()
    }

    /**
     * Sets the current translation
     */
    fun moveTo(x: Float, y: Float, z: Float) {
        translation.x = x
        translation.y = y
        translation.z = z
    }

    /**
     * Adds an offset to the current translation
     */
    fun translate(x: Float, y: Float, z: Float) {
        translation.x += x
        translation.y += y
        translation.z += z
    }

    /**
     * Sets the translation to 0
     */
    fun resetTranslation() {
        translation.x = 0f
        translation.y = 0f
        translation.z = 0f
    }

    /**
     * Rotates in the X axis by [angle] radians
     */
    fun rotateX(angle: Float) {
        rotate(angle, 1f, 0f, 0f)
    }

    /**
     * Rotates in the Y axis by [angle] radians
     */
    fun rotateY(angle: Float) {
        rotate(angle, 0f, 1f, 0f)
    }

    /**
     * Rotates in the Z axis by [angle] radians
     */
    fun rotateZ(angle: Float) {
        rotate(angle, 0f, 0f, 1f)
    }

    /**
     * Rotates around an arbitrary axis ([x], [y], [z]) by [angle] radians
     */
    fun rotate(angle: Float, x: Float, y: Float, z: Float) {
        val rot = Quat4f()
        rot.set(AxisAngle4f(x, y, z, angle))
        rotation.mul(rot)
    }

    /**
     * Rotates in 3 axis, using Euler rotations in radians
     */
    fun rotate(x: Float, y: Float, z: Float) {
        rotateX(x)
        rotateY(y)
        rotateZ(z)
    }

    /**
     * Rotates in the X axis by [angle] degrees
     */
    fun rotateDegX(angle: Float) {
        rotate(Math.toRadians(angle.toDouble()).toFloat(), 1f, 0f, 0f)
    }

    /**
     * Rotates in the Y axis by [angle] degrees
     */
    fun rotateDegY(angle: Float) {
        rotate(Math.toRadians(angle.toDouble()).toFloat(), 0f, 1f, 0f)
    }

    /**
     * Rotates in the Z axis by [angle] degrees
     */
    fun rotateDegZ(angle: Float) {
        rotate(Math.toRadians(angle.toDouble()).toFloat(), 0f, 0f, 1f)
    }

    /**
     * Rotates around an arbitrary axis ([x], [y], [z]) by [angle] degrees
     */
    fun rotateDeg(angle: Float, x: Float, y: Float, z: Float) {
        val rot = Quat4f()
        rot.set(AxisAngle4f(x, y, z, Math.toRadians(angle.toDouble()).toFloat()))
        rotation.mul(rot)
    }

    /**
     * Rotates in 3 axis, using Euler rotations in degrees
     */
    fun rotateDeg(x: Float, y: Float, z: Float) {
        rotateX(Math.toRadians(x.toDouble()).toFloat())
        rotateY(Math.toRadians(y.toDouble()).toFloat())
        rotateZ(Math.toRadians(z.toDouble()).toFloat())
    }

    /**
     * Sets the rotation to no-rotation
     */
    fun resetRotation() {
        rotation.x = 0f
        rotation.y = 0f
        rotation.z = 0f
        rotation.w = 0f
    }

    /**
     * Sets the scale factor
     */
    fun scale(factor: Float) {
        scale.x = factor
        scale.y = factor
        scale.z = factor
    }

    /**
     * Sets the scale factor for each axis
     */
    fun scale(x: Float, y: Float, z: Float) {
        scale.x = x
        scale.y = y
        scale.z = z
    }

    /**
     * Multiplies the scale factor
     */
    fun multiplyScale(factor: Float) {
        scale.x *= factor
        scale.y *= factor
        scale.z *= factor
    }

    /**
     * Multiplies the scale factor for each axis
     */
    fun multiplyScale(x: Float, y: Float, z: Float) {
        scale.x *= x
        scale.y *= y
        scale.z *= z
    }

    /**
     * Sets the scale to 1
     */
    fun resetScale() {
        scale.x = 1f
        scale.y = 1f
        scale.z = 1f
    }

    /**
     * Applies the translation, rotation and scale to the OpenGLMatrix
     *
     * Equivalent to glTranslate, glRotate and glScale, but all together.
     */
    fun glMultiply() {
        auxMatrix.setIdentity()

        // rotation
        if (rotation.w != 0.0f) {
            auxQuat.set(rotation)
            auxQuat.inverse()
            auxMatrix.setRotation(auxQuat)
        }

        // translation
        auxMatrix.m30 = translation.x
        auxMatrix.m31 = translation.y
        auxMatrix.m32 = translation.z

        // scale
        auxMatrix.m00 *= scale.x
        auxMatrix.m01 *= scale.x
        auxMatrix.m02 *= scale.x
        auxMatrix.m10 *= scale.y
        auxMatrix.m11 *= scale.y
        auxMatrix.m12 *= scale.y
        auxMatrix.m20 *= scale.z
        auxMatrix.m21 *= scale.z
        auxMatrix.m22 *= scale.z

        auxMatrix.transpose()
        ForgeHooksClient.multiplyCurrentGlMatrix(auxMatrix)
    }

    /**
     * Creates a immutable version of this transformation
     */
    fun toImmutable(): TRSTransformation {
        return TRSTransformation(
            translation = Vector3d(translation),
            rotation = Quat4d(rotation),
            scale = Vector3d(scale)
        )
    }

    companion object {
        // We use a fixed matrix and a fixed quad to avoid unnecessary allocations
        // This is safe because rendering is single-threaded, only one thread can read/write at the same time
        private val auxMatrix = Matrix4f()
        private val auxQuat = Quat4f()
    }
}