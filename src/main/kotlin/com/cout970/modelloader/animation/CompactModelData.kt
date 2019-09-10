package com.cout970.modelloader.animation

import javax.vecmath.Vector2d
import javax.vecmath.Vector3d

interface CompactModelData {
    val indices: List<Int>?
    val pos: List<Vector3d>
    val tex: List<Vector2d>
    val count: Int
    val triangles: Boolean
}