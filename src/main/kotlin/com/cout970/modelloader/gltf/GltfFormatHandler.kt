@file:Suppress("DEPRECATION")

package com.cout970.modelloader.gltf

import com.cout970.modelloader.*
import net.minecraft.client.renderer.model.BakedQuad
import net.minecraft.client.renderer.model.IUnbakedModel
import net.minecraft.client.renderer.model.ItemCameraTransforms
import net.minecraft.client.renderer.model.ModelRotation
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.vertex.VertexFormat
import net.minecraft.client.renderer.vertex.VertexFormatElement
import net.minecraft.resources.IResourceManager
import net.minecraft.util.Direction
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad
import java.io.InputStream
import java.util.function.Function
import javax.vecmath.Point3f
import javax.vecmath.Vector2d
import javax.vecmath.Vector3d

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

            val pos = posBuffer.data as List<Vector3d>
            val tex = texBuffer?.data as? List<Vector2d> ?: emptyList()

            val matrix = globalTransform.matrixVec.apply { transpose() }
            val newPos = pos.map { vec ->
                Point3f(vec.x.toFloat(), vec.y.toFloat(), vec.z.toFloat())
                    .also { matrix.transform(it) }
                    .run { Vector3d(x.toDouble(), y.toDouble(), z.toDouble()) }
            }

            val sprite = bakedTextureGetter.apply(prim.material ?: ModelLoaderMod.defaultModelTexture)

            if (prim.mode == GltfMode.QUADS) {
                for (i in 0 until pos.size / 4) {
                    quads += makeQuad(i * 4, newPos, tex, sprite)
                }
            } else {
                for (i in 0 until pos.size / 3) {
                    quads += makeQuadFromTriangle(i * 3, newPos, tex, sprite)
                }
            }
        }

        return quads
    }

    fun makeQuad(index: Int, pos: List<Vector3d>, tex: List<Vector2d>, sprite: TextureAtlasSprite): BakedQuad {
        val a = pos[index + 0]
        val b = pos[index + 1]
        val c = pos[index + 2]
        val d = pos[index + 3]

        val at = tex.getOrNull(index + 0) ?: Vector2d()
        val bt = tex.getOrNull(index + 1) ?: Vector2d()
        val ct = tex.getOrNull(index + 2) ?: Vector2d()
        val dt = tex.getOrNull(index + 3) ?: Vector2d()

        val ac = c - a
        val bd = d - b
        val normal = (ac cross bd).norm()

        return UnpackedBakedQuad.Builder(format).apply {
            setContractUVs(true)
            setTexture(sprite)
            putVertex(format, normal, a, at, sprite)
            putVertex(format, normal, b, bt, sprite)
            putVertex(format, normal, c, ct, sprite)
            putVertex(format, normal, d, dt, sprite)
            setQuadOrientation(Direction.getFacingFromVector(normal.x, normal.y, normal.z))
        }.build()
    }

    fun makeQuadFromTriangle(index: Int, pos: List<Vector3d>, tex: List<Vector2d>, sprite: TextureAtlasSprite): BakedQuad {
        val a = pos[index + 0]
        val b = pos[index + 1]
        val c = pos[index + 2]

        val at = tex.getOrNull(index + 0) ?: Vector2d()
        val bt = tex.getOrNull(index + 1) ?: Vector2d()
        val ct = tex.getOrNull(index + 2) ?: Vector2d()

        val ac = c - a
        val ab = b - a
        val rawNorm = -(ac cross ab).norm()
        val normal = if (rawNorm.x.isNaN() || rawNorm.y.isNaN() || rawNorm.z.isNaN()) {
            Vector3d(0.0, 1.0, 0.0)
        } else rawNorm

        return UnpackedBakedQuad.Builder(format).apply {
            setContractUVs(true)
            setTexture(sprite)
            putVertex(format, normal, a, at, sprite)
            putVertex(format, normal, b, bt, sprite)
            putVertex(format, normal, c, ct, sprite)
            putVertex(format, normal, c, ct, sprite)
            setQuadOrientation(Direction.getFacingFromVector(normal.x, normal.y, normal.z))
        }.build()
    }

    private fun UnpackedBakedQuad.Builder.putVertex(format: VertexFormat, side: Vector3d, pos: Vector3d,
                                                    tex: Vector2d, sprite: TextureAtlasSprite) {

        for (e in 0 until format.elementCount) {
            when (format.getElement(e).usage) {
                VertexFormatElement.Usage.POSITION -> put(e, pos.x.toFloat(), pos.y.toFloat(), pos.z.toFloat(), 1f)
                VertexFormatElement.Usage.COLOR -> put(e, 1f, 1f, 1f, 1f)
                VertexFormatElement.Usage.NORMAL -> put(e, side.x.toFloat(), side.y.toFloat(), side.z.toFloat(), 0f)
                VertexFormatElement.Usage.UV -> {
                    if (format.getElement(e).index == 0) {
                        put(e,
                            sprite.getInterpolatedU(tex.x * 16.0),
                            sprite.getInterpolatedV(tex.y * 16.0),
                            0f, 1f)
                    }
                }
                else -> put(e)
            }
        }
    }
}