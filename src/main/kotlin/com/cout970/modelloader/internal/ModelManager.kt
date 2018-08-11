package com.cout970.modelloader.internal

import com.cout970.modelloader.ModelLoaderMod
import com.cout970.modelloader.api.IBakedModelDecorator
import com.cout970.modelloader.api.Model
import com.cout970.modelloader.api.ModelEntry
import com.cout970.modelloader.api.formats.gltf.GltfModel
import com.cout970.modelloader.api.formats.mcx.McxModel
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
import net.minecraftforge.client.model.obj.OBJLoader
import net.minecraftforge.client.model.obj.OBJModel
import net.minecraftforge.common.model.TRSRTransformation
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.streams.toList

/**
 * Created by cout970 on 2017/03/05.
 */
object ModelManager {

    data class ModelRegistration(
            val modelId: ModelResourceLocation,
            val modelLocation: ResourceLocation,
            val bake: Boolean,
            val decorator: IBakedModelDecorator?
    )

    // registered models
    internal val models = mutableListOf<ModelRegistration>()
    private val texturesToRegister = mutableSetOf<ResourceLocation>()
    private val modelsToBake = mutableListOf<Pair<ModelRegistration, IModel>>()

    // models loaded by this class
    internal val loadedModels = mutableMapOf<ModelResourceLocation, ModelEntry>()

    @SubscribeEvent
    fun onTextureStart(event: TextureStitchEvent.Pre) {
        // loads all models to get the textures needed
        loadAll()
        //register every texture once
        texturesToRegister.forEach {
            event.map.registerSprite(it)
        }
        texturesToRegister.clear()
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    fun onModelBakeEvent(event: ModelBakeEvent) {
        // Bake all models
        val bakedModels = if (ModelLoaderMod.useMultiThreading) {
            modelsToBake
                    .toList()
                    .parallelStream()
                    .map { (id, model) -> id to bake(model) }
                    .toList()
        } else {
            modelsToBake
                    .map { (id, model) -> id to bake(model) }
        }

        // Fill missing loadedModels entries
        val idToModel = modelsToBake.map { it.first.modelId to it.second }.toMap()

        bakedModels.forEach { (reg, baked) ->
            loadedModels[reg.modelId] = ModelEntry(baked, idToModel[reg.modelId]!!.wrap())
        }

        // Clear cache
        modelsToBake.clear()

        // Register baked models
        bakedModels.forEach { (reg, bakedModel) ->
            // uses the User-defined decorator to wrap or edit the baked model
            val finalModel = reg.decorator?.decorate(bakedModel, reg.modelId) ?: bakedModel

            // register the model into the game
            event.modelRegistry.putObject(reg.modelId, finalModel)
        }
    }

    private fun loadAll() {
        val manager = Minecraft.getMinecraft().resourceManager
        //makes sure all caches are empty
        texturesToRegister.clear()
        modelsToBake.clear()
        loadedModels.clear()

        // loads every model
        val cache = if (ModelLoaderMod.useMultiThreading) {
            models.parallelStream()
                    .map { it.modelLocation }
                    .distinct()
                    .map { it to loadModel(manager, it) }
                    .toList()
                    .toMap()
        } else {
            models.asSequence()
                    .map { it.modelLocation }
                    .distinct()
                    .map { it to loadModel(manager, it) }
                    .toMap()
        }

        models.forEach { reg ->
            val model = cache[reg.modelLocation]!!

            if (reg.bake) {
                // collects all textures from the model
                texturesToRegister.addAll(model.textures)
                // Saves the model to bake it later
                modelsToBake += reg to model
            } else {
                // Models that doesn't get baked are stored directly
                loadedModels[reg.modelId] = ModelEntry(null, model.wrap())
            }
        }
    }

    private fun loadModel(manager: IResourceManager, location: ResourceLocation): IModel {
        try {
            val resource = manager.getResource(location).inputStream
            val extension = location.resourcePath.substringAfterLast('.', "")

            return when (extension) {
                "obj" -> OBJLoader.INSTANCE.loadModel(location)
                "gltf" -> GltfModelSerializer.load(manager, location, resource)
                else -> McxModelSerializer.load(resource)
            }
        } catch (e: Exception) {
            ModelLoaderMod.logger.error("Error reading model data for location: $this")
            e.printStackTrace()
        }
        return ModelLoaderRegistry.getMissingModel()
    }

    private fun IModel.wrap(): Model = when (this) {
        is McxModel -> Model.Mcx(this)
        is GltfModel -> Model.Gltf(this)
        is OBJModel -> Model.Obj(this)
        else -> Model.Missing
    }

    private fun bake(model: IModel): IBakedModel {
        return model.bake(TRSRTransformation.identity(), DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter())
    }
}