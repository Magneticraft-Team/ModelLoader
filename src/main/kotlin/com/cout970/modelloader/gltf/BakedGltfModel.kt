package com.cout970.modelloader.gltf

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
 * Baked model for glTF models
 */
class BakedGltfModel(
    val quads: List<BakedQuad>,
    val particle: TextureAtlasSprite,
    var itemTransform: ItemCameraTransforms
) : IBakedModel, IItemTransformable {

    var hasItemRenderer: Boolean = false
        private set

    override fun getParticleTexture(): TextureAtlasSprite = particle

    override fun getQuads(state: BlockState?, side: Direction?, rand: Random): List<BakedQuad> = quads

    override fun getOverrides(): ItemOverrideList = ItemOverrideList.EMPTY

    override fun isBuiltInRenderer(): Boolean = hasItemRenderer

    override fun isAmbientOcclusion(): Boolean = true

    override fun isGui3d(): Boolean = true

    override fun getItemCameraTransforms(): ItemCameraTransforms = itemTransform

    override fun setHasItemRenderer(hasItemRenderer: Boolean) {
        this.hasItemRenderer = hasItemRenderer
    }

    override fun setItemTransforms(it: ItemTransforms) {
        itemTransform = it.toItemCameraTransforms()
    }
}