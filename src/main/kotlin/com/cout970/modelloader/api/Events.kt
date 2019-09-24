package com.cout970.modelloader.api

import com.cout970.modelloader.PostBakeModel
import com.cout970.modelloader.animation.AnimatedModel
import net.minecraft.client.renderer.model.IBakedModel
import net.minecraft.client.renderer.model.ModelResourceLocation
import net.minecraftforge.eventbus.api.Event

/**
 * Event fired when the models are loaded (startup and reload) so a mod can register their models
 */
class ModelRegisterEvent(val map: MutableMap<ModelResourceLocation, ModelConfig>) : Event() {

    fun registerModel(modelId: ModelResourceLocation, config: ModelConfig) {
        map[modelId] = config
    }

    fun registerModel(modId: String, path: String, variant: String, config: ModelConfig) {
        map[ModelResourceLocation("$modId:$path#$variant")] = config
    }
}

/**
 * Event fired after the models are baked so a mod can retrieve their models
 */
class ModelRetrieveEvent(val map: Map<ModelResourceLocation, PostBakeModel>) : Event() {

    fun getModel(modelId: ModelResourceLocation): IBakedModel? {
        return map[modelId]?.baked
    }

    fun getModel(modId: String, path: String, variant: String): IBakedModel? {
        return getModel(ModelResourceLocation("$modId:$path#$variant"))
    }

    fun getAnimations(modelId: ModelResourceLocation): Map<String, AnimatedModel> {
        return map[modelId]?.animations ?: emptyMap()
    }

    fun getAnimations(modId: String, path: String, variant: String): Map<String, AnimatedModel> {
        return getAnimations(ModelResourceLocation("$modId:$path#$variant"))
    }
}