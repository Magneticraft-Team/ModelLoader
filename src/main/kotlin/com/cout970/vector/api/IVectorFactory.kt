package com.cout970.vector.api

import com.cout970.vector.impl.VectorFactory

/**
 * Created by cout970 on 18/08/2016.
 */
interface IVectorFactory {

    companion object {
        var factory: IVectorFactory = VectorFactory
    }

    fun vec2Of(x: Number, y: Number): IVector2
    fun mutableVec2Of(x: Number, y: Number): IMutableVector2

    fun vec3Of(x: Number, y: Number, z: Number): IVector3
    fun mutableVec3Of(x: Number, y: Number, z: Number): IMutableVector3

    fun vec4Of(x: Number, y: Number, z: Number, w: Number): IVector4
    fun mutableVec4Of(x: Number, y: Number, z: Number, w: Number): IMutableVector4

    fun quatOf(x: Number, y: Number, z: Number, w: Number): IQuaternion
    fun mutableQuatOf(x: Number, y: Number, z: Number, w: Number): IMutableQuaternion
}