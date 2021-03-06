package com.cout970.modelloader.api

import net.minecraft.client.renderer.model.IBakedModel
import net.minecraft.client.renderer.model.IUnbakedModel
import net.minecraft.client.renderer.model.ModelResourceLocation
import net.minecraft.client.renderer.model.ModelRotation
import net.minecraft.util.Direction
import net.minecraft.util.ResourceLocation

/**
 * Configuration that tells the library how to load a model.
 */
data class ModelConfig @JvmOverloads constructor(
    val location: ResourceLocation,
    val itemTransforms: ItemTransforms = ItemTransforms.DEFAULT,
    val rotation: ModelRotation = ModelRotation.X0_Y0,
    val bake: Boolean = true,
    val animate: Boolean = false,
    val mutable: Boolean = false,
    val itemRenderer: Boolean = false,
    val preBake: ((ModelResourceLocation, IUnbakedModel) -> IUnbakedModel)? = null,
    val postBake: ((ModelResourceLocation, IBakedModel) -> IBakedModel)? = null,
    val partFilter: ((String)-> Boolean)? = null
) {
    /**
     * Marks the location where the model file is stored
     */
    fun withLocation(location: ResourceLocation) = copy(location = location)

    /**
     * Marks the transformations to apply in several item views
     */
    fun withItemTransforms(itemTransforms: ItemTransforms) = copy(itemTransforms = itemTransforms)

    /**
     * Marks a rotation to apply to the model before baking
     */
    fun withRotation(rotation: ModelRotation) = copy(rotation = rotation)

    /**
     * Marks a rotation using a Direction component
     */
    fun withDirection(dir: Direction) = copy(rotation = DIRECTION_TO_ROTATION.getValue(dir))

    /**
     * Indicates that this model must be baked, this allow the model to be used for an item or a blockstate
     */
    fun withBake(bake: Boolean) = copy(bake = bake)

    /**
     * Indicates that this model must be analyzed to create animated models
     */
    fun withAnimation(animate: Boolean) = copy(animate = animate)

    /**
     * Indicates that this model should be processed to generate a [MutableModel]
     */
    fun withMutable(mutable: Boolean) = copy(mutable = mutable)

    /**
     * Marks the generated bakedmodel so Minecraft knows that the item with that model needs to use an ItemRenderer
     */
    fun withItemRenderer(itemRenderer: Boolean) = copy(itemRenderer = itemRenderer)

    /**
     * Binds a callback that will receive the model after is gets loaded
     *
     * You must return a new model instead of editing the argument,
     * because models are loaded only once and shared for several ModelConfigs registrations
     */
    fun withPreBake(preBake: ((ModelResourceLocation, IUnbakedModel) -> IUnbakedModel)?) = copy(preBake = preBake)

    /**
     * Binds a callback that will receive the model after is gets baked
     *
     * You can return the same model with changes or return a different instance, for example, wrapping the model
     */
    fun withPostBake(postBake: ((ModelResourceLocation, IBakedModel) -> IBakedModel)?) = copy(postBake = postBake)

    /**
     * Binds a callback that filters which parts to keep in the model
     */
    fun withPartFilter(partFilter: ((String)-> Boolean)?) = copy(partFilter = partFilter)

    /**
     * Combines withDirection and withRotation, allowing to use a direction for rotation
     * and apply an extra rotation over it
     */
    fun withDirection(dir: Direction, rot: ModelRotation): ModelConfig {
        return withRotation(DIRECTION_TO_ROTATION.getValue(dir) + rot)
    }

    companion object {

        /**
         * Rotations per direction
         */
        @JvmField
        val DIRECTION_TO_ROTATION = mapOf(
            Direction.DOWN to ModelRotation.X0_Y0,
            Direction.UP to ModelRotation.X180_Y0,
            Direction.NORTH to ModelRotation.X90_Y0,
            Direction.SOUTH to ModelRotation.X270_Y0,
            Direction.WEST to ModelRotation.X270_Y270,
            Direction.EAST to ModelRotation.X270_Y90
        )

        /**
         * Adds 2 rotations together
         */
        operator fun ModelRotation.plus(other: ModelRotation): ModelRotation {
            val x = this.getX() + other.getX()
            val y = this.getY() + other.getY()
            return ModelRotation.getModelRotation(x, y)
        }

        /**
         * Gets the X component of a rotation
         */
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

        /**
         * Gets the Y component of a rotation
         */
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