package com.cout970.modelloader

import com.cout970.modelloader.api.IBakedModelDecorator
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.block.model.IBakedModel
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.resources.IResourceManager
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.ModelBakeEvent
import net.minecraftforge.client.event.TextureStitchEvent
import net.minecraftforge.client.model.IModel
import net.minecraftforge.client.model.ModelLoader
import net.minecraftforge.client.model.ModelLoaderRegistry
import net.minecraftforge.common.model.TRSRTransformation
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

/**
 * Created by cout970 on 2017/03/05.
 */
object ModelManager {

    // registered model to load
    internal val models = mutableMapOf<ModelResourceLocation, ResourceLocation>()

    // model textures to load
    private val texturesCache = mutableListOf<ResourceLocation>()
    // models loaded from files
    private val modelCache = mutableMapOf<ResourceLocation, IModel>()
    // models to register in the game
    private val toRegisterCache = mutableMapOf<ModelResourceLocation, IModel>()
    // models loaded by this class
    internal val loadedModels = mutableMapOf<ModelResourceLocation, QuadProvider>()
    // Model decorators to wrap the IBackedModel
    internal val decorators = mutableMapOf<ModelResourceLocation, IBakedModelDecorator>()

    @SubscribeEvent
    fun onTextureStart(event: TextureStitchEvent.Pre) {
        // loads all models to get the textures needed
        loadAll()
        //register every texture once
        texturesCache.distinct().forEach {
            event.map.registerSprite(it)
        }
        texturesCache.clear()
    }

    private fun loadAll() {
        val manager = Minecraft.getMinecraft().resourceManager
        //makes sure all caches are empty
        texturesCache.clear()
        toRegisterCache.clear()
        modelCache.clear()

        // loads every model
        models.forEach { key, value ->
            try {
                val model = modelCache.getOrPut(value) {
                    // loads the IModel
                    val model = value.toModel(manager)
                    // collects all textures from the model
                    texturesCache.addAll(model.textures)
                    // returns the model
                    model
                }
                toRegisterCache.put(key, model)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @SubscribeEvent
    fun onModelBakeEvent(event: ModelBakeEvent) {
        val cache = mutableMapOf<IModel, IBakedModel>()

        loadedModels.clear()
        toRegisterCache.forEach { loc, model ->
            // bakes the model, but only once
            val baked = cache.getOrPut(model) {
                model.toBakeModel()
            }
            // save model for use in TileEntitySpecialRenderers
            (baked as? QuadProvider)?.let { loadedModels.put(loc, it) }
            // decorates the model for custom rendering based on the perspective: IPerspectiveAwareModel, etc
            val finalModel = decorators[loc]?.decorate(baked, loc) ?: baked
            // register the model into the game
            event.modelRegistry.putObject(loc, finalModel)
        }
        toRegisterCache.clear()
    }

    private fun ResourceLocation.toModel(manager: IResourceManager): IModel {
        try {
            return ModelSerializer.load(manager.getResource(this).inputStream)
        } catch (e: Exception) {
            ModelLoaderMod.logger.error("Error reading model data for location: $this")
            e.printStackTrace()
        }
        return ModelLoaderRegistry.getMissingModel()
    }

    private fun IModel.toBakeModel(): IBakedModel {
        return bake(TRSRTransformation.identity(), DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter())
    }
}