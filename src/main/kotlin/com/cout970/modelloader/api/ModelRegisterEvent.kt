package com.cout970.modelloader.api

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