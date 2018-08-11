package com.cout970.modelloader.api.formats.mcx

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

class McxBaker(val format: VertexFormat, val textureGetter: Function<ResourceLocation, TextureAtlasSprite>) {

    fun bake(model: McxModel): BakedMcxModel {
        val quads = model.quads.bake(model.parts)
        val particles = textureGetter.apply(model.particleTexture)
        return BakedMcxModel(model, particles, quads)
    }

    fun Mesh.bake(parts: List<McxModel.Part>): List<BakedQuad> {

        val bakedQuads = mutableListOf<BakedQuad>()

        for (part in parts) {
            val sprite = textureGetter.apply(part.texture)

            for (i in indices.subList(part.from, part.to)) {
                val pos = listOf(pos[i.a], pos[i.b], pos[i.c], pos[i.d])
                val tex = listOf(tex[i.at], tex[i.bt], tex[i.ct], tex[i.dt])
                val normal = getNormal(pos)

                val builder = UnpackedBakedQuad.Builder(format)
                builder.setQuadOrientation(EnumFacing.getFacingFromVector(normal.xf, normal.yf, normal.zf))
                builder.setContractUVs(true)
                builder.setTexture(sprite)
                for (index in 0..3) {
                    putVertex(builder, format, normal, pos[index], tex[index], sprite)
                }
                bakedQuads.add(builder.build())
            }
        }
        return bakedQuads
    }

    private fun getNormal(vertex: List<IVector3>): IVector3 {
        val ac = vertex[2] - vertex[0]
        val bd = vertex[3] - vertex[1]
        return (ac cross bd).normalize()
    }

    private fun putVertex(builder: UnpackedBakedQuad.Builder, format: VertexFormat, side: IVector3,
                          pos: IVector3, tex: IVector2, sprite: TextureAtlasSprite) {

        for (e in 0 until format.elementCount) {
            when (format.getElement(e).usage) {
                VertexFormatElement.EnumUsage.POSITION -> builder.put(e, pos.xf, pos.yf, pos.zf, 1f)
                VertexFormatElement.EnumUsage.COLOR -> builder.put(e, 1f, 1f, 1f, 1f)
                VertexFormatElement.EnumUsage.NORMAL -> builder.put(e, side.xf, side.yf, side.zf, 0f)
                VertexFormatElement.EnumUsage.UV -> {
                    if (format.getElement(e).index == 0) {
                        builder.put(e,
                                sprite.getInterpolatedU(tex.xd * 16.0),
                                sprite.getInterpolatedV(tex.yd * 16.0),
                                0f, 1f)
                    }
                }
                else -> builder.put(e)
            }
        }
    }
}