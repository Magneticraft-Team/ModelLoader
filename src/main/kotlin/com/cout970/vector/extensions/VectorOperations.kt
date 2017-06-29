package com.cout970.vector.extensions

import com.cout970.vector.api.IQuaternion
import com.cout970.vector.api.IVector2
import com.cout970.vector.api.IVector3
import com.cout970.vector.api.IVector4
import com.cout970.vector.impl.*

/**
 * Created by cout970 on 17/08/2016.
 */

//plus '+'

//vector2
operator fun Vector2i.plus(other: Vector2i) = vec2Of(xi + other.xi, yi + other.yi)

operator fun Vector2f.plus(other: Vector2f) = vec2Of(xf + other.xf, yf + other.yf)
operator fun IVector2.plus(other: IVector2) = vec2Of(xd + other.xd, yd + other.yd)

operator fun Vector2i.plus(other: Int) = vec2Of(xi + other, yi + other)
operator fun Vector2f.plus(other: Float) = vec2Of(xf + other, yf + other)
operator fun IVector2.plus(other: Number) = vec2Of(xd + other.toDouble(), yd + other.toDouble())

//vector3
//operator fun Vector3i.plus(other: Vector3i) = vec3Of(xi + other.xi, yi + other.yi, zi + other.zi)
//operator fun Vector3f.plus(other: Vector3f) = vec3Of(xf + other.xf, yf + other.yf, zf + other.zf)
operator fun IVector3.plus(other: IVector3) = vec3Of(xd + other.xd, yd + other.yd, zd + other.zd)

//operator fun Vector3i.plus(other: Int) = vec3Of(xi + other, yi + other, zi + other)
//operator fun Vector3f.plus(other: Float) = vec3Of(xf + other, yf + other, zf + other)
operator fun IVector3.plus(other: Number) = vec3Of(xd + other.toDouble(), yd + other.toDouble(), zd + other.toDouble())

//vector4
operator fun Vector4i.plus(other: Vector4i) = vec4Of(xi + other.xi, yi + other.yi, zi + other.zi, wi + other.wi)

operator fun Vector4f.plus(other: Vector4f) = vec4Of(xf + other.xf, yf + other.yf, zf + other.zf, wf + other.wf)
operator fun IVector4.plus(other: IVector4) = vec4Of(xd + other.xd, yd + other.yd, zd + other.zd, wd + other.wd)

operator fun Vector4i.plus(other: Int) = vec4Of(xi + other, yi + other, zi + other, wi + other)
operator fun Vector4f.plus(other: Float) = vec4Of(xf + other, yf + other, zf + other, wi + other)
operator fun IVector4.plus(other: Number) = vec4Of(xd + other.toDouble(), yd + other.toDouble(), zd + other.toDouble(),
        wd + other.toDouble())

//minus '-'

//vector2
operator fun Vector2i.minus(other: Vector2i) = vec2Of(xi - other.xi, yi - other.yi)

operator fun Vector2f.minus(other: Vector2f) = vec2Of(xf - other.xf, yf - other.yf)
operator fun IVector2.minus(other: IVector2) = vec2Of(xd - other.xd, yd - other.yd)

operator fun Vector2i.minus(other: Int) = vec2Of(xi - other, yi - other)
operator fun Vector2f.minus(other: Float) = vec2Of(xf - other, yf - other)
operator fun IVector2.minus(other: Number) = vec2Of(xd - other.toDouble(), yd - other.toDouble())

//vector3
//operator fun Vector3i.minus(other: Vector3i) = vec3Of(xi - other.xi, yi - other.yi, zi - other.zi)
//operator fun Vector3f.minus(other: Vector3f) = vec3Of(xf - other.xf, yf - other.yf, zf - other.zf)
operator fun IVector3.minus(other: IVector3) = vec3Of(xd - other.xd, yd - other.yd, zd - other.zd)

//
//operator fun Vector3i.minus(other: Int) = vec3Of(xi - other, yi - other, zi - other)
//operator fun Vector3f.minus(other: Float) = vec3Of(xf - other, yf - other, zf - other)
operator fun IVector3.minus(other: Number) = vec3Of(xd - other.toDouble(), yd - other.toDouble(), zd - other.toDouble())

//vector4
operator fun Vector4i.minus(other: Vector4i) = vec4Of(xi - other.xi, yi - other.yi, zi - other.zi, wi - other.wi)

operator fun Vector4f.minus(other: Vector4f) = vec4Of(xf - other.xf, yf - other.yf, zf - other.zf, wf - other.wf)
operator fun IVector4.minus(other: IVector4) = vec4Of(xd - other.xd, yd - other.yd, zd - other.zd, wd - other.wd)

operator fun Vector4i.minus(other: Int) = vec4Of(xi - other, yi - other, zi - other, wi - other)
operator fun Vector4f.minus(other: Float) = vec4Of(xf - other, yf - other, zf - other, wi - other)
operator fun IVector4.minus(other: Number) = vec4Of(xd - other.toDouble(), yd - other.toDouble(), zd - other.toDouble(),
        wd - other.toDouble())

//times '*'

//vector2
operator fun Vector2i.times(other: Vector2i) = vec2Of(xi * other.xi, yi * other.yi)

operator fun Vector2f.times(other: Vector2f) = vec2Of(xf * other.xf, yf * other.yf)
operator fun IVector2.times(other: IVector2) = vec2Of(xd * other.xd, yd * other.yd)

operator fun Vector2i.times(other: Int) = vec2Of(xi * other, yi * other)
operator fun Vector2f.times(other: Float) = vec2Of(xf * other, yf * other)
operator fun IVector2.times(other: Number) = vec2Of(xd * other.toDouble(), yd * other.toDouble())

//vector3
//operator fun Vector3i.times(other: Vector3i) = vec3Of(xi * other.xi, yi * other.yi, zi * other.zi)
//operator fun Vector3f.times(other: Vector3f) = vec3Of(xf * other.xf, yf * other.yf, zf * other.zf)
operator fun IVector3.times(other: IVector3) = vec3Of(xd * other.xd, yd * other.yd, zd * other.zd)

//
//operator fun Vector3i.times(other: Int) = vec3Of(xi * other, yi * other, zi * other)
//operator fun Vector3f.times(other: Float) = vec3Of(xf * other, yf * other, zf * other)
operator fun IVector3.times(other: Number) = vec3Of(xd * other.toDouble(), yd * other.toDouble(), zd * other.toDouble())

//vector4
operator fun Vector4i.times(other: Vector4i) = vec4Of(xi * other.xi, yi * other.yi, zi * other.zi, wi * other.wi)

operator fun Vector4f.times(other: Vector4f) = vec4Of(xf * other.xf, yf * other.yf, zf * other.zf, wf * other.wf)
operator fun IVector4.times(other: IVector4) = vec4Of(xd * other.xd, yd * other.yd, zd * other.zd, wd * other.wd)

operator fun Vector4i.times(other: Int) = vec4Of(xi * other, yi * other, zi * other, wi * other)
operator fun Vector4f.times(other: Float) = vec4Of(xf * other, yf * other, zf * other, wi * other)
operator fun IVector4.times(other: Number) = vec4Of(xd * other.toDouble(), yd * other.toDouble(), zd * other.toDouble(),
        wd * other.toDouble())

//quaternions
operator fun Quaternioni.times(b: Quaternioni) = quatOf(
        w * b.x + x * b.w + y * b.z - z * b.y,
        w * b.y - x * b.z + y * b.w + z * b.x,
        w * b.z + x * b.y - y * b.x + z * b.w,
        w * b.w - x * b.x - y * b.y - z * b.z)

operator fun Quaternionf.times(b: Quaternionf) = quatOf(
        w * b.x + x * b.w + y * b.z - z * b.y,
        w * b.y - x * b.z + y * b.w + z * b.x,
        w * b.z + x * b.y - y * b.x + z * b.w,
        w * b.w - x * b.x - y * b.y - z * b.z)

operator fun IQuaternion.times(b: IQuaternion) = quatOf(
        wd * b.xd + xd * b.wd + yd * b.zd - zd * b.yd,
        wd * b.yd - xd * b.zd + yd * b.wd + zd * b.xd,
        wd * b.zd + xd * b.yd - yd * b.xd + zd * b.wd,
        wd * b.wd - xd * b.xd - yd * b.yd - zd * b.zd)

//div '/'

//vector2
operator fun Vector2i.div(other: Vector2i) = vec2Of(xi / other.xi, yi / other.yi)

operator fun Vector2f.div(other: Vector2f) = vec2Of(xf / other.xf, yf / other.yf)
operator fun IVector2.div(other: IVector2) = vec2Of(xd / other.xd, yd / other.yd)

operator fun Vector2i.div(other: Int) = vec2Of(xi / other, yi / other)
operator fun Vector2f.div(other: Float) = vec2Of(xf / other, yf / other)
operator fun IVector2.div(other: Number) = vec2Of(xd / other.toDouble(), yd / other.toDouble())

//vector3
//operator fun Vector3i.div(other: Vector3i) = vec3Of(xi / other.xi, yi / other.yi, zi / other.zi)
//operator fun Vector3f.div(other: Vector3f) = vec3Of(xf / other.xf, yf / other.yf, zf / other.zf)
operator fun IVector3.div(other: IVector3) = vec3Of(xd / other.xd, yd / other.yd, zd / other.zd)

//
//operator fun Vector3i.div(other: Int) = vec3Of(xi / other, yi / other, zi / other)
//operator fun Vector3f.div(other: Float) = vec3Of(xf / other, yf / other, zf / other)
operator fun IVector3.div(other: Number) = vec3Of(xd / other.toDouble(), yd / other.toDouble(), zd / other.toDouble())

//vector4
operator fun Vector4i.div(other: Vector4i) = vec4Of(xi / other.xi, yi / other.yi, zi / other.zi, wi / other.wi)

operator fun Vector4f.div(other: Vector4f) = vec4Of(xf / other.xf, yf / other.yf, zf / other.zf, wf / other.wf)
operator fun IVector4.div(other: IVector4) = vec4Of(xd / other.xd, yd / other.yd, zd / other.zd, wd / other.wd)

operator fun Vector4i.div(other: Int) = vec4Of(xi / other, yi / other, zi / other, wi / other)
operator fun Vector4f.div(other: Float) = vec4Of(xf / other, yf / other, zf / other, wi / other)
operator fun IVector4.div(other: Number) = vec4Of(xd / other.toDouble(), yd / other.toDouble(), zd / other.toDouble(),
        wd / other.toDouble())