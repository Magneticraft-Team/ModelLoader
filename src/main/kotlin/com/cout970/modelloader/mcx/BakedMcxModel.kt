package com.cout970.modelloader.mcx

import net.minecraft.block.BlockState
import net.minecraft.client.renderer.model.BakedQuad
import net.minecraft.client.renderer.model.IBakedModel
import net.minecraft.client.renderer.model.ItemOverrideList
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.util.Direction
import java.util.*

class BakedMcxModel(val modelData: UnbakedMcxModel, val particles: TextureAtlasSprite, quads: List<BakedQuad>) : IBakedModel {

    val bakedQuads: Map<Direction?, MutableList<BakedQuad>> = modelData.parts
        .groupBy { it.side }
        .mapValues { entry ->
            entry.value.flatMap { quads.subList(it.from, it.to) }.toMutableList()
        }

    override fun getParticleTexture(): TextureAtlasSprite = particles

    override fun getQuads(state: BlockState?, side: Direction?, rand: Random): List<BakedQuad> {
        return bakedQuads[side] ?: emptyList()
    }

    override fun isBuiltInRenderer(): Boolean = false

    override fun isAmbientOcclusion(): Boolean = modelData.useAmbientOcclusion

    override fun isGui3d(): Boolean = modelData.use3dInGui

    override fun getOverrides(): ItemOverrideList = ItemOverrideList.EMPTY
}