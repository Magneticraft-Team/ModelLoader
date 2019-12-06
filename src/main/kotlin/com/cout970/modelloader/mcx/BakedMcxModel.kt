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

/**
 * Baked model for MCx models
 */
class BakedMcxModel(
    particles: TextureAtlasSprite,
    itemTransform: ItemCameraTransforms,
    useAmbientOcclusion: Boolean,
    use3dInGui: Boolean,
    bakedQuads: Map<Direction?, List<BakedQuad>>
) : IBakedModel, IItemTransformable {

    var configParticles: TextureAtlasSprite = particles
    var configItemTransform: ItemCameraTransforms = itemTransform
    var configUseAmbientOcclusion: Boolean = useAmbientOcclusion
    var configUse3dInGui: Boolean = use3dInGui
    var configBakedQuads: Map<Direction?, List<BakedQuad>> = bakedQuads
    var configHasItemRenderer: Boolean = false

    override fun getParticleTexture(): TextureAtlasSprite = configParticles

    override fun getQuads(state: BlockState?, side: Direction?, rand: Random): List<BakedQuad> {
        return configBakedQuads[side] ?: emptyList()
    }

    override fun isBuiltInRenderer(): Boolean = configHasItemRenderer

    override fun isAmbientOcclusion(): Boolean = configUseAmbientOcclusion

    override fun isGui3d(): Boolean = configUse3dInGui

    override fun getOverrides(): ItemOverrideList = ItemOverrideList.EMPTY

    override fun getItemCameraTransforms(): ItemCameraTransforms {
        return configItemTransform
    }

    override fun setHasItemRenderer(hasItemRenderer: Boolean) {
        this.configHasItemRenderer = hasItemRenderer
    }

    override fun setItemTransforms(it: ItemTransforms) {
        configItemTransform = it.toItemCameraTransforms()
    }
}