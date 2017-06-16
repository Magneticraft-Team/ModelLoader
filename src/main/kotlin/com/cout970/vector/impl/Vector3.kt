package com.cout970.vector.impl

import com.cout970.vector.api.IMutableVector3

/**
 * Created by cout970 on 17/08/2016.
 */

//data class Vector3i(override val x: Int, override val y: Int, override val z: Int) : IVector3
//
//data class Vector3f(override val x: Float, override val y: Float, override val z: Float) : IVector3
//
//data class Vector3d(override val x: Double, override val y: Double, override val z: Double) : IVector3

//@formatter:off
data class MutableVector3i(
        override var xi: Int,
        override var yi: Int,
        override var zi: Int
) : IMutableVector3 {
    override var x: Number get() = xi; set(i) { xi = i.toInt()}
    override var y: Number get() = yi; set(i) { yi = i.toInt()}
    override var z: Number get() = zi; set(i) { zi = i.toInt()}
}

data class MutableVector3f(
        override var xf: Float,
        override var yf: Float,
        override var zf: Float
) : IMutableVector3 {
    override var x: Number get() = xf; set(i) { xf = i.toFloat()}
    override var y: Number get() = yf; set(i) { yf = i.toFloat()}
    override var z: Number get() = zf; set(i) { zf = i.toFloat()}
}

data class MutableVector3d(
        override var xd: Double,
        override var yd: Double,
        override var zd: Double
) : IMutableVector3 {
    override var x: Number get() = xd; set(i) { xd = i.toDouble()}
    override var y: Number get() = yd; set(i) { yd = i.toDouble()}
    override var z: Number get() = zd; set(i) { zd = i.toDouble()}
}
//@formatter:on