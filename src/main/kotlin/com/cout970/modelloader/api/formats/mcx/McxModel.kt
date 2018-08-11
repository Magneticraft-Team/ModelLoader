package com.cout970.modelloader.api.formats.mcx

import com.cout970.vector.api.IVector2
import com.cout970.vector.api.IVector3
import net.minecraft.block.state.IBlockState
import net.minecraft.client.renderer.block.model.BakedQuad
import net.minecraft.client.renderer.block.model.IBakedModel
import net.minecraft.client.renderer.block.model.ItemCameraTransforms
import net.minecraft.client.renderer.block.model.ItemOverrideList
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.vertex.VertexFormat
import net.minecraft.util.EnumFacing
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.IModel
import net.minecraftforge.common.model.IModelState
import net.minecraftforge.common.model.TRSRTransformation

private typealias TextureGetter = java.util.function.Function<ResourceLocation, TextureAtlasSprite>

class McxModel(
        val useAmbientOcclusion: Boolean,
        val use3dInGui: Boolean,
        val particleTexture: ResourceLocation,
        val parts: List<Part>,
        val quads: Mesh
) : IModel {

    override fun bake(state: IModelState, format: VertexFormat, textureGetter: TextureGetter): IBakedModel {
        return McxBaker(format, textureGetter).bake(this)
    }

    override fun getTextures(): MutableCollection<ResourceLocation> {
        return (parts.map { it.texture } + particleTexture).distinct().toMutableList()
    }

    override fun getDefaultState(): IModelState = TRSRTransformation.identity()

    override fun getDependencies(): MutableCollection<ResourceLocation> = mutableListOf()

    class Part(val name: String, val from: Int, val to: Int, val side: EnumFacing?, val texture: ResourceLocation)
}

class Mesh(val pos: List<IVector3>, val tex: List<IVector2>, val indices: List<Indices>) {

    class Indices(val a: Int, val b: Int, val c: Int, val d: Int,
                  val at: Int, val bt: Int, val ct: Int, val dt: Int)

}

class BakedMcxModel(val modelData: McxModel, val particles: TextureAtlasSprite, quads: List<BakedQuad>) : IBakedModel {

    val bakedQuads: Map<EnumFacing?, MutableList<BakedQuad>> = modelData.parts
            .groupBy { it.side }
            .mapValues { entry ->
                entry.value.flatMap { quads.subList(it.from, it.to) }.toMutableList()
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