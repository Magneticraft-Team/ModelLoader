package com.cout970.modelloader.animation

import javax.vecmath.Vector2d
import javax.vecmath.Vector3d

interface ICompactModelData {
    val indices: List<Int>?
    val pos: List<Vector3d>
    val tex: List<Vector2d>
    val count: Int
    val triangles: Boolean
}

data class CompactModelData(
    override val indices: List<Int>?,
    override val pos: List<Vector3d>,
    override val tex: List<Vector2d>,
    override val count: Int,
    override val triangles: Boolean
) : ICompactModelData