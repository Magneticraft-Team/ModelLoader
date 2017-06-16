package com.cout970.modelloader.api

import com.cout970.modelloader.ModelManager
import com.cout970.modelloader.QuadProvider
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

/**
 * Created by cout970 on 2017/06/16.
 */
object ModelLoaderApi {

    /**
     * Stores the ModelResourceLocation for later loading (when the game reloads all models)
     * The baked model will be injected into the game model registry,
     * so it can be used for block/item models, just register the same modelId in a [BlockStateMapper]
     * or with [ModelLoader.setCustomModelResourceLocation]
     *
     * @param modelId model location used to identify the model with [getModel]
     *                and to inject it into the game model registry
     *
     * @param modelLocation path to the model file, mod:path is translated to assets/mod/path
     */
    @SideOnly(Side.CLIENT)
    fun registerModel(modelId: ModelResourceLocation, modelLocation: ResourceLocation) {
        ModelManager.models.put(modelId, modelLocation)
    }

    /**
     * Same as [registerModel] but adding a decorator that allow to add custom wrappers around the IBakedModel
     *
     * @param decorator object that wraps the IBakedModel to allow custom implementations,
     *                  the parameter of the method [decorate] can be the missing model or a instance of QuadProvider
     */
    @SideOnly(Side.CLIENT)
    fun registerModelWithDecorator(modelId: ModelResourceLocation, modelLocation: ResourceLocation,
                                   decorator: IBakedModelDecorator) {
        ModelManager.models.put(modelId, modelLocation)
        ModelManager.decorators.put(modelId, decorator)
    }

    /**
     * Gets the model associated to the modelId registered with [registerModel] or [registerModelWithDecorator]
     * if the id is not valid or the model loading process had an error, the result will be null
     *
     * This method allow TileEntitySpecialRenderers to get any model registered with this Loader
     *
     * @param modelId model location used to identify the model
     * @return QuadProvider the implements IBakedModel and has the ModelData,
     *         this model is not decorated by any [IBakedModelDecorator]
     */
    @SideOnly(Side.CLIENT)
    fun getModel(modelId: ModelResourceLocation): QuadProvider? {
        return ModelManager.loadedModels[modelId]
    }
}