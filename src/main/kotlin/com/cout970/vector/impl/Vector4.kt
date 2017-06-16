package com.cout970.vector.impl

import com.cout970.vector.api.IMutableVector4
import com.cout970.vector.api.IVector4

/**
 * Created by cout970 on 17/08/2016.
 */
//TODO implement Comparable

data class Vector4i(override val x: Int, override val y: Int, override val z: Int, override val w: Int) : IVector4

data class Vector4f(override val x: Float, override val y: Float, override val z: Float, override val w: Float) : IVector4

data class Vector4d(override val x: Double, override val y: Double, override val z: Double, override val w: Double) : IVector4

//@formatter:off
data class MutableVector4i(
        override var xi: Int,
        override var yi: Int,
        override var zi: Int,
        override var wi: Int
) : IMutableVector4 {
    override var x: Number get() = xi; set(i) { xi = i.toInt()}
    override var y: Number get() = yi; set(i) { yi = i.toInt()}
    override var z: Number get() = zi; set(i) { zi = i.toInt()}
    override var w: Number get() = wi; set(i) { wi = i.toInt()}
}

data class MutableVector4f(
        override var xf: Float,
        override var yf: Float,
        override var zf: Float,
        override var wf: Float
) : IMutableVector4 {
    override var x: Number get() = xf; set(i) { xf = i.toFloat()}
    override var y: Number get() = yf; set(i) { yf = i.toFloat()}
    override var z: Number get() = zf; set(i) { zf = i.toFloat()}
    override var w: Number get() = wf; set(i) { wf = i.toFloat()}
}

data class MutableVector4d(
        override var xd: Double,
        override var yd: Double,
        override var zd: Double,
        override var wd: Double
) : IMutableVector4 {
    override var x: Number get() = xd; set(i) { xd = i.toDouble()}
    override var y: Number get() = yd; set(i) { yd = i.toDouble()}
    override var z: Number get() = zd; set(i) { zd = i.toDouble()}
    override var w: Number get() = wd; set(i) { wd = i.toDouble()}
}
//@formatter:on