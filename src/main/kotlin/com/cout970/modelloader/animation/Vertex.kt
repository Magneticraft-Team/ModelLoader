package com.cout970.modelloader.animation

import net.minecraft.client.renderer.model.BakedQuad
import net.minecraft.util.ResourceLocation

/**
 * A single vertex with position, tex coord and normal vector.
 */
data class Vertex(
    val x: Float, val y: Float, val z: Float,
    val u: Float, val v: Float,
    val xn: Float, val yn: Float, val zn: Float
)

/**
 * Group of baked quads with the same texture
 */
data class QuadGroup(
    val texture: ResourceLocation,
    val quads: List<BakedQuad>
)

/**
 * Group of vertices with the same texture
 */
data class VertexGroup(
    val texture: ResourceLocation,
    val vertex: List<Vertex>
)
