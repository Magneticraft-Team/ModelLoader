package com.cout970.modelloader.api

import net.minecraft.client.renderer.block.model.IBakedModel
import net.minecraft.client.renderer.block.model.ModelResourceLocation

/**
 * Created by cout970 on 2017/06/16.
 */
interface IBakedModelDecorator {

    /**
     * Decorated a baked model, for example wrapping the model with an IPerspectiveAwareModel
     */
    fun decorate(model: IBakedModel, modelResourceLocation: ModelResourceLocation): IBakedModel
}