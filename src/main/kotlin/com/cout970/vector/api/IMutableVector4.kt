package com.cout970.vector.api

/**
 * Created by cout970 on 17/08/2016.
 */
interface IMutableVector4 : IVector4 {

    override var x: Number
    override var y: Number
    override var z: Number
    override var w: Number

    override var xi: Int get() = x.toInt(); set(i) { x = i }
    override var yi: Int get() = y.toInt(); set(i) { y = i }
    override var zi: Int get() = z.toInt(); set(i) { z = i }
    override var wi: Int get() = w.toInt(); set(i) { w = i }

    override var xf: Float get() = x.toFloat(); set(i) { x = i }
    override var yf: Float get() = y.toFloat(); set(i) { y = i }
    override var zf: Float get() = z.toFloat(); set(i) { z = i }
    override var wf: Float get() = w.toFloat(); set(i) { w = i }

    override var xd: Double get() = x.toDouble(); set(i) { x = i }
    override var yd: Double get() = y.toDouble(); set(i) { y = i }
    override var zd: Double get() = z.toDouble(); set(i) { z = i }
    override var wd: Double get() = w.toDouble(); set(i) { w = i }
}