@file:Suppress("DEPRECATION")

package com.cout970.modelloader.mcx

import com.cout970.modelloader.IItemTransformable
import com.cout970.modelloader.api.ItemTransforms
import net.minecraft.block.BlockState
import net.minecraft.client.renderer.model.BakedQuad
import net.minecraft.client.renderer.model.IBakedModel
import net.minecraft.client.renderer.model.ItemCameraTransforms
import net.minecraft.client.renderer.model.ItemOverrideList
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.util.Direction
import java.util.*

class BakedMcxModel(
    val modelData: UnbakedMcxModel,
    var particles: TextureAtlasSprite,
    var itemTransform: ItemCameraTransforms,
    quads: List<BakedQuad>
) : IBakedModel, IItemTransformable {

    var hasItemRenderer: Boolean = false
        private set

    val bakedQuads: MutableMap<Direction?, MutableList<BakedQuad>> = modelData.parts
        .groupBy { it.side }
        .mapValues { entry ->
            entry.value.flatMap { quads.subList(it.from, it.to) }.toMutableList()
        }.toMutableMap()

    override fun getParticleTexture(): TextureAtlasSprite = particles

    override fun getQuads(state: BlockState?, side: Direction?, rand: Random): List<BakedQuad> {
        return bakedQuads[side] ?: emptyList()
    }

    override fun isBuiltInRenderer(): Boolean = hasItemRenderer

    override fun isAmbientOcclusion(): Boolean = modelData.useAmbientOcclusion

    override fun isGui3d(): Boolean = modelData.use3dInGui

    override fun getOverrides(): ItemOverrideList = ItemOverrideList.EMPTY

    override fun getItemCameraTransforms(): ItemCameraTransforms {
        return itemTransform
    }

    override fun setHasItemRenderer(hasItemRenderer: Boolean) {
        this.hasItemRenderer = hasItemRenderer
    }

    override fun setItemTransforms(it: ItemTransforms) {
        itemTransform = it.toItemCameraTransforms()
    }
}