package com.cout970.modelloader.api.util

import com.cout970.modelloader.api.formats.gltf.IMatrix4
import com.cout970.vector.api.IQuaternion

operator fun IQuaternion.times(other: IQuaternion): IQuaternion {
    return IQuaternion(this).also { it.mul(other) }
}

operator fun IMatrix4.times(other: IMatrix4): IMatrix4 {
    return IMatrix4(this).also { it.mul(other) }
}

fun IQuaternion.setFromUnnormalized(mat: IMatrix4): IQuaternion {
    var nm00 = mat.m00
    var nm01 = mat.m01
    var nm02 = mat.m02
    var nm10 = mat.m10
    var nm11 = mat.m11
    var nm12 = mat.m12
    var nm20 = mat.m20
    var nm21 = mat.m21
    var nm22 = mat.m22
    val lenX = 1.0f / Math.sqrt(mat.m00 * mat.m00 + mat.m01 * mat.m01 + mat.m02 * mat.m02.toDouble()).toFloat()
    val lenY = 1.0f / Math.sqrt(mat.m10 * mat.m10 + mat.m11 * mat.m11 + mat.m12 * mat.m12.toDouble()).toFloat()
    val lenZ = 1.0f / Math.sqrt(mat.m20 * mat.m20 + mat.m21 * mat.m21 + mat.m22 * mat.m22.toDouble()).toFloat()
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

private fun IQuaternion.setFromNormalized(m00: Float, m01: Float, m02: Float,
                                          m10: Float, m11: Float, m12: Float,
                                          m20: Float, m21: Float, m22: Float) {
    var t: Float
    val tr = m00 + m11 + m22
    if (tr >= 0.0f) {
        t = Math.sqrt(tr + 1.0).toFloat()
        w = t * 0.5f
        t = 0.5f / t
        x = (m12 - m21) * t
        y = (m20 - m02) * t
        z = (m01 - m10) * t
    } else {
        if (m00 >= m11 && m00 >= m22) {
            t = Math.sqrt(m00 - (m11 + m22) + 1.0).toFloat()
            x = t * 0.5f
            t = 0.5f / t
            y = (m10 + m01) * t
            z = (m02 + m20) * t
            w = (m12 - m21) * t
        } else if (m11 > m22) {
            t = Math.sqrt(m11 - (m22 + m00) + 1.0).toFloat()
            y = t * 0.5f
            t = 0.5f / t
            z = (m21 + m12) * t
            x = (m10 + m01) * t
            w = (m20 - m02) * t
        } else {
            t = Math.sqrt(m22 - (m00 + m11) + 1.0).toFloat()
            z = t * 0.5f
            t = 0.5f / t
            x = (m02 + m20) * t
            y = (m21 + m12) * t
            w = (m01 - m10) * t
        }
    }
}