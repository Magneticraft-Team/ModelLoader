package com.cout970.modelloader.animation

import com.cout970.modelloader.api.TRSTransformation
import com.cout970.modelloader.api.ModelCache
import com.cout970.modelloader.api.ModelGroupCache
import com.cout970.modelloader.api.TextureModelCache
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import javax.vecmath.Matrix4f
import javax.vecmath.Point3f

object AnimationOptimizer {

    fun optimize(nodes: List<AnimationNodeBuilder>, animationPoints: Set<Int>): List<AnimatedNode> {
        return nodes
            .map { compactNode(it, animationPoints) }
            .map { it.toAnimated() }
    }

    private data class CompactNode(
        val index: Int,
        val transform: TRSTransformation,
        val dynamic: List<CompactNode>,
        val static: List<VertexGroup>
    )

    private fun compactNode(tree: AnimationNodeBuilder, animatedNodes: Set<Int>): CompactNode {
        val list = tree.children.map { childData -> compactNode(childData, animatedNodes) }
        val matrix = tree.transform.toImmutable().matrixVec.apply { transpose() }
        val (animated, nonAnimated) = list.partition { it.index in animatedNodes }

        val childrenVertex = if (tree.id in animatedNodes) {
            nonAnimated.flatMap { node -> node.static }
        } else {
            nonAnimated.flatMap { node ->
                node.static.map { (tex, vertex) ->
                    VertexGroup(tex, vertex.map { matrix.transform(it) })
                }
            }
        }
        val thisVertex = if (tree.id in animatedNodes) {
            tree.vertices.toList()
        } else {
            tree.vertices.map { (tex, list) -> tex to list.map { matrix.transform(it) } }
        }

        val map = mutableMapOf<ResourceLocation, MutableList<Vertex>>()

        // join duplicated pairs (groups of vertex with the same texture)
        childrenVertex.forEach { (tex, vertex) ->
            if (tex in map) {
                map[tex]!!.addAll(vertex)
            } else {
                map[tex] = vertex.toMutableList()
            }
        }

        thisVertex.forEach { (tex, vertex) ->
            if (tex in map) {
                map[tex]!!.addAll(vertex)
            } else {
                map[tex] = vertex.toMutableList()
            }
        }

        val allAnimated = animated + nonAnimated.filter { it.dynamic.isNotEmpty() }.map { it.copy(static = emptyList()) }

        return CompactNode(tree.id, tree.transform.toImmutable(), allAnimated, map.map { VertexGroup(it.key, it.value) })
    }

    private fun CompactNode.toAnimated(): AnimatedNode {
        val caches = static.map { (tex, vertex) ->
            TextureModelCache(tex, ModelCache { renderVertex(vertex) })
        }

        return AnimatedNode(
            index = index,
            transform = transform,
            children = dynamic.map { it.toAnimated() },
            cache = if (caches.size == 1) caches.first() else ModelGroupCache(*caches.toTypedArray())
        )
    }

    private fun renderVertex(vertex: List<Vertex>) {
        val tessellator = Tessellator.getInstance()
        val buffer = tessellator.buffer

        buffer.apply {
            begin(GL11.GL_QUADS, DefaultVertexFormats.OLDMODEL_POSITION_TEX_NORMAL)
            setTranslation(0.0, 0.0, 0.0)

            vertex.forEach {
                pos(it.x.toDouble(), it.y.toDouble(), it.z.toDouble())
                    .tex(it.u.toDouble(), it.v.toDouble())
                    .normal(it.xn, it.yn, it.zn)
                    .endVertex()
            }
            tessellator.draw()
        }
    }

    private fun Matrix4f.transform(vertex: Vertex): Vertex {
        val pos = Point3f(vertex.x, vertex.y, vertex.z)
        transform(pos)
        return vertex.copy(x = pos.x, y = pos.y, z = pos.z)
    }
}