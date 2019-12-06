@file:Suppress("DEPRECATION")

package com.cout970.modelloader.mcx

import com.cout970.modelloader.*
import com.cout970.modelloader.animation.VertexUtilities
import com.cout970.modelloader.api.EmptyRenderCache
import com.cout970.modelloader.api.ModelCache
import com.cout970.modelloader.api.Utilities
import com.cout970.modelloader.mutable.MutableModel
import com.cout970.modelloader.mutable.MutableModelNode
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import net.minecraft.client.renderer.model.BakedQuad
import net.minecraft.client.renderer.model.IUnbakedModel
import net.minecraft.client.renderer.model.ItemCameraTransforms
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.vertex.VertexFormat
import net.minecraft.client.renderer.vertex.VertexFormatElement
import net.minecraft.resources.IResourceManager
import net.minecraft.util.Direction
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad
import java.io.InputStream
import java.lang.reflect.Type
import java.util.function.Function
import javax.vecmath.Vector2d
import javax.vecmath.Vector3d

object McxFormatHandler : IFormatHandler {
    private val gson = GsonBuilder()
        .registerTypeAdapter(Mesh.Indices::class.java, QuadIndicesDeserializer())
        .registerTypeAdapter(Vector3d::class.java, Vector3Deserializer())
        .registerTypeAdapter(Vector2d::class.java, Vector2Deserializer())
        .registerTypeAdapter(ResourceLocation::class.java, ResourceLocationDeserializer())
        .create()!!

    override fun loadModel(resourceManager: IResourceManager, modelLocation: ResourceLocation): IUnbakedModel {
        return try {
            load(resourceManager.getResource(modelLocation).inputStream)
        } catch (e: Exception) {
            NullUnbakedModel
        }
    }

    private fun load(stream: InputStream): UnbakedMcxModel {
        return gson.fromJson(stream.reader(), UnbakedMcxModel::class.java)
    }
}

class Mesh(val pos: List<Vector3d>, val tex: List<Vector2d>, val indices: List<Indices>) {

    class Indices(val a: Int, val b: Int, val c: Int, val d: Int,
                  val at: Int, val bt: Int, val ct: Int, val dt: Int)

}

internal class McxBaker(val format: VertexFormat, val textureGetter: Function<ResourceLocation, TextureAtlasSprite>) {

    fun bake(model: UnbakedMcxModel): BakedMcxModel {
        val particles = textureGetter.apply(model.particleTexture)
        val quads = model.parts
            .groupBy { it.side }
            .mapValues { bakeMesh(model.quads, it.value) }

        return BakedMcxModel(
            particles = particles,
            itemTransform = ItemCameraTransforms.DEFAULT,
            useAmbientOcclusion = model.useAmbientOcclusion,
            use3dInGui = model.use3dInGui,
            bakedQuads = quads
        )
    }

    fun bakeMutableModel(model: UnbakedMcxModel): MutableModel {
        val parts = mutableMapOf<String, MutableModelNode>()

        model.parts.forEach { part ->
            var count = 0
            var partName = part.name

            // We make sure that parts with the same name are included in the map by autogenerating new names
            while (partName in parts) {
                partName = part.name + (++count).toString()
            }

            parts[partName] = bakeMutableNode(model, part)
        }

        return MutableModel(MutableModelNode(EmptyRenderCache, parts))
    }

    fun bakeMutableNode(model: UnbakedMcxModel, part: UnbakedMcxModel.Part): MutableModelNode {
        val quads = bakeMesh(model.quads, listOf(part))
        val content = if (quads.isNotEmpty()) ModelCache { Utilities.renderQuadsSlow(quads) } else EmptyRenderCache

        return MutableModelNode(
            content = content,
            texture = part.texture
        )
    }

    fun bakeMesh(mesh: Mesh, parts: List<UnbakedMcxModel.Part>): List<BakedQuad> {
        val bakedQuads = mutableListOf<BakedQuad>()

        for (part in parts) {
            val sprite = textureGetter.apply(part.texture)

            for (i in mesh.indices.subList(part.from, part.to)) {
                val pos = listOf(mesh.pos[i.a], mesh.pos[i.b], mesh.pos[i.c], mesh.pos[i.d])
                val tex = listOf(mesh.tex[i.at], mesh.tex[i.bt], mesh.tex[i.ct], mesh.tex[i.dt])
                val normal = getNormal(pos)

                val builder = UnpackedBakedQuad.Builder(format)
                builder.setQuadOrientation(Direction.getFacingFromVector(normal.x, normal.y, normal.z))
                builder.setContractUVs(true)
                builder.setTexture(sprite)
                for (index in 0..3) {
                    putVertex(builder, format, normal, pos[index], tex[index], sprite)
                }
                bakedQuads.add(VertexUtilities.compact(builder.build()))
            }
        }
        return bakedQuads
    }

    private fun getNormal(vertex: List<Vector3d>): Vector3d {
        val ac = Vector3d(vertex[2].x - vertex[0].x, vertex[2].y - vertex[0].y, vertex[2].z - vertex[0].z)
        val bd = Vector3d(vertex[3].x - vertex[1].x, vertex[3].y - vertex[1].y, vertex[3].z - vertex[1].z)
        return (ac cross bd).norm()
    }

    private fun putVertex(builder: UnpackedBakedQuad.Builder, format: VertexFormat, side: Vector3d,
                          pos: Vector3d, tex: Vector2d, sprite: TextureAtlasSprite) {

        for (e in 0 until format.elementCount) {
            when (format.getElement(e).usage) {
                VertexFormatElement.Usage.POSITION -> builder.put(e, pos.x.toFloat(), pos.y.toFloat(), pos.z.toFloat(), 1f)
                VertexFormatElement.Usage.COLOR -> builder.put(e, 1f, 1f, 1f, 1f)
                VertexFormatElement.Usage.NORMAL -> builder.put(e, side.x.toFloat(), side.y.toFloat(), side.z.toFloat(), 0f)
                VertexFormatElement.Usage.UV -> {
                    if (format.getElement(e).index == 0) {
                        builder.put(e,
                            sprite.getInterpolatedU(tex.x * 16.0),
                            sprite.getInterpolatedV(tex.y * 16.0),
                            0f, 1f
                        )
                    }
                }
                else -> builder.put(e)
            }
        }
    }
}

private class ResourceLocationDeserializer : JsonDeserializer<ResourceLocation> {
    override fun deserialize(json: JsonElement, typeOfT: Type?,
                             context: JsonDeserializationContext?): ResourceLocation {
        return ResourceLocation(json.asString)
    }
}

private class QuadIndicesDeserializer : JsonDeserializer<Mesh.Indices> {
    override fun deserialize(json: JsonElement, typeOfT: Type?,
                             context: JsonDeserializationContext?): Mesh.Indices {
        val pos = json.asJsonArray[0].asJsonArray
        val tex = json.asJsonArray[1].asJsonArray
        return Mesh.Indices(pos[0].asInt, pos[1].asInt, pos[2].asInt, pos[3].asInt,
            tex[0].asInt, tex[1].asInt, tex[2].asInt, tex[3].asInt)
    }
}
