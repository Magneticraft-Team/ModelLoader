package com.cout970.modelloader

import com.cout970.modelloader.api.ItemTransforms
import com.cout970.modelloader.api.ModelConfig
import com.cout970.modelloader.api.ModelRegisterEvent
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

data class PreBakeModel(
    val modelId: ModelResourceLocation,
    val pre: ModelConfig,
    val unbaked: IUnbakedModel
)

data class PostBakeModel(
    val modelId: ModelResourceLocation,
    val pre: ModelConfig,
    val baked: IBakedModel?
)

interface IItemTransformable {
    fun setItemTransforms(it: ItemTransforms)
}

object ModelManager {
    private val registeredModels = mutableMapOf<ModelResourceLocation, ModelConfig>()
    private var textureToRegister: Set<ResourceLocation>? = null
    private var modelsToBake: List<PreBakeModel>? = null
    private var loadedModels: Map<ModelResourceLocation, PostBakeModel> = emptyMap()

    fun register(modelId: ModelResourceLocation, pre: ModelConfig) {
        registeredModels[modelId] = pre
    }

    fun loadModelFiles(resourceManager: IResourceManager) {
        ModLoader.get().postEvent(ModelRegisterEvent(registeredModels))

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
            val finalModel = pre.preBake?.invoke(modelId, model) ?: model
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

    fun onTextureStitchEvent(event: TextureStitchEvent.Pre) {
        // Register all textures
        textureToRegister?.forEach { event.addSprite(it) }
        textureToRegister = null
    }

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
            }

            // Allow to alter the model after it gets baked
            val finalModel = pre.postBake?.invoke(modelId, bakedModel) ?: bakedModel

            // Register the model into the game, so it can be used in any block/item that uses the same model id
            event.modelRegistry[modelId] = finalModel
        }
    }

    private fun processModel(model: PreBakeModel, loader: ModelLoader): PostBakeModel {
        val baked = if (model.pre.bake) bakeModel(model.unbaked, loader, model.pre.rotation) else null
        return PostBakeModel(model.modelId, model.pre, baked)
    }

    fun bakeModel(model: IUnbakedModel, loader: ModelLoader, rotation: ISprite): IBakedModel? {
        return model.bake(loader, ModelLoader.defaultTextureGetter(), rotation, DefaultVertexFormats.ITEM)
    }
}

