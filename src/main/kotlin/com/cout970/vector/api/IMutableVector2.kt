package com.cout970.vector.api

/**
 * Created by cout970 on 17/08/2016.
 */
interface IMutableVector2 : IVector2 {

    override var x: Number
    override var y: Number

    override var xi: Int get() = x.toInt(); set(i) {
        x = i
    }
    override var yi: Int get() = y.toInt(); set(i) {
        y = i
    }

    override var xf: Float get() = x.toFloat(); set(i) {
        x = i
    }
    override var yf: Float get() = y.toFloat(); set(i) {
        y = i
    }

    override var xd: Double get() = x.toDouble(); set(i) {
        x = i
    }
    override var yd: Double get() = y.toDouble(); set(i) {
        y = i
    }
}