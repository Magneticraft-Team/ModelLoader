package com.cout970.vector.extensions

import com.cout970.vector.api.IVector3
import com.cout970.vector.api.x
import com.cout970.vector.api.y
import com.cout970.vector.api.z

/**
 * Created by cout970 on 17/08/2016.
 */


//@formatter:off
//Int

inline val IVector3.xi: Int get() = x.toInt()
inline val IVector3.yi: Int get() = y.toInt()
inline val IVector3.zi: Int get() = z.toInt()

//Float

inline val IVector3.xf: Float get() = x.toFloat()
inline val IVector3.yf: Float get() = y.toFloat()
inline val IVector3.zf: Float get() = z.toFloat()

//Double

inline val IVector3.xd: Double get() = x.toDouble()
inline val IVector3.yd: Double get() = y.toDouble()
inline val IVector3.zd: Double get() = z.toDouble()

//@formatter:on

