package com.cout970.vector.impl

import com.cout970.vector.api.*
import net.minecraft.util.math.Vec3d
import javax.vecmath.Quat4f

/**
 * Created by cout970 on 18/08/2016.
 */
object VectorFactory : IVectorFactory {

    override fun vec2Of(x: Number, y: Number): IVector2 = Vector2d(x.toDouble(), y.toDouble())

    override fun mutableVec2Of(x: Number, y: Number): IMutableVector2 = MutableVector2d(x.toDouble(), y.toDouble())


    override fun vec3Of(x: Number, y: Number, z: Number): IVector3 = Vec3d(x.toDouble(), y.toDouble(), z.toDouble())

    override fun mutableVec3Of(x: Number, y: Number, z: Number): IMutableVector3 = MutableVector3d(x.toDouble(),
            y.toDouble(), z.toDouble())


    override fun vec4Of(x: Number, y: Number, z: Number, w: Number): IVector4 = Vector4d(x.toDouble(), y.toDouble(),
            z.toDouble(), w.toDouble())

    override fun mutableVec4Of(x: Number, y: Number, z: Number, w: Number): IMutableVector4 = MutableVector4d(
            x.toDouble(), y.toDouble(), z.toDouble(), w.toDouble())

    override fun quatOf(x: Number, y: Number, z: Number, w: Number): IQuaternion = Quat4f(x.toFloat(),
            y.toFloat(), z.toFloat(), w.toFloat())

    override fun mutableQuatOf(x: Number, y: Number, z: Number, w: Number): IMutableQuaternion = MutableQuaterniond(
            x.toDouble(), y.toDouble(), z.toDouble(), w.toDouble())
}