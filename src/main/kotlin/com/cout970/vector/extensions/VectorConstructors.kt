package com.cout970.vector.extensions

import com.cout970.vector.api.*

/**
 * Created by cout970 on 17/08/2016.
 */

// IVector2
fun vec2Of(x: Number, y: Number): IVector2 = IVectorFactory.factory.vec2Of(x, y)
fun mutableVec2Of(x: Number, y: Number): IMutableVector2 = IVectorFactory.factory.mutableVec2Of(x, y)

fun vec2Of(n: Number): IVector2 = IVectorFactory.factory.vec2Of(n, n)
fun mutableVec2Of(n: Number = 0.0): IMutableVector2 = IVectorFactory.factory.mutableVec2Of(n, n)

// IVector3
fun vec3Of(x: Number, y: Number, z: Number): IVector3 = IVectorFactory.factory.vec3Of(x, y, z)
fun mutableVec3Of(x: Number, y: Number, z: Number): IMutableVector3 = IVectorFactory.factory.mutableVec3Of(x, y, z)

fun vec3Of(n: Number): IVector3 = IVectorFactory.factory.vec3Of(n, n, n)
fun mutableVec3Of(n: Number = 0.0): IMutableVector3 = IVectorFactory.factory.mutableVec3Of(n, n, n)

// IVector4
fun vec4Of(x: Number, y: Number, z: Number, w: Number): IVector4 = IVectorFactory.factory.vec4Of(x, y, z, w)
fun mutableVec4Of(x: Number, y: Number, z: Number, w: Number): IMutableVector4 = IVectorFactory.factory.mutableVec4Of(x, y, z, w)

//IQuaternion
fun quatOf(x: Number, y: Number, z: Number, w: Number): IQuaternion = IVectorFactory.factory.quatOf(x, y, z, w)
fun mutableQuatOf(x: Number, y: Number, z: Number, w: Number): IMutableQuaternion = IVectorFactory.factory.mutableQuatOf(x, y, z, w)

fun vec4Of(n: Number): IVector4 = IVectorFactory.factory.vec4Of(n, n, n, n)
fun mutableVec4Of(n: Number = 0.0): IMutableVector4 = IVectorFactory.factory.mutableVec4Of(n, n, n, n)