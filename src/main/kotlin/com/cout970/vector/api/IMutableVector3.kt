package com.cout970.vector.api

import java.io.Serializable

/**
 * Created by cout970 on 17/08/2016.
 */
interface IMutableVector3 : Serializable {

    var x: Number
    var y: Number
    var z: Number

    var xi: Int get() = x.toInt(); set(i) {
        x = i
    }
    var yi: Int get() = y.toInt(); set(i) {
        y = i
    }
    var zi: Int get() = z.toInt(); set(i) {
        z = i
    }

    var xf: Float get() = x.toFloat(); set(i) {
        x = i
    }
    var yf: Float get() = y.toFloat(); set(i) {
        y = i
    }
    var zf: Float get() = z.toFloat(); set(i) {
        z = i
    }

    var xd: Double get() = x.toDouble(); set(i) {
        x = i
    }
    var yd: Double get() = y.toDouble(); set(i) {
        y = i
    }
    var zd: Double get() = z.toDouble(); set(i) {
        z = i
    }
}