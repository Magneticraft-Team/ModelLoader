package com.cout970.modelloader

import net.minecraft.client.renderer.model.IBakedModel
import net.minecraft.client.renderer.model.IUnbakedModel
import net.minecraft.client.renderer.model.ModelBakery
import net.minecraft.client.renderer.texture.ISprite
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.vertex.VertexFormat
import net.minecraft.resources.IResourceManager
import net.minecraft.util.ResourceLocation
import java.util.function.Function

interface IFormatHandler {
    fun loadModel(resourceManager: IResourceManager, modelLocation: ResourceLocation): IUnbakedModel
}

object ModelFormatRegistry {
    private val registry = mutableMapOf<String, IFormatHandler>()

    fun loadUnbakedModel(resourceManager: IResourceManager, modelLocation: ResourceLocation): IUnbakedModel {
        val extension = modelLocation.path.substringAfterLast('.', "missing")
        val handler = registry[extension]

        if (handler == null) {
            ModelLoaderMod.logger.error("Unable to load model with $extension extension: $modelLocation")
            return NullUnbakedModel
        }

        return handler.loadModel(resourceManager, modelLocation)
    }

    fun supportsExtension(extension: String): Boolean = registry.containsKey(extension)

    fun registerHandler(extension: String, handler: IFormatHandler) {
        registry[extension] = handler
    }
}

object NullUnbakedModel : IUnbakedModel {
    override fun bake(bakery: ModelBakery, spriteGetter: Function<ResourceLocation, TextureAtlasSprite>, sprite: ISprite, format: VertexFormat): IBakedModel? = null

    override fun getTextures(modelGetter: Function<ResourceLocation, IUnbakedModel>, missingTextureErrors: MutableSet<String>): MutableCollection<ResourceLocation> = mutableListOf()

    override fun getDependencies(): MutableCollection<ResourceLocation> = mutableListOf()
}