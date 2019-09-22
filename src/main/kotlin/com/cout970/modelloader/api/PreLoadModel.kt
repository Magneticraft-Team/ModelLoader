package com.cout970.modelloader.api

import net.minecraft.client.renderer.model.IBakedModel
import net.minecraft.client.renderer.model.IUnbakedModel
import net.minecraft.client.renderer.model.ModelResourceLocation
import net.minecraft.client.renderer.model.ModelRotation
import net.minecraft.util.ResourceLocation

data class PreLoadModel @JvmOverloads constructor(
    val modelId: ModelResourceLocation,
    val location: ResourceLocation,
    val itemTransforms: ItemTransforms = ItemTransforms.DEFAULT,
    val rotation: ModelRotation = ModelRotation.X0_Y0,
    val bake: Boolean = true,
    val preBake: ((ModelResourceLocation, IUnbakedModel) -> IUnbakedModel)? = null,
    val postBake: ((ModelResourceLocation, IBakedModel) -> IBakedModel)? = null
) {
    fun withModelId(modelId: ModelResourceLocation) = copy(modelId = modelId)
    fun withLocation(location: ResourceLocation) = copy(location = location)
    fun withItemTransforms(itemTransforms: ItemTransforms) = copy(itemTransforms = itemTransforms)
    fun withRotation(rotation: ModelRotation) = copy(rotation = rotation)
    fun withBake(bake: Boolean) = copy(bake = bake)
    fun withPreBake(preBake: (ModelResourceLocation, IUnbakedModel) -> IUnbakedModel) = copy(preBake = preBake)
    fun withPostBake(postBake: (ModelResourceLocation, IBakedModel) -> IBakedModel) = copy(postBake = postBake)
}