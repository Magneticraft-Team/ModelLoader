package com.cout970.modelloader.api

import net.minecraft.client.renderer.model.IBakedModel
import net.minecraft.client.renderer.model.IUnbakedModel
import net.minecraft.client.renderer.model.ModelResourceLocation
import net.minecraft.client.renderer.model.ModelRotation
import net.minecraft.util.Direction
import net.minecraft.util.ResourceLocation

data class ModelConfig @JvmOverloads constructor(
    val location: ResourceLocation,
    val itemTransforms: ItemTransforms = ItemTransforms.DEFAULT,
    val rotation: ModelRotation = ModelRotation.X0_Y0,
    val bake: Boolean = true,
    val animate: Boolean = false,
    val itemRenderer: Boolean = false,
    val preBake: ((ModelResourceLocation, IUnbakedModel) -> IUnbakedModel)? = null,
    val postBake: ((ModelResourceLocation, IBakedModel) -> IBakedModel)? = null
) {
    fun withLocation(location: ResourceLocation) = copy(location = location)
    fun withItemTransforms(itemTransforms: ItemTransforms) = copy(itemTransforms = itemTransforms)
    fun withRotation(rotation: ModelRotation) = copy(rotation = rotation)
    fun withDirection(dir: Direction) = copy(rotation = DIRECTION_TO_ROTATION.getValue(dir))
    fun withBake(bake: Boolean) = copy(bake = bake)
    fun withAnimation(animate: Boolean) = copy(animate = animate)
    fun withItemRenderer(itemRenderer: Boolean) = copy(itemRenderer = itemRenderer)
    fun withPreBake(preBake: (ModelResourceLocation, IUnbakedModel) -> IUnbakedModel) = copy(preBake = preBake)
    fun withPostBake(postBake: (ModelResourceLocation, IBakedModel) -> IBakedModel) = copy(postBake = postBake)

    fun withDirection(dir: Direction, rot: ModelRotation): ModelConfig {
        return withRotation(DIRECTION_TO_ROTATION.getValue(dir) + rot)
    }

    companion object {
        @JvmField
        val DIRECTION_TO_ROTATION = mapOf(
            Direction.DOWN to ModelRotation.X0_Y0,
            Direction.UP to ModelRotation.X180_Y0,
            Direction.NORTH to ModelRotation.X90_Y0,
            Direction.SOUTH to ModelRotation.X270_Y0,
            Direction.WEST to ModelRotation.X270_Y270,
            Direction.EAST to ModelRotation.X270_Y90
        )

        operator fun ModelRotation.plus(other: ModelRotation): ModelRotation {
            val x = this.getX() + other.getX()
            val y = this.getY() + other.getY()
            return ModelRotation.getModelRotation(x, y)
        }

        fun ModelRotation.getX() = when (this) {
            ModelRotation.X0_Y0 -> 0
            ModelRotation.X0_Y90 -> 0
            ModelRotation.X0_Y180 -> 0
            ModelRotation.X0_Y270 -> 0
            ModelRotation.X90_Y0 -> 90
            ModelRotation.X90_Y90 -> 90
            ModelRotation.X90_Y180 -> 90
            ModelRotation.X90_Y270 -> 90
            ModelRotation.X180_Y0 -> 180
            ModelRotation.X180_Y90 -> 180
            ModelRotation.X180_Y180 -> 180
            ModelRotation.X180_Y270 -> 180
            ModelRotation.X270_Y0 -> 270
            ModelRotation.X270_Y90 -> 270
            ModelRotation.X270_Y180 -> 270
            ModelRotation.X270_Y270 -> 270
        }

        fun ModelRotation.getY() = when (this) {
            ModelRotation.X0_Y0 -> 0
            ModelRotation.X0_Y90 -> 90
            ModelRotation.X0_Y180 -> 180
            ModelRotation.X0_Y270 -> 270
            ModelRotation.X90_Y0 -> 0
            ModelRotation.X90_Y90 -> 90
            ModelRotation.X90_Y180 -> 180
            ModelRotation.X90_Y270 -> 270
            ModelRotation.X180_Y0 -> 0
            ModelRotation.X180_Y90 -> 90
            ModelRotation.X180_Y180 -> 180
            ModelRotation.X180_Y270 -> 270
            ModelRotation.X270_Y0 -> 0
            ModelRotation.X270_Y90 -> 90
            ModelRotation.X270_Y180 -> 180
            ModelRotation.X270_Y270 -> 270
        }
    }
}