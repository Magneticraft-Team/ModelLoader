package com.cout970.modelloader

import com.cout970.modelloader.animation.AnimatedModel
import com.cout970.modelloader.api.*
import com.cout970.modelloader.mutable.MutableModel
import net.minecraft.client.renderer.model.IBakedModel
import net.minecraft.client.renderer.model.IUnbakedModel
import net.minecraft.client.renderer.model.ModelResourceLocation
import net.minecraft.client.renderer.texture.ISprite
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.resources.IResourceManager
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.ModelBakeEvent
import net.minecraftforge.client.event.TextureStitchEvent
import net.minecraftforge.client.model.ModelLoader
import net.minecraftforge.fml.ModLoader
import java.util.stream.Collectors

/**
 * Model loaded from disk
 */
data class PreBakeModel(
    val modelId: ModelResourceLocation,
    val pre: ModelConfig,
    val unbaked: IUnbakedModel
)

/**
 * Model after the bake event
 */
data class PostBakeModel(
    val modelId: ModelResourceLocation,
    val pre: ModelConfig,
    val baked: IBakedModel?,
    val animations: Map<String, AnimatedModel>,
    val mutable: MutableModel?
)

/**
 * IBakedModel with extra variables
 */
interface IItemTransformable {
    fun setItemTransforms(it: ItemTransforms)
    fun setHasItemRenderer(hasItemRenderer: Boolean)
}

/**
 * Internal use only, please use ModelRegisterEvent and ModelRetrieveEvent instead
 */
object ModelManager {
    private val registeredModels = mutableMapOf<ModelResourceLocation, ModelConfig>()
    private val sharedModels = mutableMapOf<ModelResourceLocation, ModelResourceLocation>()
    private var textureToRegister: Set<ResourceLocation>? = null
    private var modelsToBake: List<PreBakeModel>? = null
    private var loadedModels: Map<ModelResourceLocation, PostBakeModel> = emptyMap()

    @JvmStatic
    fun register(modelId: ModelResourceLocation, pre: ModelConfig) {
        registeredModels[modelId] = pre
    }

    @JvmStatic
    fun share(origin: ModelResourceLocation, destine: ModelResourceLocation) {
        sharedModels[destine] = origin
    }

    @JvmStatic
    fun getModel(modelId: ModelResourceLocation): IBakedModel? {
        return loadedModels[modelId]?.baked
    }

    @JvmStatic
    fun getAnimations(modelId: ModelResourceLocation): Map<String, AnimatedModel> {
        return loadedModels[modelId]?.animations ?: emptyMap()
    }

    /**
     * Called to load from disk all models
     */
    fun loadModelFiles(resourceManager: IResourceManager) {
        try {
            ModLoader.get().postEvent(ModelRegisterEvent(registeredModels, sharedModels))
        } catch (e: Exception) {
            ModelLoaderMod.logger.error("Error in ModelRegisterEvent, some models may be missing, check the log for details")
        }

        // Load model from disk
        val cache = if (Config.useMultithreading.get()) {
            registeredModels.values
                .parallelStream()
                .map { it.location }
                .distinct()
                .map { it to ModelFormatRegistry.loadUnbakedModel(resourceManager, it) }
                .collect(Collectors.toList())
                .toMap()
        } else {
            registeredModels.values
                .asSequence()
                .map { it.location }
                .distinct()
                .map { it to ModelFormatRegistry.loadUnbakedModel(resourceManager, it) }
                .toMap()
        }

        val textures = mutableSetOf<ResourceLocation>()
        val models = mutableListOf<PreBakeModel>()
        val errors = mutableSetOf<String>()

        registeredModels.forEach { (modelId, pre) ->
            // If the same model appears twice it gets loaded only once
            // and here it gets shared to all registered models with the same location
            val model = cache[pre.location] ?: return@forEach

            // Collects all textures needed for the baking process
            if (pre.bake) {
                textures += model.getTextures({ null }, errors)
            }

            // Allow to alter the model before it gets baked
            val processedModel = pre.preBake?.invoke(modelId, model) ?: model

            // Apply filter if specified and supported
            val finalModel = if (pre.partFilter != null) {
                if (processedModel is FilterableModel) processedModel.filterParts(pre.partFilter) else processedModel
            } else {
                processedModel
            }

            models += PreBakeModel(modelId, pre, finalModel)
        }

        // Print texture errors
        if (errors.isNotEmpty()) {
            ModelLoaderMod.logger.warn("Found following texture errors:")
            errors.forEach { ModelLoaderMod.logger.warn("    $it") }
        }

        // Save values for other events
        textureToRegister = textures
        modelsToBake = models
    }

    /**
     * Called to get all textures needed for models
     */
    fun onTextureStitchEvent(event: TextureStitchEvent.Pre) {
        // Register all textures
        textureToRegister?.forEach { event.addSprite(it) }
        textureToRegister = null
    }

    /**
     * Called to bake all models and animations
     */
    fun onModelBakeEvent(event: ModelBakeEvent) {
        val modelsToBake = modelsToBake ?: return
        ModelManager.modelsToBake = null

        // Bake models
        val bakedModels = if (Config.useMultithreading.get()) {
            modelsToBake
                .toList()
                .parallelStream()
                .map { processModel(it, event.modelLoader) }
                .collect(Collectors.toList())
        } else {
            modelsToBake.map { processModel(it, event.modelLoader) }
        }

        loadedModels = bakedModels.map { it.modelId to it }.toMap()

        // Register baked models
        bakedModels.forEach { (modelId, pre, bakedModel) ->
            // Ignore null models
            bakedModel ?: return@forEach

            // Set item transformation for gui, ground, etc
            if (bakedModel is IItemTransformable) {
                bakedModel.setItemTransforms(pre.itemTransforms)
                bakedModel.setHasItemRenderer(pre.itemRenderer)
            }

            // Allow to alter the model after it gets baked
            val finalModel = pre.postBake?.invoke(modelId, bakedModel) ?: bakedModel

            // Register the model into the game, so it can be used in any block/item that uses the same model id
            event.modelRegistry[modelId] = finalModel
        }

        // Share models
        sharedModels.forEach { (destine, origin) ->
            val model = event.modelRegistry[origin]
            if (model != null) {
                event.modelRegistry[destine] = model
            } else {
                ModelLoaderMod.logger.error("Unable to share model $origin, it doesn't exist")
            }
        }

        try {
            ModLoader.get().postEvent(ModelRetrieveEvent(loadedModels))
        } catch (e: Exception) {
            ModelLoaderMod.logger.error("Error in ModelRetrieveEvent, some models may be missing, check the log for details")
        }
    }

    /**
     * Internal method: bake models and create animations
     */
    private fun processModel(model: PreBakeModel, loader: ModelLoader): PostBakeModel {
        val baked = if (model.pre.bake){
            bakeModel(model.unbaked, loader, model.pre.rotation)
        }
        else {
            null
        }

        val animations = if (model.pre.animate && model.unbaked is AnimatableModel) {
            model.unbaked.getAnimations()
        } else {
            emptyMap()
        }

        val mutable = if (model.pre.mutable && model.unbaked is MutableModelConversion) {
            model.unbaked.toMutable(ModelLoader.defaultTextureGetter(), DefaultVertexFormats.ITEM)
        } else {
            null
        }

        return PostBakeModel(model.modelId, model.pre, baked, animations, mutable)
    }

    /**
     * Bake a single model
     */
    fun bakeModel(model: IUnbakedModel, loader: ModelLoader, rotation: ISprite): IBakedModel? {
        return model.bake(loader, ModelLoader.defaultTextureGetter(), rotation, DefaultVertexFormats.ITEM)
    }
}

