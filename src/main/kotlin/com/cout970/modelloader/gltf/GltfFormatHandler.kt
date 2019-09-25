@file:Suppress("DEPRECATION")

package com.cout970.modelloader.gltf

import com.cout970.modelloader.*
import com.cout970.modelloader.animation.*
import net.minecraft.client.renderer.model.BakedQuad
import net.minecraft.client.renderer.model.IUnbakedModel
import net.minecraft.client.renderer.model.ItemCameraTransforms
import net.minecraft.client.renderer.model.ModelRotation
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.vertex.VertexFormat
import net.minecraft.resources.IResourceManager
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.ModelLoader
import java.io.InputStream
import java.util.function.Function
import javax.vecmath.*

object GltfFormatHandler : IFormatHandler {

    override fun loadModel(resourceManager: IResourceManager, modelLocation: ResourceLocation): IUnbakedModel {
        val fileStream = resourceManager.getResource(modelLocation).inputStream
        val file = GltfDefinition.parse(fileStream)

        fun retrieveFile(path: String): InputStream {
            val basePath = modelLocation.path.substringBeforeLast('/', "")
            val loc = ResourceLocation(modelLocation.namespace, if (basePath.isEmpty()) path else "$basePath/$path")

            return resourceManager.getResource(loc).inputStream
        }

        val tree = GltfTree.parse(file, modelLocation, ::retrieveFile)

        return UnbakedGltfModel(tree)
    }
}

internal class GltfAnimator(
    val tree: GltfTree.DefinitionTree,
    val getSprite: Function<ResourceLocation, TextureAtlasSprite>? = ModelLoader.defaultTextureGetter()
) {

    val nodeMap = mutableMapOf<Int, GltfTree.Node>()

    fun animate(animation: GltfTree.Animation) = AnimationBuilder().apply {
        animation.channels.forEach { channel ->
            val scene = tree.scenes[tree.scene]
            scene.nodes.forEach {
                createNode(it.index) { newNode(it, TRSTransformation()) }
            }

            when (channel.path) {
                GltfChannelPath.translation -> {
                    val base = nodeMap[channel.node]?.transform?.translation ?: Vector3d()
                    addTranslationChannel(channel.node, channel.times.zip(channel.values).map {
                        AnimationKeyframe(it.first, (it.second as Vector3d) - base)
                    })
                }
                GltfChannelPath.rotation -> {
                    val baseRot = nodeMap[channel.node]?.transform?.rotation ?: Quat4d(0.0, 0.0, 0.0, 1.0)
                    if (baseRot.w == 0.0) baseRot.w = 1.0
                    baseRot.inverse()
                    addRotationChannel(channel.node, channel.times.zip(channel.values).map {
                        val a = it.second as Vector4d
                        val b = Quat4d(a.x, a.y, a.z, a.w)
                        b.mulInverse(baseRot)
                        AnimationKeyframe(it.first, Vector4d(b.x, b.y, b.z, b.w))
                    })
                }
                GltfChannelPath.scale -> {
                    val base = nodeMap[channel.node]?.transform?.scale ?: Vector3d(1.0, 1.0, 1.0)
                    addScaleChannel(channel.node, channel.times.zip(channel.values).map {
                        AnimationKeyframe(it.first, (it.second as Vector3d) / base)
                    })
                }
                GltfChannelPath.weights -> error("Unsupported")
            }
        }
    }.build()

    fun AnimationNodeBuilder.newNode(node: GltfTree.Node, transform: TRSTransformation) {
        nodeMap[node.index] = node
        node.children.forEach {
            createChildren(it.index) { newNode(it, TRSTransformation()) }
        }
        if (node.children.isNotEmpty()) {
            withTransform(node.transform)
        }

        val mesh = node.mesh ?: return
        withVertices(mesh.primitives.mapNotNull {
            val material = it.material ?: ModelLoaderMod.defaultModelTexture
            val sprite: TextureAtlasSprite? = ModelLoader.defaultTextureGetter().apply(material)

            it.toVertexGroup(node.transform, sprite)
        })
    }
}

internal class GltfBaker(
    val format: VertexFormat,
    val bakedTextureGetter: Function<ResourceLocation, TextureAtlasSprite>,
    val rotation: ModelRotation
) {

    fun bake(model: UnbakedGltfModel): BakedGltfModel {
        val nodeQuads = mutableListOf<BakedQuad>()
        val scene = model.tree.scenes[model.tree.scene]
        val trs = TRSTransformation(Vector3d(-0.5, -0.5, -0.5)) +
            rotation.matrixVec.toTRS() +
            TRSTransformation(Vector3d(0.5, 0.5, 0.5))

        scene.nodes.forEach { node ->
            recursiveBakeNodes(node, trs, nodeQuads)
        }

        val particleLocation = model.tree.textures.firstOrNull() ?: ModelLoaderMod.defaultParticleTexture
        val particle = bakedTextureGetter.apply(particleLocation)

        return BakedGltfModel(nodeQuads, particle, ItemCameraTransforms.DEFAULT)
    }

    fun recursiveBakeNodes(node: GltfTree.Node, transform: TRSTransformation, list: MutableList<BakedQuad>) {
        val globalTransform = node.transform + transform
        node.children.forEach {
            recursiveBakeNodes(it, globalTransform, list)
        }
        val mesh = node.mesh ?: return
        list += bakeMesh(mesh, globalTransform)
    }

    @Suppress("UNCHECKED_CAST")
    fun bakeMesh(mesh: GltfTree.Mesh, globalTransform: TRSTransformation): List<BakedQuad> {
        val quads = mutableListOf<BakedQuad>()

        mesh.primitives.forEach { prim ->
            val sprite = bakedTextureGetter.apply(prim.material ?: ModelLoaderMod.defaultModelTexture)
            val group = prim.toVertexGroup(globalTransform, null) ?: return@forEach

            quads += VertexUtilities.bakedVertices(format, sprite, group.vertex)
        }

        return quads
    }
}

private fun GltfTree.Primitive.toVertexGroup(globalTransform: TRSTransformation, sprite: TextureAtlasSprite?): VertexGroup? {
    if (mode != GltfMode.TRIANGLES && mode != GltfMode.QUADS) {
        ModelLoaderMod.logger.warn("Found primitive with unsupported mode: ${mode}, ignoring")
        return null
    }

    if (indices != null) {
        ModelLoaderMod.logger.warn("Found primitive with indices, this is not supported yet, ignoring")
        return null
    }

    val posBuffer = attributes[GltfAttribute.POSITION]
    val texBuffer = attributes[GltfAttribute.TEXCOORD_0]

    if (posBuffer == null) {
        ModelLoaderMod.logger.warn("Found primitive without vertex, ignoring")
        return null
    }

    if (posBuffer.type != GltfType.VEC3) {
        ModelLoaderMod.logger.warn("Found primitive with in valid vertex pos type: ${posBuffer.type}, ignoring")
        return null
    }

    if (texBuffer != null && texBuffer.type != GltfType.VEC2) {
        ModelLoaderMod.logger.warn("Found primitive with in valid vertex uv type: ${texBuffer.type}, ignoring")
        return null
    }

    @Suppress("UNCHECKED_CAST")
    val pos = posBuffer.data as List<Vector3d>
    @Suppress("UNCHECKED_CAST")
    val tex = texBuffer?.data as? List<Vector2d> ?: emptyList()

    val matrix = globalTransform.matrixVec.apply { transpose() }
    val newPos = pos.map { vec ->
        Point3f(vec.x.toFloat(), vec.y.toFloat(), vec.z.toFloat())
            .also { matrix.transform(it) }
            .run { Vector3d(x.toDouble(), y.toDouble(), z.toDouble()) }
    }

    val texture = material ?: ModelLoaderMod.defaultModelTexture
    val vertex = mutableListOf<Vertex>()

    VertexUtilities.collect(
        CompactModelData(indices, newPos, tex, newPos.size, mode != GltfMode.QUADS), sprite, vertex
    )
    return VertexGroup(texture, vertex)
}