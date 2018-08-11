package com.cout970.modelloader.api.formats.gltf

import com.cout970.modelloader.internal.resourceLocationOf
import net.minecraft.block.state.IBlockState
import net.minecraft.client.renderer.block.model.BakedQuad
import net.minecraft.client.renderer.block.model.IBakedModel
import net.minecraft.client.renderer.block.model.ItemOverrideList
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.vertex.VertexFormat
import net.minecraft.util.EnumFacing
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.IModel
import net.minecraftforge.common.model.IModelState
import java.util.function.Function

class GltfModel(val location: ResourceLocation, val definition: GltfFile, val structure: GltfStructure.File) : IModel {

    override fun bake(state: IModelState, format: VertexFormat,
                      bakedTextureGetter: Function<ResourceLocation, TextureAtlasSprite>): IBakedModel {

        return GltfBaker(format, bakedTextureGetter).bake(this)
    }

    override fun getTextures(): Collection<ResourceLocation> {
        return definition.images.map { resourceLocationOf(location, it.uri!!) }
    }
}

class GltfBakedModel(val quads: Map<EnumFacing?, List<BakedQuad>>, val particle: TextureAtlasSprite): IBakedModel {

    override fun getParticleTexture(): TextureAtlasSprite = particle

    override fun getQuads(state: IBlockState?, side: EnumFacing?, rand: Long): List<BakedQuad> = quads[side] ?: emptyList()

    override fun isBuiltInRenderer(): Boolean = false

    override fun isAmbientOcclusion(): Boolean = true

    override fun isGui3d(): Boolean = true

    override fun getOverrides(): ItemOverrideList = ItemOverrideList.NONE
}