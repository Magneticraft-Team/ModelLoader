package com.cout970.vector.extensions

/**
 * Created by cout970 on 17/08/2016.
 */
object Vector2 {
    val ORIGIN = vec2Of(0, 0)
    val ONE = vec2Of(1, 1)

    val X_AXIS = vec2Of(1, 0)
    val Y_AXIS = vec2Of(0, 1)
    val NEG_X_AXIS = vec2Of(-1, 0)
    val NEG_Y_AXIS = vec2Of(0, -1)
}

object Vector3 {
    val ORIGIN = vec3Of(0, 0, 0)
    val ONE = vec3Of(1, 1, 1)

    val X_AXIS = vec3Of(1, 0, 0)
    val Y_AXIS = vec3Of(0, 1, 0)
    val Z_AXIS = vec3Of(0, 0, 1)
    val NEG_X_AXIS = vec3Of(-1, 0, 0)
    val NEG_Y_AXIS = vec3Of(0, -1, 0)
    val NEG_Z_AXIS = vec3Of(0, 0, -1)
}

object Vector4 {
    val ORIGIN = vec4Of(0, 0, 0, 0)
    val ONE = vec4Of(1, 1, 1, 1)

    val X_AXIS = vec4Of(1, 0, 0, 0)
    val Y_AXIS = vec4Of(0, 1, 0, 0)
    val Z_AXIS = vec4Of(0, 0, 1, 0)
    val W_AXIS = vec4Of(0, 0, 0, 1)
    val NEG_X_AXIS = vec4Of(-1, 0, 0, 0)
    val NEG_Y_AXIS = vec4Of(0, -1, 0, 0)
    val NEG_Z_AXIS = vec4Of(0, 0, -1, 0)
    val NEG_W_AXIS = vec4Of(0, 0, 0, -1)
}

object Quaternion {
    val IDENTITY = quatOf(0, 0, 0, 1)
}
