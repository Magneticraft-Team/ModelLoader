package com.cout970.modelloader.api.formats.gltf

import com.cout970.modelloader.ModelLoaderMod
import com.cout970.modelloader.api.util.TRSTransformation
import com.cout970.modelloader.internal.resourceLocationOf
import com.cout970.vector.api.IVector2
import com.cout970.vector.api.IVector3
import com.cout970.vector.extensions.*
import net.minecraft.client.renderer.block.model.BakedQuad
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.vertex.VertexFormat
import net.minecraft.client.renderer.vertex.VertexFormatElement
import net.minecraft.util.EnumFacing
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad
import java.util.function.Function
import javax.vecmath.Point3f

class GltfBaker(val format: VertexFormat, val bakedTextureGetter: Function<ResourceLocation, TextureAtlasSprite>) {

    fun bake(model: GltfModel): GltfBakedModel {
        val nodeQuads = mutableListOf<BakedQuad>()

        val scene = model.structure.scenes[model.definition.scene ?: 0]

        scene.nodes.forEach { node ->
            node.children.forEach {
                recursiveBakeNodes(it, node.transform, nodeQuads)
            }
        }

        val particle = resourceLocationOf(model.location, model.definition.images[0].uri!!)
        return GltfBakedModel(mapOf(null to nodeQuads), bakedTextureGetter.apply(particle))
    }

    fun recursiveBakeNodes(node: GltfStructure.Node, transform: TRSTransformation, list: MutableList<BakedQuad>) {
        val globalTransform = node.transform + transform
        node.children.forEach {
            recursiveBakeNodes(it, globalTransform, list)
        }
        val mesh = node.mesh ?: return
        list += bakeMesh(mesh, globalTransform)
    }

    @Suppress("UNCHECKED_CAST")
    fun bakeMesh(mesh: GltfStructure.Mesh, globalTransform: TRSTransformation): List<BakedQuad> {
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

            val pos = posBuffer.data as List<IVector3>
            val tex = texBuffer?.data as? List<IVector2> ?: emptyList()

            val matrix = globalTransform.matrix
            val newPos = pos.map { vec ->
                Point3f(vec.xf, vec.yf, vec.zf)
                        .apply { matrix.transform(this) }
                        .let { vec3Of(it.x, it.y, it.z) }
            }

            if (prim.mode == GltfMode.QUADS) {
                for (i in 0 until pos.size / 4) {
                    quads += makeQuad(i * 4, newPos, tex, bakedTextureGetter.apply(prim.material))
                }
            } else {
                for (i in 0 until pos.size / 3) {
                    quads += makeQuadFromTriangle(i * 3, newPos, tex, bakedTextureGetter.apply(prim.material))
                }
            }
        }

        return quads
    }

    fun makeQuad(index: Int, pos: List<IVector3>, tex: List<IVector2>, sprite: TextureAtlasSprite): BakedQuad {
        val a = pos[index + 0]
        val b = pos[index + 1]
        val c = pos[index + 2]
        val d = pos[index + 3]

        val at = tex.getOrNull(index + 0) ?: vec2Of(0)
        val bt = tex.getOrNull(index + 1) ?: vec2Of(0)
        val ct = tex.getOrNull(index + 2) ?: vec2Of(0)
        val dt = tex.getOrNull(index + 3) ?: vec2Of(0)

        val ac = c - a
        val bd = d - b
        val normal = (ac cross bd).normalize()

        return UnpackedBakedQuad.Builder(format).apply {
            setContractUVs(true)
            setTexture(sprite)
            putVertex(format, normal, a, at, sprite)
            putVertex(format, normal, b, bt, sprite)
            putVertex(format, normal, c, ct, sprite)
            putVertex(format, normal, d, dt, sprite)
            setQuadOrientation(EnumFacing.getFacingFromVector(normal.xf, normal.yf, normal.zf))
        }.build()
    }

    fun makeQuadFromTriangle(index: Int, pos: List<IVector3>, tex: List<IVector2>, sprite: TextureAtlasSprite): BakedQuad {
        val a = pos[index + 0]
        val b = pos[index + 1]
        val c = pos[index + 2]

        val at = tex.getOrNull(index + 0) ?: vec2Of(0)
        val bt = tex.getOrNull(index + 1) ?: vec2Of(0)
        val ct = tex.getOrNull(index + 2) ?: vec2Of(0)

        val ac = c - a
        val ab = b - a
        val normal = -(ac cross ab).normalize()

        return UnpackedBakedQuad.Builder(format).apply {
            setContractUVs(true)
            setTexture(sprite)
            putVertex(format, normal, a, at, sprite)
            putVertex(format, normal, b, bt, sprite)
            putVertex(format, normal, c, ct, sprite)
            putVertex(format, normal, c, ct, sprite)
            setQuadOrientation(EnumFacing.getFacingFromVector(normal.xf, normal.yf, normal.zf))
        }.build()
    }

    private fun UnpackedBakedQuad.Builder.putVertex(format: VertexFormat, side: IVector3, pos: IVector3,
                                                    tex: IVector2, sprite: TextureAtlasSprite) {

        for (e in 0 until format.elementCount) {
            when (format.getElement(e).usage) {
                VertexFormatElement.EnumUsage.POSITION -> put(e, pos.xf, pos.yf, pos.zf, 1f)
                VertexFormatElement.EnumUsage.COLOR -> put(e, 1f, 1f, 1f, 1f)
                VertexFormatElement.EnumUsage.NORMAL -> put(e, side.xf, side.yf, side.zf, 0f)
                VertexFormatElement.EnumUsage.UV -> {
                    if (format.getElement(e).index == 0) {
                        put(e,
                                sprite.getInterpolatedU(tex.xd * 16.0),
                                sprite.getInterpolatedV(tex.yd * 16.0),
                                0f, 1f)
                    }
                }
                else -> put(e)
            }
        }
    }
}