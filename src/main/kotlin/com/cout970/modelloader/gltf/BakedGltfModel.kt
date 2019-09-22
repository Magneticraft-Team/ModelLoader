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

class BakedGltfModel(
    val quads: List<BakedQuad>,
    val particle: TextureAtlasSprite,
    var itemTransform: ItemCameraTransforms
): IBakedModel, IItemTransformable {

    override fun getParticleTexture(): TextureAtlasSprite = particle

    override fun getQuads(state: BlockState?, side: Direction?, rand: Random): List<BakedQuad> = quads

    override fun getOverrides(): ItemOverrideList = ItemOverrideList.EMPTY

    override fun isBuiltInRenderer(): Boolean = false

    override fun isAmbientOcclusion(): Boolean = true

    override fun isGui3d(): Boolean = true

    override fun getItemCameraTransforms(): ItemCameraTransforms = itemTransform

    override fun setItemTransforms(it: ItemTransforms) {
        itemTransform = it.toItemCameraTransforms()
    }
}