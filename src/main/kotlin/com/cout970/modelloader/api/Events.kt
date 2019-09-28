package com.cout970.modelloader.api

import com.cout970.modelloader.PostBakeModel
import com.cout970.modelloader.animation.AnimatedModel
import net.minecraft.client.renderer.model.IBakedModel
import net.minecraft.client.renderer.model.ModelResourceLocation
import net.minecraftforge.eventbus.api.Event

/**
 * Event fired when the models are loaded (startup and reload) so a mod can register their models
 */
class ModelRegisterEvent(
    val map: MutableMap<ModelResourceLocation, ModelConfig>,
    val sharedMap: MutableMap<ModelResourceLocation, ModelResourceLocation>
) : Event() {

    /**
     * Registers a model to be loaded
     */
    fun registerModel(modelId: ModelResourceLocation, config: ModelConfig) {
        map[modelId] = config
    }

    /**
     * Registers a model to be loaded
     */
    fun registerModel(modId: String, path: String, variant: String, config: ModelConfig) {
        map[ModelResourceLocation("$modId:$path#$variant")] = config
    }

    /**
     * Reuses a registered model for another model id
     */
    fun shareModel(originModId: String, originPath: String, originVariant: String, destineModId: String, destinePath: String, destineVariant: String) {
        shareModel(
            ModelResourceLocation("$originModId:$originPath#$originVariant"),
            ModelResourceLocation("$destineModId:$destinePath#$destineVariant")
        )
    }

    /**
     * Reuses a registered model for another model id
     */
    fun shareModel(origin: ModelResourceLocation, destine: ModelResourceLocation) {
        sharedMap[destine] = origin
    }
}

/**
 * Event fired after the models are baked so a mod can retrieve their models
 */
class ModelRetrieveEvent(val map: Map<ModelResourceLocation, PostBakeModel>) : Event() {

    /**
     * Retrieves a baked model
     */
    fun getModel(modelId: ModelResourceLocation): IBakedModel? {
        return map[modelId]?.baked
    }

    /**
     * Retrieves a baked model
     */
    fun getModel(modId: String, path: String, variant: String): IBakedModel? {
        return getModel(ModelResourceLocation("$modId:$path#$variant"))
    }

    /**
     * Retrieves all the animations of a model
     */
    fun getAnimations(modelId: ModelResourceLocation): Map<String, AnimatedModel> {
        return map[modelId]?.animations ?: emptyMap()
    }

    /**
     * Retrieves all the animations of a model
     */
    fun getAnimations(modId: String, path: String, variant: String): Map<String, AnimatedModel> {
        return getAnimations(ModelResourceLocation("$modId:$path#$variant"))
    }
}