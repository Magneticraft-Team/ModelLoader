package com.cout970.modelloader.api.formats.gltf

import com.cout970.modelloader.ModelLoaderMod
import com.cout970.modelloader.api.ModelCache
import com.cout970.modelloader.api.ModelGroupCache
import com.cout970.modelloader.api.TextureModelCache
import com.cout970.modelloader.api.animation.AnimatedModel
import com.cout970.modelloader.api.util.TRSTransformation
import com.cout970.vector.api.IVector2
import com.cout970.vector.api.IVector3
import com.cout970.vector.extensions.*
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.ModelLoader
import org.lwjgl.opengl.GL11
import java.util.function.Function
import javax.vecmath.Point3f

private typealias VertexGroup = Pair<ResourceLocation, List<GltfAnimationBuilder.Vertex>>

class GltfAnimationBuilder {

    /**
     * Option for custom texture getter, override to provide different textures
     */
    var bakedTextureGetter: Function<ResourceLocation, TextureAtlasSprite> = ModelLoader.defaultTextureGetter()

    /**
     * If true, the model will adapt the UV coordinates from 0..1 to the sprite UVs in the block texture map
     * This requires the model to be backed, otherwise the texture will not be registered in the block texture map
     */
    var useTextureAtlas = false

    /**
     * Set of all node indices to ignore, if the node is a group, their children will be excluded too
     */
    var excludedNodes: Set<Int> = emptySet()

    /**
     * Mapping function that can change the textures of the final model
     */
    var transformTexture: ((ResourceLocation) -> ResourceLocation)? = null

    /**
     * Convert all the animations on the model to AnimatedModel
     */
    fun build(model: GltfModel): List<Pair<String, AnimatedModel>> {

        val scene = model.structure.scenes[model.definition.scene ?: 0]
        val nodes = scene.nodes.mapNotNull { node ->
            if (node.index in excludedNodes) return@mapNotNull null
            processNode(node)
        }

        return model.structure.animations.mapIndexed { index, animation ->
            val animationModes = compactNodes(nodes, animation).map { bakeNode(it) }
            val channels = modelChannel(animation.channels)

            val name = animation.name ?: index.toString()

            name to AnimatedModel(animationModes, channels)
        }
    }

    /**
     * Create and empty animation that renders the model without moving parts
     */
    fun buildPlain(model: GltfModel): AnimatedModel {

        val scene = model.structure.scenes[model.definition.scene ?: 0]
        val nodes = scene.nodes.map { node ->
            processNode(node)
        }

        val rootNodes = nodes.map { compactNode(it, emptySet()) }.map { bakeNode(it) }

        return AnimatedModel(rootNodes, emptyList())
    }

    private fun modelChannel(gltfChannels: List<GltfStructure.Channel>): List<AnimatedModel.Channel> {
        return gltfChannels.filter { it.path != GltfChannelPath.weights }.map {
            AnimatedModel.Channel(
                index = it.node,
                type = it.path.toChannelType(),
                keyframes = it.times.zip(it.values)
            )
        }
    }

    private fun bakeNode(node: AnimatedNode): AnimatedModel.Node {
        val caches = node.static.map { (tex, vertex) ->
            TextureModelCache(tex, ModelCache { renderVertex(vertex) })
        }
        return AnimatedModel.Node(
            index = node.index,
            transform = node.transform,
            children = node.dynamic.map { bakeNode(it) },
            cache = ModelGroupCache(*caches.toTypedArray())
        )
    }

    private fun compactNodes(nodes: List<NodeTree>, animation: GltfStructure.Animation): List<AnimatedNode> {
        val animatedNodes = animation.channels.map { it.node }.toSet()
        return nodes.map { compactNode(it, animatedNodes) }
    }

    private fun compactNode(tree: NodeTree, animatedNodes: Set<Int>): AnimatedNode {
        val list = tree.children.map { childData -> compactNode(childData, animatedNodes) }
        val matrix = tree.transform.matrix.apply { transpose() }
        val (animated, nonAnimated) = list.partition { it.index in animatedNodes }

        val childrenVertex = if (tree.index in animatedNodes) {
            nonAnimated.flatMap { node -> node.static }
        } else {
            nonAnimated.flatMap { node ->
                node.static.map { (tex, vertex) -> tex to vertex.map { matrix.transform(it) } }
            }
        }
        val thisVertex = if (tree.index in animatedNodes) {
            tree.vertex
        } else {
            tree.vertex.map { (tex, list) -> tex to list.map { matrix.transform(it) } }
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

        return AnimatedNode(tree.index, tree.transform, allAnimated, map.toList())
    }

    private fun processNode(node: GltfStructure.Node): NodeTree {
        val children = node.children.mapNotNull {
            if (it.index in excludedNodes) return@mapNotNull null
            processNode(it)
        }
        val mesh = node.mesh
        val vertex = mesh?.let { collectVertex(it) } ?: emptyList()

        return NodeTree(node.index, vertex, node.transform, children)
    }

    @Suppress("UNCHECKED_CAST")
    private fun collectVertex(mesh: GltfStructure.Mesh): List<VertexGroup> {
        val groups = mutableMapOf<ResourceLocation, MutableList<Vertex>>()

        mesh.primitives.forEach { prim ->
            if (prim.mode != GltfMode.TRIANGLES && prim.mode != GltfMode.QUADS) {
                ModelLoaderMod.logger.warn("Found primitive with unsupported mode: ${prim.mode}, ignoring")
                return@forEach
            }

            if (prim.indices != null) {
                ModelLoaderMod.logger.warn("Found primitive with indices, this is not supported yet, ignoring")
                return@forEach
            }

            val posBuffer = prim.attributes[GltfAttribute.POSITION]
            val texBuffer = prim.attributes[GltfAttribute.TEXCOORD_0]

            if (posBuffer == null) {
                ModelLoaderMod.logger.warn("Found primitive without vertex, ignoring")
                return@forEach
            }

            if (posBuffer.type != GltfType.VEC3) {
                ModelLoaderMod.logger.warn("Found primitive with in valid vertex pos type: ${posBuffer.type}, ignoring")
                return@forEach
            }

            if (texBuffer != null && texBuffer.type != GltfType.VEC2) {
                ModelLoaderMod.logger.warn("Found primitive with in valid vertex uv type: ${texBuffer.type}, ignoring")
                return@forEach
            }

            val pos = posBuffer.data as List<IVector3>
            val tex = texBuffer?.data as? List<IVector2> ?: emptyList()
            val texLocation = transformTexture?.invoke(prim.material) ?: prim.material
            val sprite = bakedTextureGetter.apply(texLocation)

            val vertex = mutableListOf<Vertex>()

            if (prim.mode == GltfMode.QUADS) {
                for (i in 0 until pos.size / 4) {
                    collectQuad(i * 4, pos, tex, sprite, vertex)
                }
            } else {
                for (i in 0 until pos.size / 3) {
                    collectQuadFromTriangle(i * 3, pos, tex, sprite, vertex)
                }
            }

            val texture = if (useTextureAtlas) TextureMap.LOCATION_BLOCKS_TEXTURE else texLocation

            if (texture in groups) {
                groups[texture]!!.addAll(vertex)
            } else {
                groups[texture] = vertex.toMutableList()
            }
        }

        return groups.toList()
    }

    private fun collectQuad(index: Int, pos: List<IVector3>, tex: List<IVector2>,
                            sprite: TextureAtlasSprite, result: MutableList<Vertex>) {

        val a = pos[index + 0]
        val b = pos[index + 1]
        val c = pos[index + 2]
        val d = pos[index + 3]

        val at = tex.getOrNull(index + 0).applySprite(sprite)
        val bt = tex.getOrNull(index + 1).applySprite(sprite)
        val ct = tex.getOrNull(index + 2).applySprite(sprite)
        val dt = tex.getOrNull(index + 3).applySprite(sprite)

        val ac = c - a
        val bd = d - b
        val normal = (ac cross bd).normalize()

        result += vertexOf(a, at, normal)
        result += vertexOf(b, bt, normal)
        result += vertexOf(c, ct, normal)
        result += vertexOf(d, dt, normal)
    }

    private fun collectQuadFromTriangle(index: Int, pos: List<IVector3>, tex: List<IVector2>,
                                        sprite: TextureAtlasSprite, result: MutableList<Vertex>) {

        val a = pos[index + 0]
        val b = pos[index + 1]
        val c = pos[index + 2]

        val at = tex.getOrNull(index + 0).applySprite(sprite)
        val bt = tex.getOrNull(index + 1).applySprite(sprite)
        val ct = tex.getOrNull(index + 2).applySprite(sprite)

        val ac = c - a
        val ab = b - a
        val normal = -(ac cross ab).normalize()

        result += vertexOf(a, at, normal)
        result += vertexOf(b, bt, normal)
        result += vertexOf(c, ct, normal)
        result += vertexOf(c, ct, normal)
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

    private fun GltfChannelPath.toChannelType() = when (this) {
        GltfChannelPath.translation -> AnimatedModel.ChannelType.TRANSLATION
        GltfChannelPath.rotation -> AnimatedModel.ChannelType.ROTATION
        GltfChannelPath.scale -> AnimatedModel.ChannelType.SCALE
        GltfChannelPath.weights -> error("Unsupported")
    }

    private fun vertexOf(pos: IVector3, uv: IVector2, normal: IVector3) = Vertex(
        pos.xf, pos.yf, pos.zf, uv.xf, uv.yf, normal.xf, normal.yf, normal.zf
    )

    private fun IMatrix4.transform(vertex: Vertex): Vertex {
        val pos = Point3f(vertex.x, vertex.y, vertex.z)
        transform(pos)
        return vertex.copy(x = pos.x, y = pos.y, z = pos.z)
    }

    fun IVector2?.applySprite(sprite: TextureAtlasSprite): IVector2 {
        this ?: return vec2Of(0)

        if (useTextureAtlas) {
            return vec2Of(sprite.getInterpolatedU(xd * 16.0), sprite.getInterpolatedV(yd * 16.0))
        }

        return this
    }

    data class Vertex(
        val x: Float, val y: Float, val z: Float,
        val u: Float, val v: Float,
        val xn: Float, val yn: Float, val zn: Float
    )

    data class NodeTree(
        val index: Int,
        val vertex: List<VertexGroup>,
        val transform: TRSTransformation,
        val children: List<NodeTree>
    )

    data class AnimatedNode(
        val index: Int,
        val transform: TRSTransformation,
        val dynamic: List<AnimatedNode>,
        val static: List<VertexGroup>
    )
}