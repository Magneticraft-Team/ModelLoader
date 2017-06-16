package com.cout970.modelloader

import com.cout970.vector.api.IVector2
import com.cout970.vector.api.IVector3
import com.cout970.vector.extensions.*
import com.google.common.base.Function
import net.minecraft.block.state.IBlockState
import net.minecraft.client.renderer.block.model.BakedQuad
import net.minecraft.client.renderer.block.model.IBakedModel
import net.minecraft.client.renderer.block.model.ItemCameraTransforms
import net.minecraft.client.renderer.block.model.ItemOverrideList
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.vertex.VertexFormat
import net.minecraft.client.renderer.vertex.VertexFormatElement
import net.minecraft.util.EnumFacing
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.IModel
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad
import net.minecraftforge.common.model.IModelState
import net.minecraftforge.common.model.TRSRTransformation

/**
 * Created by cout970 on 2017/01/26.
 */

class ModelData(
        val useAmbientOcclusion: Boolean,
        val use3dInGui: Boolean,
        val particleTexture: ResourceLocation,
        val parts: List<Part>,
        val quads: QuadStorage
) : IModel {

    override fun bake(state: IModelState?, format: VertexFormat,
                      textureGetter: Function<ResourceLocation, TextureAtlasSprite>): IBakedModel {

        val quads = quads.bake(format, textureGetter, parts)
        val particles = textureGetter.apply(particleTexture)!!
        return QuadProvider(this, particles, quads)
    }

    override fun getTextures(): MutableCollection<ResourceLocation> {
        return (parts.map { it.texture } + particleTexture).distinct().toMutableList()
    }

    override fun getDefaultState(): IModelState = TRSRTransformation.identity()

    override fun getDependencies(): MutableCollection<ResourceLocation> = mutableListOf()

    class Part(val name: String, val from: Int, val to: Int, val side: EnumFacing?, val texture: ResourceLocation)
}

class QuadStorage(val pos: List<IVector3>, val tex: List<IVector2>, val indices: List<QuadIndices>) {

    class QuadIndices(val a: Int, val b: Int, val c: Int, val d: Int,
                      val at: Int, val bt: Int, val ct: Int, val dt: Int)

    fun bake(format: VertexFormat, textureGetter: Function<ResourceLocation, TextureAtlasSprite>,
             parts: List<ModelData.Part>): List<BakedQuad> {

        val bakedQuads = mutableListOf<BakedQuad>()
        for (part in parts) {
            val sprite = textureGetter.apply(part.texture)!!
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

        for (e in 0..format.elementCount - 1) {
            when (format.getElement(e).usage) {
                VertexFormatElement.EnumUsage.POSITION -> builder.put(e, pos.xf, pos.yf, pos.zf, 1f)
                VertexFormatElement.EnumUsage.COLOR -> builder.put(e, 1f, 1f, 1f, 1f)
                VertexFormatElement.EnumUsage.UV -> {
                    if (format.getElement(e).index == 0) {
                        builder.put(e, sprite.getInterpolatedU(tex.xd * 16.0), sprite.getInterpolatedV(tex.yd * 16.0),
                                0f, 1f)
                    }
                }
                VertexFormatElement.EnumUsage.NORMAL -> builder.put(e, side.xf, side.yf, side.zf, 0f)
                else -> builder.put(e)
            }
        }
    }
}

class QuadProvider(val modelData: ModelData, val particles: TextureAtlasSprite, quads: List<BakedQuad>) : IBakedModel {

    val bakedQuads: Map<EnumFacing?, MutableList<BakedQuad>> = modelData.parts
            .groupBy { it.side }
            .mapValues {
                it.value.flatMap { quads.subList(it.from, it.to) }.toMutableList()
            }

    override fun getParticleTexture(): TextureAtlasSprite = particles

    override fun getQuads(state: IBlockState?, side: EnumFacing?, rand: Long): List<BakedQuad> {
        return bakedQuads[side] ?: emptyList()
    }

    override fun getItemCameraTransforms(): ItemCameraTransforms = ItemCameraTransforms.DEFAULT

    override fun isBuiltInRenderer(): Boolean = false

    override fun isAmbientOcclusion(): Boolean = modelData.useAmbientOcclusion

    override fun isGui3d(): Boolean = modelData.use3dInGui

    override fun getOverrides(): ItemOverrideList = ItemOverrideList.NONE
}